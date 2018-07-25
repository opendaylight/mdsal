/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
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
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
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

    enum ServiceState {
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

    private static final Logger LOG = LoggerFactory.getLogger(ClusterSingletonServiceGroupImpl.class);

    private final S entityOwnershipService;
    private final String identifier;

    /* Entity instances */
    private final E serviceEntity;
    private final E cleanupEntity;

    private final StampedLock configLock = new StampedLock();
    private final Set<ClusterSingletonServiceRegistration> serviceGroup = ConcurrentHashMap.newKeySet();

    // We are using a simple semaphore because we explicitly do not want the lock to succeed if we re-enter our code
    private final Semaphore applyLock = new Semaphore(1, true);
    @GuardedBy("applyLock")
    private final Map<ClusterSingletonServiceRegistration, ServiceInfo> services = new HashMap<>();

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
    @GuardedBy("applyLock")
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityReg = null;
    /**
     * Service (base) entity last reported state.
     */
    @GuardedBy("applyLock")
    private EntityState serviceEntityState = EntityState.UNREGISTERED;

    /**
     * Cleanup (owner) entity registration. This entity guards access to service state and coordinates shutdown cleanup
     * and startup.
     */
    @GuardedBy("applyLock")
    private GenericEntityOwnershipCandidateRegistration<P, E> cleanupEntityReg;
    /**
     * Cleanup (owner) entity last reported state.
     */
    @GuardedBy("applyLock")
    private EntityState cleanupEntityState = EntityState.UNREGISTERED;

    /**
     * Optional event capture list. This field is initialized when we interact with entity ownership service, to capture
     * events reported during EOS method invocation -- like immediate acquisition of entity when we register it. This
     * prevents bugs from recursion.
     */
    @GuardedBy("applyLock")
    private List<C> capture = null;

    /**
     * State of local services.
     */
    @GuardedBy("applyLock")
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
            final E closeEntity, final Collection<ClusterSingletonServiceRegistration> services) {
        checkArgument(!identifier.isEmpty(), "Identifier may not be empty");
        this.identifier = identifier;
        this.entityOwnershipService = requireNonNull(entityOwnershipService);
        this.serviceEntity = requireNonNull(mainEntity);
        this.cleanupEntity = requireNonNull(closeEntity);
        serviceGroup.addAll(services);

        LOG.debug("Instantiated new service group for {}", identifier);
    }

    @VisibleForTesting
    ClusterSingletonServiceGroupImpl(final String identifier, final E mainEntity,
            final E closeEntity, final S entityOwnershipService) {
        this(identifier, entityOwnershipService, mainEntity, closeEntity, ImmutableList.of());
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

        if (!applyLock.tryAcquire()) {
            // The lock is held, the cleanup will be finished by the owner thread
            LOG.debug("Singleton group {} cleanup postponed", identifier);
            return future;
        }

        try {
            lockedClose(future);
        } finally {
            applyLock.release();
        }

        LOG.debug("Service group {} {}", identifier, future.isDone() ? "closed" : "closing");
        return future;
    }

    private boolean isClosed() {
        return closeFuture.get() != null;
    }

    @GuardedBy("lock")
    private void startCapture() {
        verify(capture == null, "Service group {} is already capturing events {}", identifier, capture);
        capture = new ArrayList<>(0);
        LOG.debug("Service group {} started capturing events", identifier);
    }

    private List<C> endCapture() {
        final List<C> ret = verifyNotNull(capture, "Service group {} is not currently capturing", identifier);
        capture = null;
        LOG.debug("Service group {} finished capturing events, {} events to process", identifier, ret.size());
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
            LOG.debug("Service group {} unregistering cleanup entity {}", identifier, cleanupEntity);
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
        verify(applyLock.tryAcquire());
        try {
            checkState(serviceEntityState == EntityState.UNREGISTERED, "Singleton group %s was already initilized",
                    identifier);

            LOG.debug("Initializing service group {} with services {}", identifier, serviceGroup);
            startCapture();
            serviceEntityReg = entityOwnershipService.registerCandidate(serviceEntity);
            serviceEntityState = EntityState.REGISTERED;
            endCapture().forEach(this::lockedOwnershipChanged);
        } finally {
            applyLock.release();
        }
    }

    private void checkNotClosed() {
        checkState(closeFuture.get() == null, "Service group %s has already been closed", identifier);
    }

    @Override
    void registerService(final ClusterSingletonServiceRegistration reg) {
        final ClusterSingletonService service = reg.getInstance();
        verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        final long stamp = configLock.writeLock();
        try {
            // First put the service
            LOG.debug("Adding service {} to service group {}", service, identifier);
            verify(serviceGroup.add(reg));
        } finally {
            configLock.unlockWrite(stamp);
        }

        if (!applyLock.tryAcquire()) {
            LOG.debug("Service group {} delayed register of {}", identifier, reg);
            return;
        }

        reconcileState();
    }

    @CheckReturnValue
    @Override
    boolean unregisterService(final ClusterSingletonServiceRegistration reg) {
        final ClusterSingletonService service = reg.getInstance();
        verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        final long stamp = configLock.writeLock();
        try {
            verify(serviceGroup.remove(reg));
        } finally {
            configLock.unlockWrite(stamp);
        }

        if (!applyLock.tryAcquire()) {
            LOG.debug("Service group {} delayed unregister of {}", identifier, reg);
            return false;
        }

        reconcileState();
        return true;
    }

    @Override
    void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} for ClusterSingletonServiceGroup {}", ownershipChange, identifier);

        applyLock.acquire();
        try {
            if (capture != null) {
                capture.add(ownershipChange);
            } else {
                lockedOwnershipChanged(ownershipChange);
            }
        } finally {
            applyLock.release();
            finishCloseIfNeeded();
        }
    }

    /**
     * Handle an ownership change with the lock held. Callers are expected to handle termination conditions, this method
     * and anything it calls must not call {@link #lockedClose(SettableFuture)}.
     *
     * @param ownershipChange reported change
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
                    if (cleanupEntityReg == null) {
                        LOG.debug("Service group {} ignoring cleanup entity ownership when unregistered", identifier);
                        return;
                    }

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
                if (cleanupEntityReg == null) {
                    LOG.debug("Service group {} ignoring cleanup entity ownership when unregistered", identifier);
                    return;
                }

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
                    if (serviceEntityReg == null) {
                        LOG.debug("Service group {} ignoring service entity ownership when unregistered", identifier);
                        return;
                    }

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
                if (serviceEntityReg == null) {
                    LOG.debug("Service group {} ignoring service entity ownership when unregistered", identifier);
                    return;
                }

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
            applyLock.acquire();
            try {
                lockedClose(future);
            } finally {
                applyLock.release();
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
        try {
            cleanupEntityReg = entityOwnershipService.registerCandidate(cleanupEntity);
            cleanupEntityState = EntityState.REGISTERED;
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
        serviceGroup.forEach(reg -> {
            final ClusterSingletonService service = reg.getInstance();
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

                final List<ListenableFuture<?>> serviceCloseFutureList = new ArrayList<>(serviceGroup.size());
                for (final ClusterSingletonServiceRegistration reg : serviceGroup) {
                    final ClusterSingletonService service = reg.getInstance();
                    final ListenableFuture<?> future;
                    try {
                        future = service.closeServiceInstance();
                    } catch (Exception e) {
                        LOG.warn("Service group {} service {} failed to stop, attempting to continue", identifier,
                            service, e);
                        continue;
                    }

                    serviceCloseFutureList.add(future);
                }

                LOG.debug("Service group {} initiated service shutdown", identifier);

                Futures.addCallback(Futures.allAsList(serviceCloseFutureList), new FutureCallback<List<?>>() {
                    @Override
                    public void onFailure(final Throwable cause) {
                        LOG.warn("Service group {} service stopping reported error", identifier, cause);
                        onServicesStopped();
                    }

                    @Override
                    public void onSuccess(final List<?> nulls) {
                        onServicesStopped();
                    }
                }, MoreExecutors.directExecutor());

                return localServicesState == ServiceState.STOPPING;
            case STOPPED:
                LOG.debug("Service group {} has already stopped services", identifier);
                return false;
            case STOPPING:
                LOG.debug("Service group {} is already stopping services", identifier);
                return true;
            default:
                throw new IllegalStateException("Unhandled local services state " + localServicesState);
        }
    }

    void onServicesStopped() {
        LOG.debug("Service group {} finished stopping services", identifier);
        applyLock.acquire();
        try {
            localServicesState = ServiceState.STOPPED;

            if (isClosed()) {
                LOG.debug("Service group {} closed, skipping service restart check", identifier);
                return;
            }

            // If we lost the service entity while services were stopping, we need to unregister cleanup entity
            switch (serviceEntityState) {
                case OWNED:
                case OWNED_JEOPARDY:
                    // No need to churn cleanup entity
                    break;
                case REGISTERED:
                case UNOWNED:
                case UNREGISTERED:
                    if (cleanupEntityReg != null) {
                        startCapture();
                        cleanupEntityReg.close();
                        cleanupEntityReg = null;
                        endCapture().forEach(this::lockedOwnershipChanged);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unhandled service entity state" + serviceEntityState);
            }

            if (cleanupEntityReg == null) {
                LOG.debug("Service group {} does not have cleanup entity registered, skipping restart check",
                    identifier);
                return;
            }

            // Double-check if the services should really be down
            switch (cleanupEntityState) {
                case OWNED:
                    // We have finished stopping services, but we own cleanup, e.g. we should start them again.
                    startServices();
                    return;
                case UNOWNED:
                case OWNED_JEOPARDY:
                case REGISTERED:
                case UNREGISTERED:
                    break;
                default:
                    throw new IllegalStateException("Unhandled cleanup entity state" + cleanupEntityState);
            }
        } finally {
            applyLock.release();
            finishCloseIfNeeded();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
    }

    // has to be called with applyLock, which will be released prior to returning
    private void reconcileState() {
        final Collection<Entry<ListenableFuture<?>, FutureCallback<Object>>> futures = new ArrayList<>();

        while (true) {
            final long stamp = configLock.tryOptimisticRead();
            final Set<ClusterSingletonServiceRegistration> localConfig = ImmutableSet.copyOf(serviceGroup);
            if (!configLock.validate(stamp)) {
                // The set moved while we were copying it, restart.
                continue;
            }

            reconcileState(localConfig, futures);
            if (configLock.validate(stamp)) {
                // No change in state, or someone is holding the lock -- bail out
                break;
            }
        }

        applyLock.release();
        futures.forEach(entry -> Futures.addCallback(entry.getKey(), entry.getValue(), MoreExecutors.directExecutor()));
    }

    @GuardedBy("applyLock")
    private void reconcileState(final Set<ClusterSingletonServiceRegistration> localConfig,
            final Collection<Entry<ListenableFuture<?>, FutureCallback<Object>>> futures) {
        // We are the sole thread observing entity state, now we need to reconcile all state
        switch (serviceEntityState) {
            case OWNED:
                ensureServicesStarting(localConfig, futures);
                break;
            case OWNED_JEOPARDY:
            case REGISTERED:
            case UNOWNED:
            case UNREGISTERED:
                ensureServicesStopping(futures);
                break;
            default:
                throw new IllegalStateException("Unhandled state " + serviceEntityState);
        }
    }

    @GuardedBy("applyLock")
    private void ensureServicesStarting(final Set<ClusterSingletonServiceRegistration> services,
            final Collection<Entry<ListenableFuture<?>, FutureCallback<Object>>> futures) {


        // TODO Auto-generated method stub

    }

    @GuardedBy("applyLock")
    private void ensureServicesStopping(final Collection<Entry<ListenableFuture<?>, FutureCallback<Object>>> futures) {
        final Iterator<Entry<ClusterSingletonServiceRegistration, ServiceInfo>> it = services.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ClusterSingletonServiceRegistration, ServiceInfo> entry = it.next();
            final ServiceInfo info = entry.getValue();

            switch (info.getState()) {
                case STARTED:
                    final ClusterSingletonServiceRegistration reg = entry.getKey();
                    final ClusterSingletonService service = reg.getInstance();

                    LOG.debug("Service group {} stopping service {}", identifier, service);
                    final ListenableFuture<?> future;
                    try {
                        future = service.closeServiceInstance();
                    } catch (Exception e) {
                        LOG.warn("Service group {} service {} failed to stop, attempting to continue", identifier,
                            service, e);
                        it.remove();
                        continue;
                    }

                    entry.setValue(info.toState(ServiceState.STOPPING, future));
                    futures.add(new SimpleImmutableEntry<>(future, new FutureCallback<Object>() {
                        @Override
                        public void onSuccess(final Object result) {
                            LOG.debug("Service group {} service {} stopped successfully", identifier, service);
                            serviceTransitionCompleted();
                        }

                        @Override
                        public void onFailure(final Throwable cause) {
                            LOG.debug("Service group {} service {} stopped with error", identifier, service,
                                cause);
                            serviceTransitionCompleted();
                        }
                    }));
                    break;
                case STOPPED:
                    it.remove();
                    LOG.debug("Service group {} removed stopped service {}", identifier, entry.getKey().getInstance());
                    break;
                case STOPPING:
                    // No-op
                    break;
                default:
                    throw new IllegalStateException("Unhandles state " + info.getState());
            }
        }
    }

    private void serviceTransitionCompleted() {
        // FIXME: try acquire lock, etc, etc.

        // TODO Auto-generated method stub
    }
}
