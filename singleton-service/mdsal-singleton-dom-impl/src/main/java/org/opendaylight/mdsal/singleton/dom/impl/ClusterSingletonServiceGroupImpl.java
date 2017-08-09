/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ClusterSingletonServiceGroup} on top of the Entitiy Ownership Service. Since EOS is atomic
 * in its operation and singleton services incur startup and most notably cleanup, we need to do something smart here.
 *
 * The implementation takes advantage of the fact that EOS provides stable ownership, i.e. owners are not moved as
 * a result on new candidates appearing. We use two entities:
 *
 * - service entity, to which all nodes register
 * - cleanup entity, which only the service entity owner registers to
 *
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
        S extends GenericEntityOwnershipService<P, E, G>> implements ClusterSingletonServiceGroup<P, E, C>,
        Identifiable<String> {
    private enum State {
        /**
         * This group has been freshly allocated and has not been started yet.
         */
        INITIAL,
        /**
         * Operational state. Service entity is registered, but is not held by us.
         */
        REGISTERED,
        /**
         * Service entity acquired. Attempting to acquire cleanup entity.
         */
        TAKING_OWNERSHIP,
        /**
         * Both entities held and user services are being started.
         */
        STARTING_SERVICES,
        /**
         * Steady state. Both entities held and services have finisehd starting.
         */
        OWNER,
        /**
         * User services are being stopped due to either loss of an entity or a shutdown.
         */
        STOPPING_SERVICES,
        /**
         * Terminated, this group cannot be used anymore.
         */
        TERMINATED
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClusterSingletonServiceGroupImpl.class.getName());

    private final AbstractClusterSingletonServiceProviderImpl<P, E, C, G, ?, ?> parent;
    private final String identifier;
    private final S entityOwnershipService;

    /* Entity instances */
    private final E serviceEntity;
    private final E cleanupEntity;

    private final AtomicReference<SettableFuture<List<Void>>> closeFuture = new AtomicReference<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    @GuardedBy("lock")
    private State state = State.INITIAL;

    @GuardedBy("lock")
    private final List<ClusterSingletonServiceRegistrationDelegator> serviceGroup = new ArrayList<>();

    @GuardedBy("lock")
    private List<C> capture;

    /* EOS Candidate Registrations */
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityReg;

    @GuardedBy("lock")
    private GenericEntityOwnershipCandidateRegistration<P, E> cleanupEntityReg;

    /**
     * Class constructor.
     *
     * @param identifier not empty string as identifier
     * @param mainEntity as Entity instance
     * @param closeEntity as Entity instance
     * @param entityOwnershipService GenericEntityOwnershipService instance
     * @param allServiceGroups concurrentMap of String and ClusterSingletonServiceGroup type
     */
    ClusterSingletonServiceGroupImpl(final String identifier, final E mainEntity,
            final E closeEntity, final S entityOwnershipService,
            final AbstractClusterSingletonServiceProviderImpl<P, E, C, G, ?, ?> parent) {
        Preconditions.checkArgument(!identifier.isEmpty(), "Identifier may not be empty");
        this.identifier = identifier;
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.serviceEntity = Preconditions.checkNotNull(mainEntity);
        this.cleanupEntity = Preconditions.checkNotNull(closeEntity);
        this.parent = Preconditions.checkNotNull(parent);
        LOG.debug("Instantiated new service group for {}", identifier);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ListenableFuture<List<Void>> closeClusterSingletonGroup() {
        // Assert our future first
        final SettableFuture<List<Void>> future = SettableFuture.create();
        final SettableFuture<List<Void>> existing = closeFuture.getAndSet(future);
        if (existing != null) {
            return existing;
        }

        if (!lock.tryLock()) {
            // The lock is held, the cleanup will be finished by the owner thread
            LOG.debug("Singleton group {} cleanup postponed", identifier);
            return future;
        }

        try {
            lockedClose();
        } finally {
            lock.unlock();
        }

        LOG.debug("Service group {} {}", identifier, future.isDone() ? "closed" : "closing");
        return future;
    }

    @GuardedBy("lock")
    private void lockedClose() {
        switch (state) {
            case INITIAL:
                // Not started: not much to do
                state = State.TERMINATED;
                parent.onGroupClosed(this);
                break;
            case TERMINATED:
                // Already done: no-op
                break;
            case OWNER:
                stopServices();
                break;
            case STOPPING_SERVICES:
                // Waiting for services. Will resume once we get notified.
                break;
            case REGISTERED:
                LOG.debug("Service group {} unregistering", identifier);
                serviceEntityReg.close();
                state = State.TERMINATED;
                parent.onGroupClosed(this);
                break;
            case TAKING_OWNERSHIP:
                // Abort taking of ownership and close
                LOG.debug("Service group {} aborting ownership bid", identifier);
                cleanupEntityReg.close();
                serviceEntityReg.close();
                state = State.TERMINATED;
                parent.onGroupClosed(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void initializationClusterSingletonGroup() throws CandidateAlreadyRegisteredException {
        LOG.debug("Initialization ClusterSingletonGroup {}", identifier);

        lock.lock();
        try {
            Preconditions.checkState(state == State.INITIAL, "Unexpected singleton group %s state %s", identifier,
                    state);

            // Catch events if they fire during this call
            capture = new ArrayList<>(0);
            serviceEntityReg = entityOwnershipService.registerCandidate(serviceEntity);
            state = State.REGISTERED;

            final List<C> captured = capture;
            capture = null;
            captured.forEach(this::lockedOwnershipChanged);
        } finally {
            lock.unlock();
        }
    }

    private void checkNotClosed() {
        Preconditions.checkState(closeFuture.get() == null, "Service group %s has already been closed",
                identifier);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public ClusterSingletonServiceRegistration registerService(final ClusterSingletonService service) {
        Verify.verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        LOG.debug("RegisterService method call for ClusterSingletonServiceGroup {}", identifier);

        lock.lock();
        try {
            Preconditions.checkState(state != State.INITIAL, "Service group %s is not initialized yet", identifier);
            final ClusterSingletonServiceRegistrationDelegator delegator =
                    new ClusterSingletonServiceRegistrationDelegator(service, this);
            serviceGroup.add(delegator);

            switch (state) {
                case OWNER:
                case STARTING_SERVICES:
                    delegator.instantiateServiceInstance();
                default:
                    break;
            }

            return delegator;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void unregisterService(final ClusterSingletonService service) {
        Verify.verify(identifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        lock.lock();
        try {
            Verify.verify(serviceGroup.remove(service));
            LOG.debug("Service {} was removed from group.", service.getIdentifier().getValue());

            switch (state) {
                case OWNER:
                case STARTING_SERVICES:
                    service.closeServiceInstance();
                    break;
                default:
                    break;
            }

            if (serviceGroup.isEmpty()) {
                // This was the last service to unregister, also shut down the entire group.
                if (closeFuture.compareAndSet(null, SettableFuture.create())) {
                    lockedClose();
                }
            }
        } finally {
            lock.unlock();
            finishCloseIfNeeded();
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} flor ClusterSingletonServiceGroup {}", ownershipChange, identifier);

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

    private void lockedOwnershipChanged(final C ownershipChange) {
        if (ownershipChange.inJeopardy()) {
            LOG.warn("Cluster Node lost connection to another cluster nodes {}", ownershipChange);
            lostOwnership();
            return;
        }

        final E entity = ownershipChange.getEntity();
        if (serviceEntity.equals(entity)) {
            serviceOwnershipChanged(ownershipChange);
        } else if (cleanupEntity.equals(entity)) {
            cleanupCandidateOwnershipChanged(ownershipChange);
        } else {
            LOG.warn("Group {} received unrecognized change {}", identifier, ownershipChange);
        }
    }

    private void cleanupCandidateOwnershipChanged(final C ownershipChange) {
        if (ownershipChange.getState() == EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED
                && state == State.TAKING_OWNERSHIP) {
            // SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()
            startServices();
            return;
        }

        // Not needed notifications
        LOG.debug("Not processed doubleCandidate OwnershipChange {} in service Provider {}", ownershipChange,
            identifier);
    }

    private void serviceOwnershipChanged(final C ownershipChange) {
        switch (ownershipChange.getState()) {
            case LOCAL_OWNERSHIP_GRANTED:
                // SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()
                takeOwnership();
                break;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
                // MASTER to SLAVE : !ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner()
                lostOwnership();
                break;
            default:
                // Not needed notifications
                LOG.debug("Not processed entity OwnershipChange {} in service Provider {}", ownershipChange,
                        identifier);
        }
    }

    private void finishCloseIfNeeded() {
        final SettableFuture<List<Void>> future = closeFuture.get();
        if (future != null) {
            lock.lock();
            try {
                lockedClose();
            } finally {
                lock.unlock();
            }
        }
    }

    private boolean isClosed() {
        return closeFuture.get() != null;
    }

    /*
     * Help method to registered DoubleCandidateEntity. It is first step
     * before the actual instance take Leadership.
     */
    private void takeOwnership() {
        if (isClosed()) {
            LOG.debug("Service group {} is closed, not taking ownership", identifier);
            return;
        }

        LOG.debug("Group {} taking ownership", identifier);


        state = State.TAKING_OWNERSHIP;
        try {
            cleanupEntityReg = entityOwnershipService.registerCandidate(cleanupEntity);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.error("Service group {} failed to take ownership", identifier, e);
        }
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


        LOG.debug("Service group {} services started", identifier);
        state = State.OWNER;
    }

    /*
     * Help method calls suspendService method for stop this single cluster-wide service instance.
     * The last async. step has to close DoubleCandidateRegistration reference what should initialize
     * new election for DoubleCandidateEntity.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void lostOwnership() {
        if (isClosed()) {
            LOG.debug("Service group {} is closed, ignoring ownership loss", identifier);
            return;
        }

        switch (state) {
            case INITIAL:
            case REGISTERED:
            case TERMINATED:
                LOG.info("Service group {} ignoring lost ownership in state {},", identifier, state);
                return;
            case OWNER:
                stopServices();
                return;
            case STARTING_SERVICES:
            case STOPPING_SERVICES:
                // No-op, as these will re-check state before proceeding
                break;
            case TAKING_OWNERSHIP:
                cleanupEntityReg.close();
                cleanupEntityReg = null;
                break;
            default:
                break;
        }

        state = State.REGISTERED;
    }

    void stopServices() {
        state = State.STOPPING_SERVICES;

        final List<ListenableFuture<Void>> serviceCloseFutureList = new ArrayList<>(serviceGroup.size());
        for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
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
        });
    }

    void onServicesStopped() {
        LOG.debug("Service group {} finished stopping services", identifier);
        lock.lock();
        try {
            if (cleanupEntityReg != null) {
                cleanupEntityReg.close();
                cleanupEntityReg = null;
            }

            state = State.REGISTERED;
        } finally {
            lock.unlock();
            finishCloseIfNeeded();
        }
    }
}
