/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ClusterSingletonServiceGroup} on top of the Entitiy Ownership Service. Since EOS is atomic
 * in its operation and singleton services incur startup and most notably cleanup, we need to do something smart here.
 *
 * <p>
 * The implementation takes advantage of the fact that EOS provides stable ownership, i.e. owners are not moved as
 * a result on new candidates appearing. We use two entities:
 * - service entity, to which all nodes register
 * - cleanup entity, which only the service entity owner registers to
 *
 * <p>
 * Once the cleanup entity ownership is acquired, services are started. As long as the cleanup entity is registered,
 * it should remain the owner. In case a new service owner emerges, the old owner will start the cleanup process,
 * eventually releasing the cleanup entity. The new owner registers for the cleanup entity -- but will not see it
 * granted until the old owner finishes the cleanup.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 */
@ThreadSafe
final class ClusterSingletonServiceGroupImpl<P extends Path<P>, E extends GenericEntity<P>,
        C extends GenericEntityOwnershipChange<P, E>,  G extends GenericEntityOwnershipListener<P, C>,
        S extends GenericEntityOwnershipService<P, E, G>> extends ClusterSingletonServiceGroup<P, E, C> {

    private enum EntityState {
        /**
         * This entity was never registered.
         */
        UNREGISTERED,
        /**
         * Registration exists, but we are waiting for it to resolve.
         */
        REGISTERED,
        /**
         * Registration indicated we are the owner.
         */
        OWNED,
        /**
         * Registration indicated we are the owner, but global state is uncertain -- meaning there can be owners in
         * another partition, for example.
         */
        OWNED_JEOPARDY,
        /**
         * Registration indicated we are not the owner. In this state we do not care about global state, therefore we
         * do not need an UNOWNED_JEOPARDY state.
         */
        UNOWNED,
    }

    private enum ServiceState {
        /**
         * Local services are stopped.
         */
        STOPPED,
        /**
         * Local services are up and running.
         */
        // FIXME: we should support async startup, which will require a STARTING state.
        STARTED,
        /**
         * Local services are being stopped.
         */
        STOPPING,
    }

    private enum State {
        /**
         * This group has been freshly allocated and has not been started yet.
         */
        INITIAL,
        /**
         * Operational state. Service entity is registered, but ownership was not resolved yet.
         */
        REGISTERED,
        /**
         * Operational state. Service entity confirmed to be follower.
         */
        STANDBY,
        /**
         * Service entity acquired. Attempting to acquire cleanup entity.
         */
        TAKING_OWNERSHIP,
        /**
         * Both entities held and user services are being started.
         */
        STARTING_SERVICES,
        /**
         * Steady state. Both entities held and services have finished starting.
         */
        OWNER,
        /**
         * User services are being stopped due to either loss of an entity or a shutdown.
         */
        STOPPING_SERVICES,
        /**
         * We have stopped services and are now relinquishing the cleanup entity.
         */
        RELEASING_OWNERSHIP,
        /**
         * Terminated, this group cannot be used anymore.
         */
        TERMINATED
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClusterSingletonServiceGroupImpl.class);

    private final S entityOwnershipService;
    private final String identifier;

    /* Entity instances */
    private final E serviceEntity;
    private final E cleanupEntity;

    private final ReentrantLock lock = new ReentrantLock(true);

    @GuardedBy("lock")
    private final List<ClusterSingletonService> serviceGroup;

    /*
     * State tracking is quite involved, as we are tracking up to four asynchronous sources of events:
     * - user calling close()
     * - service entity ownership
     * - cleanup entity ownership
     * - service shutdown future
     *
     * Absolutely correct solution would be a set of behaviors, which govern each state, remembering where we want to
     * get to and what we are doing. That would result in ~15 classes which would quickly render this code unreadable
     * due to boilerplate overhead.
     *
     * We therefore take a different approach, tracking state directly in this class and evaluate state transitions
     * based on recorded bits -- without explicit representation of state machine state.
     */
    /**
     * Group close future. In can only go from null to non-null reference. Whenever it is non-null, it indicates that
     * the user has closed the group and we are converging to termination.
     */
    // We are using volatile get-and-set to support non-blocking close(). It may be more efficient to inline it here,
    // as we perform a volatile read after unlocking -- that volatile read may easier on L1 cache.
    // XXX: above needs a microbenchmark contention ever becomes a problem.
    private final AtomicReference<SettableFuture<Void>> closeFuture = new AtomicReference<>();

    /**
     * Service (base) entity registration. This entity selects an owner candidate across nodes. Candidates proceed to
     * acquire {@link #cleanupEntity}.
     */
    @GuardedBy("lock")
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityReg = null;
    /**
     * Service (base) entity last reported state.
     */
    @GuardedBy("lock")
    private EntityState serviceEntityState = EntityState.UNREGISTERED;

    /**
     * Cleanup (owner) entity registration. This entity guards access to service state and coordinates shutdown cleanup
     * and startup.
     */
    @GuardedBy("lock")
    private GenericEntityOwnershipCandidateRegistration<P, E> cleanupEntityReg;
    /**
     * Cleanup (owner) entity last reported state.
     */
    @GuardedBy("lock")
    private EntityState cleanupEntityState = EntityState.UNREGISTERED;

    /**
     * Optional event capture list. This field is initialized when we interact with entity ownership service, to capture
     * events reported during EOS method invocation -- like immediate acquisition of entity when we register it. This
     * prevents bugs from recursion.
     */
    @GuardedBy("lock")
    private List<C> capture = null;

    /**
     * State of local services.
     */
    @GuardedBy("lock")
    private ServiceState localServicesState = ServiceState.STOPPED;

    /**
     * Class constructor. Note: last argument is reused as-is.
     *
     * @param identifier non-empty string as identifier
     * @param mainEntity as Entity instance
     * @param closeEntity as Entity instance
     * @param entityOwnershipService GenericEntityOwnershipService instance
     * @param parent parent service
     * @param services Services list
     */
    ClusterSingletonServiceGroupImpl(final String identifier, final S entityOwnershipService, final E mainEntity,
            final E closeEntity, final List<ClusterSingletonService> services) {
        Preconditions.checkArgument(!identifier.isEmpty(), "Identifier may not be empty");
        this.identifier = identifier;
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.serviceEntity = Preconditions.checkNotNull(mainEntity);
        this.cleanupEntity = Preconditions.checkNotNull(closeEntity);
        this.serviceGroup = Preconditions.checkNotNull(services);
        LOG.debug("Instantiated new service group for {}", identifier);
    }

    @VisibleForTesting
    ClusterSingletonServiceGroupImpl(final String identifier, final E mainEntity,
            final E closeEntity, final S entityOwnershipService) {
        this(identifier, entityOwnershipService, mainEntity, closeEntity, new ArrayList<>(1));
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    ListenableFuture<?> closeClusterSingletonGroup() {
        // Assert our future first
        final SettableFuture<Void> future = SettableFuture.create();
        final SettableFuture<Void> existing = closeFuture.getAndSet(future);
        if (existing != null) {
            return existing;
        }

        if (!lock.tryLock()) {
            // The lock is held, the cleanup will be finished by the owner thread
            LOG.debug("Singleton group {} cleanup postponed", identifier);
            return future;
        }

        try {
            lockedClose(future);
        } finally {
            lock.unlock();
        }

        LOG.debug("Service group {} {}", identifier, future.isDone() ? "closed" : "closing");
        return future;
    }

    private boolean isClosed() {
        return closeFuture.get() != null;
    }

    @GuardedBy("lock")
    private void startCapture() {
        Verify.verify(capture == null, "Service group {} is already capturing events {}", identifier, capture);
        capture = new ArrayList<>(0);
    }

    private List<C> endCapture() {
        final List<C> ret = Verify.verifyNotNull(capture, "Service group {} is not currently capturing", identifier);
        capture = null;
        return ret;
    }

    @GuardedBy("lock")
    private void lockedClose(final SettableFuture<Void> future) {
        if (serviceEntityReg != null) {
            // We are still holding the service registration, close it now...
            LOG.debug("Service group {} unregistering service entity {}", identifier, serviceEntity);
            startCapture();
            serviceEntityReg.close();
            serviceEntityReg = null;

            // This can potentially mutate our state, so all previous checks need to be re-validated.
            endCapture().forEach(this::lockedOwnershipChanged);
        }

        // Now check service entity state: if it is still owned, we need to wait until it is acknowledged as
        // unregistered.
        switch (serviceEntityState) {
            case REGISTERED:
            case UNOWNED:
            case UNREGISTERED:
                // We have either successfully shut down, or have never started up, proceed with termination
                break;
            case OWNED:
                // We have unregistered, but EOS has not reported our loss of ownership. We will continue with shutdown
                // when that loss is reported.
                LOG.debug("Service group {} is still owned, postponing termination", identifier);
                return;
            case OWNED_JEOPARDY:
                // This is a significant event, as it relates to cluster split/join operations, operators need to know
                // we are waiting for cluster join event.
                LOG.info("Service group {} is still owned with split cluster, postponing termination", identifier);
                return;
            default:
                throw new IllegalStateException("Unhandled service entity state " + serviceEntityState);
        }

        // We do not own service entity state: we need to ensure services are stopped.
        if (stopServices()) {
            LOG.debug("Service group {} started shutting down services, postponing termination", identifier);
            return;
        }

        // Local cleanup completed, release cleanup entity if needed
        if (cleanupEntityReg != null) {
            LOG.debug("Service group {} unregistering service entity {}", identifier, serviceEntity);
            startCapture();
            cleanupEntityReg.close();
            cleanupEntityReg = null;

            // This can potentially mutate our state, so all previous checks need to be re-validated.
            endCapture().forEach(this::lockedOwnershipChanged);
        }

        switch (cleanupEntityState) {
            case REGISTERED:
            case UNOWNED:
            case UNREGISTERED:
                // We have either successfully shut down, or have never started up, proceed with termination
                break;
            case OWNED:
                // We have unregistered, but EOS has not reported our loss of ownership. We will continue with shutdown
                // when that loss is reported.
                LOG.debug("Service group {} is still owns cleanup, postponing termination", identifier);
                return;
            case OWNED_JEOPARDY:
                // This is a significant event, as it relates to cluster split/join operations, operators need to know
                // we are waiting for cluster join event.
                LOG.info("Service group {} is still owns cleanup with split cluster, postponing termination",
                    identifier);
                return;
            default:
                throw new IllegalStateException("Unhandled cleanup entity state " + serviceEntityState);
        }

        // No registrations left and no service operations pending, we are done
        LOG.debug("Service group {} completing termination", identifier);
        future.set(null);
    }

    @Override
    void initialize() throws CandidateAlreadyRegisteredException {
        lock.lock();
        try {
            Preconditions.checkState(serviceEntityState == EntityState.UNREGISTERED,
                    "Singleton group %s was already initilized", identifier);

            LOG.debug("Initializing service group {} with services {}", identifier, serviceGroup);
            startCapture();
            serviceEntityReg = entityOwnershipService.registerCandidate(serviceEntity);
            serviceEntityState = EntityState.REGISTERED;
            endCapture().forEach(this::lockedOwnershipChanged);
        } finally {
            lock.unlock();
        }
    }

    private void checkNotClosed() {
        Preconditions.checkState(closeFuture.get() == null, "Service group %s has already been closed",
                identifier);
    }

    @Override
    void registerService(final ClusterSingletonService service) {
        Verify.verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        lock.lock();
        try {
            Preconditions.checkState(serviceEntityState != EntityState.UNREGISTERED,
                    "Service group %s is not initialized yet", identifier);

            LOG.debug("Adding service {} to service group {}", service, identifier);
            serviceGroup.add(service);

            switch (localServicesState) {
                case STARTED:
                    LOG.debug("Service group {} starting late-registered service {}", identifier, service);
                    service.instantiateServiceInstance();
                    break;
                case STOPPED:
                case STOPPING:
                    break;
                default:
                    throw new IllegalStateException("Unhandled local services state " + localServicesState);
            }
        } finally {
            lock.unlock();
            finishCloseIfNeeded();
        }
    }

    @CheckReturnValue
    @Override
    boolean unregisterService(final ClusterSingletonService service) {
        Verify.verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        lock.lock();
        try {
            // There is a slight problem here, as the type does not match the list type, hence we need to tread
            // carefully.
            if (serviceGroup.size() == 1) {
                Verify.verify(serviceGroup.contains(service));
                return true;
            }

            Verify.verify(serviceGroup.remove(service));
            LOG.debug("Service {} was removed from group.", service.getIdentifier().getValue());

            switch (localServicesState) {
                case STARTED:
                    LOG.warn("Service group {} stopping unregistered service {}", identifier, service);
                    service.closeServiceInstance();
                    break;
                case STOPPED:
                case STOPPING:
                    break;
                default:
                    throw new IllegalStateException("Unhandled local services state " + localServicesState);
            }

            return false;
        } finally {
            lock.unlock();
            finishCloseIfNeeded();
        }
    }

    @Override
    void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} for ClusterSingletonServiceGroup {}", ownershipChange, identifier);

        lock.lock();
        try {
            if (capture != null) {
                capture.add(ownershipChange);
            } else {
                lockedOwnershipChanged(ownershipChange);
            }
        } finally {
            lock.unlock();
            finishCloseIfNeeded();
        }
    }

    /**
     * Handle an ownership change with the lock held. Callers are expected to handle termination conditions, this method
     * and anything it calls must not call {@link #lockedClose(SettableFuture)}.
     *
     * @param ownershipChange
     */
    @GuardedBy("lock")
    private void lockedOwnershipChanged(final C ownershipChange) {
        final E entity = ownershipChange.getEntity();
        if (serviceEntity.equals(entity)) {
            serviceOwnershipChanged(ownershipChange.getState(), ownershipChange.inJeopardy());
        } else if (cleanupEntity.equals(entity)) {
            cleanupCandidateOwnershipChanged(ownershipChange.getState(), ownershipChange.inJeopardy());
        } else {
            LOG.warn("Group {} received unrecognized change {}", identifier, ownershipChange);
        }
    }

    private void cleanupCandidateOwnershipChanged(final EntityOwnershipChangeState state, final boolean jeopardy) {
        if (jeopardy) {
            switch (state) {
                case LOCAL_OWNERSHIP_GRANTED:
                case LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE:
                    LOG.warn("Service group {} cleanup entity owned without certainty", identifier);
                    cleanupEntityState = EntityState.OWNED_JEOPARDY;
                    break;
                case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
                case LOCAL_OWNERSHIP_LOST_NO_OWNER:
                case REMOTE_OWNERSHIP_CHANGED:
                case REMOTE_OWNERSHIP_LOST_NO_OWNER:
                    LOG.info("Service group {} cleanup entity ownership uncertain", identifier);
                    cleanupEntityState = EntityState.UNOWNED;
                    break;
                default:
                    throw new IllegalStateException("Unhandled cleanup entity jeopardy change " + state);
            }

            stopServices();
            return;
        }

        if (cleanupEntityState == EntityState.OWNED_JEOPARDY) {
            // Pair info message with previous jeopardy
            LOG.info("Service group {} cleanup entity ownership ascertained", identifier);
        }

        switch (state) {
            case LOCAL_OWNERSHIP_GRANTED:
            case LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE:
                cleanupEntityState = EntityState.OWNED;

                switch (localServicesState) {
                    case STARTED:
                        LOG.debug("Service group {} already has local services running", identifier);
                        break;
                    case STOPPED:
                        startServices();
                        break;
                    case STOPPING:
                        LOG.debug("Service group {} has local services stopping, postponing startup", identifier);
                        break;
                    default:
                        throw new IllegalStateException("Unhandled local services state " + localServicesState);
                }
                break;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
                cleanupEntityState = EntityState.UNOWNED;
                stopServices();
                break;
            case REMOTE_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_CHANGED:
                cleanupEntityState = EntityState.UNOWNED;
                break;
            default:
                LOG.warn("Service group {} ignoring unhandled cleanup entity change {}", identifier, state);
                break;
        }
    }

    private void serviceOwnershipChanged(final EntityOwnershipChangeState state, final boolean jeopardy) {
        if (jeopardy) {
            LOG.info("Service group {} service entity ownership uncertain", identifier);

            // Service entity ownership is uncertain, which means we want to record the state, but we do not want
            // to stop local services nor do anything with the cleanup entity.
            switch (state) {
                case LOCAL_OWNERSHIP_GRANTED:
                case LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE:
                    serviceEntityState = EntityState.OWNED_JEOPARDY;
                    break;
                case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
                case LOCAL_OWNERSHIP_LOST_NO_OWNER:
                case REMOTE_OWNERSHIP_CHANGED:
                case REMOTE_OWNERSHIP_LOST_NO_OWNER:
                    serviceEntityState = EntityState.UNOWNED;
                    break;
                default:
                    throw new IllegalStateException("Unhandled cleanup entity jeopardy change " + state);
            }
            return;
        }

        if (serviceEntityState == EntityState.OWNED_JEOPARDY) {
            // Pair info message with previous jeopardy
            LOG.info("Service group {} service entity ownership ascertained", identifier);
        }

        switch (state) {
            case LOCAL_OWNERSHIP_GRANTED:
            case LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE:
                serviceEntityState = EntityState.OWNED;
                takeOwnership();
                break;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
                LOG.debug("Service group {} lost service entity ownership", identifier);
                serviceEntityState = EntityState.UNOWNED;
                if (stopServices()) {
                    LOG.debug("Service group {} already stopping services, postponing cleanup", identifier);
                    return;
                }

                if (cleanupEntityReg != null) {
                    cleanupEntityReg.close();
                    cleanupEntityReg = null;
                }
                break;
            case REMOTE_OWNERSHIP_CHANGED:
            case REMOTE_OWNERSHIP_LOST_NO_OWNER:
                // No need to react, just update the state
                serviceEntityState = EntityState.UNOWNED;
                break;
            default:
                LOG.warn("Service group {} ignoring unhandled cleanup entity change {}", identifier, state);
                break;
        }
    }

    private void finishCloseIfNeeded() {
        final SettableFuture<Void> future = closeFuture.get();
        if (future != null) {
            lock.lock();
            try {
                lockedClose(future);
            } finally {
                lock.unlock();
            }
        }
    }

    /*
     * Help method to registered DoubleCandidateEntity. It is first step
     * before the actual instance take Leadership.
     */
    private void takeOwnership() {
        if (isClosed()) {
            LOG.debug("Service group {} is closed, skipping cleanup ownership bid", identifier);
            return;
        }

        LOG.debug("Service group {} registering cleanup entity", identifier);

        startCapture();
        cleanupEntityState = EntityState.REGISTERED;
        try {
            cleanupEntityReg = entityOwnershipService.registerCandidate(cleanupEntity);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.error("Service group {} failed to take ownership", identifier, e);
        }

        endCapture().forEach(this::lockedOwnershipChanged);
    }

    /*
     * Help method calls instantiateServiceInstance method for create single cluster-wide service instance.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void startServices() {
        if (isClosed()) {
            LOG.debug("Service group {} is closed, not starting services", identifier);
            return;
        }

        LOG.debug("Service group {} starting services", identifier);
        serviceGroup.forEach(service -> {
            LOG.debug("Starting service {}", service);
            try {
                service.instantiateServiceInstance();
            } catch (Exception e) {
                LOG.warn("Service group {} service {} failed to start, attempting to continue", identifier, service, e);
            }
        });

        localServicesState = ServiceState.STARTED;
        LOG.debug("Service group {} services started", identifier);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    boolean stopServices() {
        switch (localServicesState) {
            case STARTED:
                localServicesState = ServiceState.STOPPING;

                final List<ListenableFuture<Void>> serviceCloseFutureList = new ArrayList<>(serviceGroup.size());
                for (final ClusterSingletonService service : serviceGroup) {
                    final ListenableFuture<Void> future;

                    try {
                        future = service.closeServiceInstance();
                    } catch (Exception e) {
                        LOG.warn("Service group {} service {} failed to stop, attempting to continue", identifier,
                            service, e);
                        continue;
                    }

                    serviceCloseFutureList.add(future);
                }

                Futures.addCallback(Futures.allAsList(serviceCloseFutureList), new FutureCallback<List<Void>>() {
                    @Override
                    public void onFailure(final Throwable cause) {
                        LOG.warn("Service group {} service stopping reported error", identifier, cause);
                        onServicesStopped();
                    }

                    @Override
                    public void onSuccess(final List<Void> nulls) {
                        onServicesStopped();
                    }
                }, MoreExecutors.directExecutor());

                return localServicesState == ServiceState.STOPPING;
            case STOPPED:
                return false;
            case STOPPING:
                return true;
            default:
                throw new IllegalStateException("Unhandled local services state " + localServicesState);
        }
    }

    void onServicesStopped() {
        LOG.debug("Service group {} finished stopping services", identifier);
        lock.lock();
        try {
            localServicesState = ServiceState.STOPPED;

            // FIXME: check if we need to initiate a shutdown (via entity ownership etc.)


        } finally {
            lock.unlock();
            finishCloseIfNeeded();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
    }
}
