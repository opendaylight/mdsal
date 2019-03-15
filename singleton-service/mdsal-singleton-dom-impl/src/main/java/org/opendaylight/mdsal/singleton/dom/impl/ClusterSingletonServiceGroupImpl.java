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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
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
 * Implementation of {@link ClusterSingletonServiceGroup} on top of the Entity Ownership Service. Since EOS is atomic
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
         * Local service is up and running.
         */
        // FIXME: we should support async startup, which will require a STARTING state.
        STARTED,
        /**
         * Local service is being stopped.
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

    // Marker for when any state changed
    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<ClusterSingletonServiceGroupImpl> DIRTY_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ClusterSingletonServiceGroupImpl.class, "dirty");
    private volatile int dirty;

    // Simplified lock: non-reentrant, support tryLock() only
    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<ClusterSingletonServiceGroupImpl> LOCK_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ClusterSingletonServiceGroupImpl.class, "lock");
    @SuppressWarnings("unused")
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

    private volatile boolean initialized;

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
        final ListenableFuture<?> ret = destroyGroup();
        members.clear();
        markDirty();

        if (tryLock()) {
            reconcileState();
        } else {
            LOG.debug("Service group {} postponing sync on close", identifier);
        }

        return ret;
    }

    private boolean isClosed() {
        return closeFuture.get() != null;
    }

    @Override
    void initialize() throws CandidateAlreadyRegisteredException {
        verify(tryLock());
        try {
            checkState(!initialized, "Singleton group %s was already initilized", identifier);
            LOG.debug("Initializing service group {} with services {}", identifier, members);
            synchronized (this) {
                serviceEntityState = EntityState.REGISTERED;
                serviceEntityReg = entityOwnershipService.registerCandidate(serviceEntity);
                initialized = true;
            }
        } finally {
            unlock();
        }
    }

    private void checkNotClosed() {
        checkState(!isClosed(), "Service group %s has already been closed", identifier);
    }

    @Override
    void registerService(final ClusterSingletonServiceRegistration reg) {
        final ClusterSingletonService service = reg.getInstance();
        verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        checkState(initialized, "Service group %s is not initialized yet", identifier);

        // First put the service
        LOG.debug("Adding service {} to service group {}", service, identifier);
        verify(members.add(reg));
        markDirty();

        if (!tryLock()) {
            LOG.debug("Service group {} delayed register of {}", identifier, reg);
            return;
        }

        reconcileState();
    }

    @Override
    ListenableFuture<?> unregisterService(final ClusterSingletonServiceRegistration reg) {
        final ClusterSingletonService service = reg.getInstance();
        verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        verify(members.remove(reg));
        markDirty();
        if (members.isEmpty()) {
            // We need to let AbstractClusterSingletonServiceProviderImpl know this group is to be shutdown
            // before we start applying state, because while we do not re-enter, the user is free to do whatever,
            // notably including registering a service with the same ID from the service shutdown hook. That
            // registration request needs to hit the successor of this group.
            return destroyGroup();
        }

        if (tryLock()) {
            reconcileState();
        } else {
            LOG.debug("Service group {} delayed unregister of {}", identifier, reg);
        }
        return null;
    }

    private synchronized @NonNull ListenableFuture<?> destroyGroup() {
        final SettableFuture<Void> future = SettableFuture.create();
        if (!closeFuture.compareAndSet(null, future)) {
            return verifyNotNull(closeFuture.get());
        }

        if (serviceEntityReg != null) {
            // We are still holding the service registration, close it now...
            LOG.debug("Service group {} unregistering service entity {}", identifier, serviceEntity);
            serviceEntityReg.close();
            serviceEntityReg = null;
        }

        markDirty();
        return future;
    }

    @Override
    void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} for ClusterSingletonServiceGroup {}", ownershipChange, identifier);

        synchronized (this) {
            lockedOwnershipChanged(ownershipChange);
        }

        if (isDirty()) {
            if (!tryLock()) {
                LOG.debug("Service group {} postponing ownership change sync", identifier);
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
     */
    @Holding("this")
    private void lockedOwnershipChanged(final C ownershipChange) {
        final E entity = ownershipChange.getEntity();
        if (serviceEntity.equals(entity)) {
            serviceOwnershipChanged(ownershipChange.getState(), ownershipChange.inJeopardy());
            markDirty();
        } else if (cleanupEntity.equals(entity)) {
            cleanupCandidateOwnershipChanged(ownershipChange.getState(), ownershipChange.inJeopardy());
            markDirty();
        } else {
            LOG.warn("Group {} received unrecognized change {}", identifier, ownershipChange);
        }
    }

    @Holding("this")
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
                break;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_CHANGED:
                cleanupEntityState = EntityState.UNOWNED;
                break;
            default:
                LOG.warn("Service group {} ignoring unhandled cleanup entity change {}", identifier, state);
                break;
        }
    }

    @Holding("this")
    private void serviceOwnershipChanged(final EntityOwnershipChangeState state, final boolean jeopardy) {
        if (jeopardy) {
            LOG.info("Service group {} service entity ownership uncertain", identifier);
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
                LOG.debug("Service group {} acquired service entity ownership", identifier);
                serviceEntityState = EntityState.OWNED;
                break;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
            case REMOTE_OWNERSHIP_CHANGED:
            case REMOTE_OWNERSHIP_LOST_NO_OWNER:
                LOG.debug("Service group {} lost service entity ownership", identifier);
                serviceEntityState = EntityState.UNOWNED;
                break;
            default:
                LOG.warn("Service group {} ignoring unhandled cleanup entity change {}", identifier, state);
        }
    }

    // has to be called with lock asserted, which will be released prior to returning
    private void reconcileState() {
        // Always check if there is any state change to be applied.
        while (true) {
            try {
                if (conditionalClean()) {
                    tryReconcileState();
                }
            } finally {
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
            }

            if (isDirty()) {
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

    // Has to be called with lock asserted
    private void tryReconcileState() {
        // First take a safe snapshot of current state on which we will base our decisions.
        final Set<ClusterSingletonServiceRegistration> localMembers;
        final boolean haveCleanup;
        final boolean haveService;
        synchronized (this) {
            if (serviceEntityReg != null) {
                switch (serviceEntityState) {
                    case OWNED:
                    case OWNED_JEOPARDY:
                        haveService = true;
                        break;
                    case REGISTERED:
                    case UNOWNED:
                    case UNREGISTERED:
                        haveService = false;
                        break;
                    default:
                        throw new IllegalStateException("Unhandled service entity state " + serviceEntityState);
                }
            } else {
                haveService = false;
            }

            if (haveService && cleanupEntityReg == null) {
                // We have the service entity but have not registered for cleanup entity. Do that now and retry.
                LOG.debug("Service group {} registering cleanup entity", identifier);
                try {
                    cleanupEntityState = EntityState.REGISTERED;
                    cleanupEntityReg = entityOwnershipService.registerCandidate(cleanupEntity);
                } catch (CandidateAlreadyRegisteredException e) {
                    LOG.error("Service group {} failed to take ownership, aborting", identifier, e);
                    if (serviceEntityReg != null) {
                        serviceEntityReg.close();
                        serviceEntityReg = null;
                    }
                }
                markDirty();
                return;
            }

            if (cleanupEntityReg != null) {
                switch (cleanupEntityState) {
                    case OWNED:
                        haveCleanup = true;
                        break;
                    case OWNED_JEOPARDY:
                    case REGISTERED:
                    case UNOWNED:
                    case UNREGISTERED:
                        haveCleanup = false;
                        break;
                    default:
                        throw new IllegalStateException("Unhandled service entity state " + serviceEntityState);
                }
            } else {
                haveCleanup = false;
            }

            localMembers = ImmutableSet.copyOf(members);
        }

        if (haveService && haveCleanup) {
            ensureServicesStarting(localMembers);
            return;
        }

        ensureServicesStopping();

        if (!haveService && services.isEmpty()) {
            LOG.debug("Service group {} has no running services", identifier);
            final boolean canFinishClose;
            synchronized (this) {
                if (cleanupEntityReg != null) {
                    LOG.debug("Service group {} releasing cleanup entity", identifier);
                    cleanupEntityReg.close();
                    cleanupEntityReg = null;
                }

                switch (cleanupEntityState) {
                    case OWNED:
                    case OWNED_JEOPARDY:
                    case REGISTERED:
                        // When we are registered we need to wait for registration to resolve, otherwise
                        // the notification could be routed to the next incarnation of this group -- which could be
                        // confused by the fact it is not registered, but receives, for example, OWNED notification.
                        canFinishClose = false;
                        break;
                    case UNOWNED:
                    case UNREGISTERED:
                        canFinishClose = true;
                        break;
                    default:
                        throw new IllegalStateException("Unhandled cleanup entity state " + cleanupEntityState);
                }
            }

            if (canFinishClose) {
                final SettableFuture<Void> localFuture = closeFuture.get();
                if (localFuture != null && !localFuture.isDone()) {
                    LOG.debug("Service group {} completing termination", identifier);
                    localFuture.set(null);
                }
            }
        }
    }

    // Has to be called with lock asserted
    @SuppressWarnings("illegalCatch")
    private void ensureServicesStarting(final Set<ClusterSingletonServiceRegistration> localConfig) {
        LOG.debug("Service group {} starting services", identifier);

        // This may look counter-intuitive, but the localConfig may be missing some services that are started -- for
        // example when this method is executed as part of unregisterService() call. In that case we need to ensure
        // services in the list are stopping
        final Iterator<Entry<ClusterSingletonServiceRegistration, ServiceInfo>> it = services.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ClusterSingletonServiceRegistration, ServiceInfo> entry = it.next();
            final ClusterSingletonServiceRegistration reg = entry.getKey();
            if (!localConfig.contains(reg)) {
                final ServiceInfo newInfo = ensureStopping(reg, entry.getValue());
                if (newInfo != null) {
                    entry.setValue(newInfo);
                } else {
                    it.remove();
                }
            }
        }

        // Now make sure member services are being juggled around
        for (ClusterSingletonServiceRegistration reg : localConfig) {
            if (!services.containsKey(reg)) {
                final ClusterSingletonService service = reg.getInstance();
                LOG.debug("Starting service {}", service);

                try {
                    service.instantiateServiceInstance();
                } catch (Exception e) {
                    LOG.warn("Service group {} service {} failed to start, attempting to continue", identifier, service,
                        e);
                    continue;
                }

                services.put(reg, ServiceInfo.started());
            }
        }
    }

    // Has to be called with lock asserted
    private void ensureServicesStopping() {
        final Iterator<Entry<ClusterSingletonServiceRegistration, ServiceInfo>> it = services.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ClusterSingletonServiceRegistration, ServiceInfo> entry = it.next();
            final ServiceInfo newInfo = ensureStopping(entry.getKey(), entry.getValue());
            if (newInfo != null) {
                entry.setValue(newInfo);
            } else {
                it.remove();
            }
        }
    }

    @SuppressWarnings("illegalCatch")
    private ServiceInfo ensureStopping(final ClusterSingletonServiceRegistration reg, final ServiceInfo info) {
        switch (info.getState()) {
            case STARTED:
                final ClusterSingletonService service = reg.getInstance();

                LOG.debug("Service group {} stopping service {}", identifier, service);
                final @NonNull ListenableFuture<?> future;
                try {
                    future = verifyNotNull(service.closeServiceInstance());
                } catch (Exception e) {
                    LOG.warn("Service group {} service {} failed to stop, attempting to continue", identifier, service,
                        e);
                    return null;
                }

                Futures.addCallback(future, new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(final Object result) {
                        LOG.debug("Service group {} service {} stopped successfully", identifier, service);
                        serviceTransitionCompleted();
                    }

                    @Override
                    public void onFailure(final Throwable cause) {
                        LOG.debug("Service group {} service {} stopped with error", identifier, service, cause);
                        serviceTransitionCompleted();
                    }
                }, MoreExecutors.directExecutor());
                return info.toState(ServiceState.STOPPING, future);
            case STOPPING:
                if (info.getFuture().isDone()) {
                    LOG.debug("Service group {} removed stopped service {}", identifier, reg.getInstance());
                    return null;
                }
                return info;
            default:
                throw new IllegalStateException("Unhandled state " + info.getState());
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
    }
}
