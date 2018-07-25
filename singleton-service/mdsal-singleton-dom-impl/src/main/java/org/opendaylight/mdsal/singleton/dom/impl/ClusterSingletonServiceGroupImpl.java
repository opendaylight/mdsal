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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
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

    private enum GroupState {
        /**
         * The group is currently supposed to be active.
         */
        ACTIVE,
        /**
         * The group is currently supposed to be inactive.
         */
        INACTIVE,
        /**
         * The group is to be destroyed.
         */
        DESTROYED;
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

    private final Set<ClusterSingletonServiceRegistration> members = ConcurrentHashMap.newKeySet();
    // Guarded by lock
    private final Map<ClusterSingletonServiceRegistration, ServiceInfo> services = new HashMap<>();

    private static final AtomicReferenceFieldUpdater<ClusterSingletonServiceGroupImpl, GroupState> TARGET_STATE_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ClusterSingletonServiceGroupImpl.class, GroupState.class,
            "targetState");
    private volatile GroupState targetState = GroupState.INACTIVE;

    // Marker for when any state changed
    private static final AtomicIntegerFieldUpdater<ClusterSingletonServiceGroupImpl> DIRTY_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ClusterSingletonServiceGroupImpl.class, "dirty");
    private volatile int dirty;

    // Simplified lock: non-reentrant, support tryLock() only
    private static final AtomicIntegerFieldUpdater<ClusterSingletonServiceGroupImpl> LOCK_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ClusterSingletonServiceGroupImpl.class, "lock");
    private volatile int lock;

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
    @GuardedBy("this")
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityReg = null;
    /**
     * Service (base) entity last reported state.
     */
    @GuardedBy("this")
    private EntityState serviceEntityState = EntityState.UNREGISTERED;

    /**
     * Cleanup (owner) entity registration. This entity guards access to service state and coordinates shutdown cleanup
     * and startup.
     */
    @GuardedBy("this")
    private GenericEntityOwnershipCandidateRegistration<P, E> cleanupEntityReg;
    /**
     * Cleanup (owner) entity last reported state.
     */
    @GuardedBy("this")
    private EntityState cleanupEntityState = EntityState.UNREGISTERED;

    /**
     * Optional event capture list. This field is initialized when we interact with entity ownership service, to capture
     * events reported during EOS method invocation -- like immediate acquisition of entity when we register it. This
     * prevents bugs from recursion.
     */
    @GuardedBy("this")
    private List<C> capture = null;

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
        members.addAll(services);

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

        if (!tryLock()) {
            // The lock is held, the cleanup will be finished by the owner thread
            LOG.debug("Singleton group {} cleanup postponed", identifier);
            return future;
        }

        try {
            lockedClose(future);
        } finally {
            unlock();
        }

        LOG.debug("Service group {} {}", identifier, future.isDone() ? "closed" : "closing");
        return future;
    }

    private boolean isClosed() {
        return closeFuture.get() != null;
    }

    @GuardedBy("this")
    private void startCapture() {
        verify(capture == null, "Service group {} is already capturing events {}", identifier, capture);
        capture = new ArrayList<>(0);
        LOG.debug("Service group {} started capturing events", identifier);
    }

    @GuardedBy("this")
    private GroupState endCapture() {
        final List<C> events = verifyNotNull(capture, "Service group {} is not currently capturing", identifier);
        capture = null;
        LOG.debug("Service group {} finished capturing events, {} events to process", identifier, events.size());

        GroupState ret = null;
        for (C change : events) {
            final GroupState newState = lockedOwnershipChanged(change);
            if (newState != null) {
                ret = newState;
                markDirty();
            }
        }
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
            endCapture();
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
            endCapture();
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
        verify(tryLock());
        try {
            checkState(serviceEntityState == EntityState.UNREGISTERED, "Singleton group %s was already initilized",
                    identifier);

            LOG.debug("Initializing service group {} with services {}", identifier, members);

            synchronized (this) {
                startCapture();
                serviceEntityReg = entityOwnershipService.registerCandidate(serviceEntity);
                serviceEntityState = EntityState.REGISTERED;
                endCapture();
            }
        } finally {
            unlock();
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

        // First put the service
        LOG.debug("Adding service {} to service group {}", service, identifier);
        verify(members.add(reg));
        dirty = 1;

        if (!tryLock()) {
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

        verify(members.remove(reg));
        dirty = 1;

        if (!tryLock()) {
            LOG.debug("Service group {} delayed unregister of {}", identifier, reg);
            return false;
        }

        reconcileState();
        return true;
    }

    @Override
    void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} for ClusterSingletonServiceGroup {}", ownershipChange, identifier);

        synchronized (this) {
            if (capture != null) {
                capture.add(ownershipChange);
                return;
            }

            final GroupState oldTarget = targetState;
            if (oldTarget != GroupState.DESTROYED) {
                final GroupState newTarget = lockedOwnershipChanged(ownershipChange);

                if (oldTarget != newTarget) {
                    targetState = newTarget;
                    markDirty();
                }
            }
        }

        if (isDirty()) {
            if (!tryLock()) {
                LOG.debug("Service group {} postponing ownership change sync");
                return;
            }

            reconcileState();
        }
    }

    /**
     * Handle an ownership change with the lock held. Callers are expected to handle termination conditions, this method
     * and anything it calls must not call {@link #lockedClose(SettableFuture)}.
     *
     * @param ownershipChange reported change
     * @return New target state
     */
    @GuardedBy("this")
    private GroupState lockedOwnershipChanged(final C ownershipChange) {
        final E entity = ownershipChange.getEntity();
        if (serviceEntity.equals(entity)) {
            return serviceOwnershipChanged(ownershipChange.getState(), ownershipChange.inJeopardy());
        } else if (cleanupEntity.equals(entity)) {
            return cleanupCandidateOwnershipChanged(ownershipChange.getState(), ownershipChange.inJeopardy());
        } else {
            LOG.warn("Group {} received unrecognized change {}", identifier, ownershipChange);
            return null;
        }
    }

    @GuardedBy("this")
    private GroupState cleanupCandidateOwnershipChanged(final EntityOwnershipChangeState state,
            final boolean jeopardy) {
        if (jeopardy) {
            switch (state) {
                case LOCAL_OWNERSHIP_GRANTED:
                case LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE:
                    if (cleanupEntityReg == null) {
                        LOG.debug("Service group {} ignoring cleanup entity ownership when unregistered", identifier);
                        return null;
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

            return GroupState.INACTIVE;
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
                    return null;
                }

                cleanupEntityState = EntityState.OWNED;
                return GroupState.ACTIVE;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_CHANGED:
                cleanupEntityState = EntityState.UNOWNED;
                return GroupState.INACTIVE;
            default:
                LOG.warn("Service group {} ignoring unhandled cleanup entity change {}", identifier, state);
                return null;
        }
    }

    @GuardedBy("this")
    private GroupState serviceOwnershipChanged(final EntityOwnershipChangeState state, final boolean jeopardy) {
        if (jeopardy) {
            LOG.info("Service group {} service entity ownership uncertain", identifier);

            // Service entity ownership is uncertain, which means we want to record the state, but we do not want
            // to stop local services nor do anything with the cleanup entity.
            switch (state) {
                case LOCAL_OWNERSHIP_GRANTED:
                case LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE:
                    if (serviceEntityReg == null) {
                        LOG.debug("Service group {} ignoring service entity ownership when unregistered", identifier);
                        return null;
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
            return null;
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
                    return null;
                }

                serviceEntityState = EntityState.OWNED;
                return takeOwnership();
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_CHANGED:
            case REMOTE_OWNERSHIP_LOST_NO_OWNER:
                LOG.debug("Service group {} lost service entity ownership", identifier);
                serviceEntityState = EntityState.UNOWNED;
                return GroupState.INACTIVE;
            default:
                LOG.warn("Service group {} ignoring unhandled cleanup entity change {}", identifier, state);
                return null;
        }
    }

    private void finishCloseIfNeeded() {
        final SettableFuture<Void> future = closeFuture.get();
        if (future != null) {
            lock.acquire();
            try {
                lockedClose(future);
            } finally {
                lock.release();
            }
        }
    }

    /*
     * Help method to registered DoubleCandidateEntity. It is first step before the actual instance take Leadership.
     */
    @GuardedBy("this")
    private GroupState takeOwnership() {
        if (isClosed()) {
            LOG.debug("Service group {} is closed, skipping cleanup ownership bid", identifier);
            return null;
        }

        LOG.debug("Service group {} registering cleanup entity", identifier);
        startCapture();
        try {
            cleanupEntityReg = entityOwnershipService.registerCandidate(cleanupEntity);
            cleanupEntityState = EntityState.REGISTERED;
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.error("Service group {} failed to take ownership", identifier, e);
        }

        return endCapture();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private boolean stopServices() {
        switch (localServicesState) {
            case STARTED:
                localServicesState = ServiceState.STOPPING;

                final List<ListenableFuture<?>> serviceCloseFutureList = new ArrayList<>(members.size());
                for (final ClusterSingletonServiceRegistration reg : members) {
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
        lock.acquire();
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
                        endCapture();
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
            lock.release();
            finishCloseIfNeeded();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
    }

    // has to be called with applyLock, which will be released prior to returning
    private void reconcileState() {
        // Always check if there is any state change to be applied.
        while (true) {
            if (conditionalClean()) {
                final Set<ClusterSingletonServiceRegistration> localConfig = ImmutableSet.copyOf(members);
                reconcileState(localConfig);
            }

            // We may have ran a round of reconciliation, but the either one of may have happened asynchronously:
            // - registration
            // - unregistration
            // - service future completed
            // - entity state changed
            //
            // We are dropping the lock, but we need to recheck dirty and try to apply state again if it is found to
            // be dirty again. This closes the following race condition:
            //
            // A: runs these checks holding the lock
            // B: modifies them, fails to acquire lock
            // A: releases lock -> noone takes care of reconciliation
            unlock();

            if (dirty != 0) {
                if (tryLock()) {
                    LOG.debug("Service group {} re-running reconciliation", identifier);
                    continue;
                }

                LOG.debug("Service group {} will be reconciled by someone else", identifier);
            } else {
                LOG.debug("Service group {} is completely reconciled", identifier);
            }

            break;
        }
    }

    private void serviceTransitionCompleted() {
        markDirty();
        if (tryLock()) {
            reconcileState();
        }
    }

    @GuardedBy("applyLock")
    private void reconcileState(final Set<ClusterSingletonServiceRegistration> localConfig) {
        // We are the sole thread observing entity state, now we need to reconcile all state
        final GroupState local = targetState;
        switch (local) {
            case ACTIVE:
                ensureServicesStarting(localConfig);
                break;
            case DESTROYED:
                ensureServicesStopping();
                if (services.isEmpty()) {
                    if (cleanupEntityReg != null) {
                        cleanupEntityReg.close();
                        cleanupEntityReg = null;
                    }

                    // FIXME: complete the future and ... anything else?
                }
                break;
            case INACTIVE:
                ensureServicesStopping();
                break;
            default:
                throw new IllegalStateException("Unhandled state " + local);
        }
    }

    @GuardedBy("applyLock")
    private void ensureServicesStarting(final Set<ClusterSingletonServiceRegistration> services) {
        LOG.debug("Service group {} starting services", identifier);
        // FIXME: implement this

//        members.forEach(reg -> {
//            final ClusterSingletonService service = reg.getInstance();
//            LOG.debug("Starting service {}", service);
//            try {
//                service.instantiateServiceInstance();
//            } catch (Exception e) {
//                LOG.warn("Service group {} service {} failed to start, attempting to continue", identifier, service, e);
//            }
//        });
    }

    @GuardedBy("applyLock")
    private void ensureServicesStopping() {
        final Iterator<Entry<ClusterSingletonServiceRegistration, ServiceInfo>> it = services.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ClusterSingletonServiceRegistration, ServiceInfo> entry = it.next();
            final ServiceInfo info = entry.getValue();

            switch (info.getState()) {
                case STARTED:
                    final ClusterSingletonService service = entry.getKey().getInstance();

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
                    Futures.addCallback(future, new FutureCallback<Object>() {
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
                    }, MoreExecutors.directExecutor());
                    break;
                case STOPPED:
                    it.remove();
                    LOG.debug("Service group {} removed stopped service {}", identifier, entry.getKey().getInstance());
                    break;
                case STOPPING:
                    if (info.getFuture().get().isDone()) {
                        it.remove();
                        LOG.debug("Service group {} removed stopped service {}", identifier,
                            entry.getKey().getInstance());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unhandles state " + info.getState());
            }
        }
    }

    private void markDirty() {
        dirty = 1;
    }

    private boolean isDirty() {
        return dirty != 0;
    }

    private boolean conditionalClean() {
        return DIRTY_UPDATER.compareAndSet(this, 1, 0);
    }

    private boolean tryLock() {
        return LOCK_UPDATER.compareAndSet(this, 0, 1);
    }

    private boolean unlock() {
        verify(LOCK_UPDATER.compareAndSet(this, 1, 0));
        return true;
    }
}
