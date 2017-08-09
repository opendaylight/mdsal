/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
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
 * Implementation of {@link ClusterSingletonServiceGroup}.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 */
final class ClusterSingletonServiceGroupImpl<P extends Path<P>, E extends GenericEntity<P>,
        C extends GenericEntityOwnershipChange<P, E>,  G extends GenericEntityOwnershipListener<P, C>,
        S extends GenericEntityOwnershipService<P, E, G>> implements ClusterSingletonServiceGroup<P, E, C> {
    // TODO :it needs to rewrite for StateMachine (INITIALIZED, TRY_TO_TAKE_LEADERSHIP, LEADER, FOLLOWER, TERMINATED)
    // INITIALIZED : we have registered baseCandidate and we are waiting for first EOS response (!do we really need it?)
    // FOLLOWER : baseCandidate is registered correctly
    // TRY_TO_TAKE_LEADERSHIP : guardCandidate is registered correctly
    // LEADER : both candidate have mastership from EOS
    // TERMINATED : service go down
    // Abstract base state holder
    private abstract static class State {

        abstract Closed close();
    }

    // Initial, fresh state, needs to be initialized
    private static final class Initial extends State {
        @Override
        public Closed close() {
            return new Closed();
        }
    }

    private static final class Registering extends State {
        @Override
        Closed close() {
            // This should never happen
            throw new IllegalStateException("Attempted to close group while it is registering");
        }
    }

    // Initializing, current ownership status has not been ascertained
    private static final class Registered extends State {

        private final GenericEntityOwnershipCandidateRegistration<?, ?> registration;

        Registered(final GenericEntityOwnershipCandidateRegistration<?, ?> registration) {
            this.registration = Preconditions.checkNotNull(registration);
        }

        @Override
        Closed close() {
            registration.close();
            return new Closed();
        }
    }

    // We are the group owner
    private static final class Owner extends State {

        @Override
        Closed close() {
            // We are the owner. Relinquish the candidate registration and notify the user

            // TODO Auto-generated method stub
            return null;
        }
    }

    // We are not the owner
    private static final class Standby extends State {

        @Override
        Closed close() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    // We are becoming the new owner
    private static final class Acquiring extends State {

        @Override
        Closed close() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    // Terminated state
    private static final class Closed extends State {
        @Override
        public Closed close() {
            // No-op
            return this;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClusterSingletonServiceGroupImpl.class.getName());

    private final Semaphore clusterLock = new Semaphore(1, true);
    private final String clusterSingletonGroupIdentifier;
    private final S entityOwnershipService;

    /* Entity instances */
    private final E serviceEntity;
    private final E doubleCandidateEntity;

    private final AtomicReference<SettableFuture<List<Void>>> closeFuture = new AtomicReference<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    @GuardedBy("lock")
    private State state = new Initial();




    @GuardedBy("clusterLock")
    private boolean hasOwnership = false;
    @GuardedBy("clusterLock")
    private final List<ClusterSingletonServiceRegistrationDelegator> serviceGroup = new LinkedList<>();
    private final ConcurrentMap<String, ClusterSingletonServiceGroup<P, E, C>> allServiceGroups;

    /* EOS Candidate Registrations */
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityCandidateReg;
    private GenericEntityOwnershipCandidateRegistration<P, E> asyncCloseEntityCandidateReg;

    /**
     * Class constructor.
     *
     * @param clusterSingletonServiceGroupIdentifier not empty string as identifier
     * @param mainEntity as Entity instance
     * @param closeEntity as Entity instance
     * @param entityOwnershipService GenericEntityOwnershipService instance
     * @param allServiceGroups concurrentMap of String and ClusterSingletonServiceGroup type
     */
    ClusterSingletonServiceGroupImpl(final String clusterSingletonServiceGroupIdentifier, final E mainEntity,
            final E closeEntity, final S entityOwnershipService,
            final ConcurrentMap<String, ClusterSingletonServiceGroup<P, E, C>> allServiceGroups) {
        LOG.debug("New Instance of ClusterSingletonServiceGroup {}", clusterSingletonServiceGroupIdentifier);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterSingletonServiceGroupIdentifier));
        this.clusterSingletonGroupIdentifier = clusterSingletonServiceGroupIdentifier;
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.serviceEntity = Preconditions.checkNotNull(mainEntity);
        this.doubleCandidateEntity = Preconditions.checkNotNull(closeEntity);
        this.allServiceGroups = Preconditions.checkNotNull(allServiceGroups);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public ListenableFuture<List<Void>> closeClusterSingletonGroup() {
        LOG.debug("Close method for service Provider {}", clusterSingletonGroupIdentifier);

        // Assert our future first
        final SettableFuture<List<Void>> future = SettableFuture.create();
        final boolean asserted = closeFuture.compareAndSet(null, future);
        Preconditions.checkState(asserted, "Singleton group %s has already been closed",
            clusterSingletonGroupIdentifier);

        if (!lock.tryLock()) {
            // The lock is held, the cleanup will be finished by the owner thread
            LOG.debug("Singleton group {} cleanup postponed", clusterSingletonGroupIdentifier);
            return future;
        }

        try {
            state = state.close();
        } finally {
            lock.unlock();
        }

        return future;

//            if (serviceEntityCandidateReg != null) {
//                serviceEntityCandidateReg.close();
//                serviceEntityCandidateReg = null;
//            }
//            final List<ListenableFuture<Void>> serviceCloseFutureList = new ArrayList<>();
//            if (hasOwnership) {
//                for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
//                    try {
//                        serviceCloseFutureList.add(service.closeServiceInstance());
//                    } catch (final RuntimeException e) {
//                        LOG.warn("Unexpected exception while closing service: {}, resuming with next..",
//                                service.getIdentifier());
//                    }
//                }
//                hasOwnership = false;
//            }
//            destroyFuture = Futures.allAsList(serviceCloseFutureList);
//            final Semaphore finalRelease = needReleaseLock ? clusterLock : null;
//            Futures.addCallback(destroyFuture, newAsyncCloseCallback(finalRelease, true));
//        }
//        return destroyFuture;
    }

    @Override
    public void initializationClusterSingletonGroup() throws CandidateAlreadyRegisteredException {
        LOG.debug("Initialization ClusterSingletonGroup {}", clusterSingletonGroupIdentifier);

        lock.lock();
        try {
            Preconditions.checkState(state instanceof Initial, "Unexpected singleton group %s state %s",
                clusterSingletonGroupIdentifier, state);

            // Transitive state in case the registration fires in our thread
            state = new Registering();
            final GenericEntityOwnershipCandidateRegistration<P, E> registration =
                    entityOwnershipService.registerCandidate(serviceEntity);

            // FIXME: check if the registration already fired and transition accordingly

            state = new Registered(registration);
        } finally {
            lock.unlock();
        }
    }

    private void checkNotClosed() {
        Preconditions.checkState(closeFuture.get() == null, "Service group %s has already been closed",
                clusterSingletonGroupIdentifier);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public ClusterSingletonServiceRegistration registerService(final ClusterSingletonService service) {
        Verify.verify(clusterSingletonGroupIdentifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        LOG.debug("RegisterService method call for ClusterSingletonServiceGroup {}", clusterSingletonGroupIdentifier);

        lock.lock();
        try {
//            Verify.verify(serviceEntityCandidateReg != null);
            final ClusterSingletonServiceRegistrationDelegator delegator =
                    new ClusterSingletonServiceRegistrationDelegator(service, this);
            serviceGroup.add(delegator);
            if (state instanceof Owner) {
                delegator.instantiateServiceInstance();
            }
            return delegator;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void unregisterService(final ClusterSingletonService service) {
        Verify.verify(clusterSingletonGroupIdentifier.equals(service.getIdentifier().getValue()));
        checkNotClosed();

        final boolean needClose;
        lock.lock();
        try {
            Verify.verify(serviceGroup.remove(service));
            LOG.debug("Service {} was removed from group.", service.getIdentifier().getValue());
            needClose = state instanceof Owner;
        } finally {
            lock.unlock();
        }

        if (needClose) {
            service.closeServiceInstance();
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} flor ClusterSingletonServiceGroup {}", ownershipChange,
                clusterSingletonGroupIdentifier);

        final E entity = ownershipChange.getEntity();
        if (serviceEntity.equals(entity)) {
            serviceOwnershipChanged(ownershipChange);
        } else if (doubleCandidateEntity.equals(entity)) {
            doubleCandidateOwnershipChanged(ownershipChange);
        } else {
            LOG.warn("Group {} received unrecognized change {}", clusterSingletonGroupIdentifier, ownershipChange);
        }

//        if (ownershipChange.inJeopardy()) {
//            LOG.warn("Cluster Node lost connection to another cluster nodes {}", ownershipChange);
//            lostOwnership();
//            return;
//        }
    }

    private void doubleCandidateOwnershipChanged(final C ownershipChange) {
        switch (ownershipChange.getState()) {
            case LOCAL_OWNERSHIP_GRANTED:
                // SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()
                takeOwnership();
                break;
            default:
                // Not needed notifications
                LOG.debug("Not processed doubleCandidate OwnershipChange {} in service Provider {}", ownershipChange,
                    clusterSingletonGroupIdentifier);
        }
    }

    private void serviceOwnershipChanged(final C ownershipChange) {
        switch (ownershipChange.getState()) {
            case LOCAL_OWNERSHIP_GRANTED:
                // SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()
                tryToTakeOwnership();
                break;
            case LOCAL_OWNERSHIP_LOST_NEW_OWNER:
            case LOCAL_OWNERSHIP_LOST_NO_OWNER:
                // MASTER to SLAVE : !ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner()
                lostOwnership();
                break;
            default:
                // Not needed notifications
                LOG.debug("Not processed entity OwnershipChange {} in service Provider {}", ownershipChange,
                        clusterSingletonGroupIdentifier);
        }
    }

    /*
     * Help method to registered DoubleCandidateEntity. It is first step
     * before the actual instance take Leadership.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void tryToTakeOwnership() {
        LOG.debug("TryToTakeLeadership method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            if (serviceEntityCandidateReg != null) {
                Verify.verify(asyncCloseEntityCandidateReg == null);
                asyncCloseEntityCandidateReg = entityOwnershipService.registerCandidate(doubleCandidateEntity);
            } else {
                LOG.debug("Service {} is closed, so don't to tryTakeLeadership", clusterSingletonGroupIdentifier);
            }
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for service Provider {} in TryToTakeLeadership",
                    clusterSingletonGroupIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls setupService method for create single cluster-wide service instance.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void takeOwnership() {
        LOG.debug("TakeLeadership method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            if (serviceEntityCandidateReg != null) {
                Verify.verify(asyncCloseEntityCandidateReg != null);
                for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
                    service.instantiateServiceInstance();
                }
                hasOwnership = true;
            } else {
                LOG.debug("Service {} is closed, so don't take leadership", clusterSingletonGroupIdentifier);
            }
        } catch (final RuntimeException | InterruptedException e) {
            LOG.error("Unexpected exception state for service Provider {} in TakeLeadership",
                    clusterSingletonGroupIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls suspendService method for stop this single cluster-wide service instance.
     * The last async. step has to close DoubleCandidateRegistration reference what should initialize
     * new election for DoubleCandidateEntity.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void lostOwnership() {
        LOG.debug("LostLeadership method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            final List<ListenableFuture<Void>> serviceCloseFutureList = new ArrayList<>();
            if (hasOwnership) {
                Verify.verify(asyncCloseEntityCandidateReg != null);
                for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
                    try {
                        serviceCloseFutureList.add(service.closeServiceInstance());
                    } catch (final RuntimeException e) {
                        LOG.error("Unexpected exception while closing service: {}, resuming with next..",
                                service.getIdentifier());
                    }
                }
                hasOwnership = false;
            }

            final ListenableFuture<List<Void>> destroyFuture = Futures.allAsList(serviceCloseFutureList);
            if (serviceEntityCandidateReg != null) {
                // we don't want to remove this instance from map
                Futures.addCallback(destroyFuture, newAsyncCloseCallback(clusterLock, false));
            } else {
                // we have to remove this ClusterSingletonServiceGroup instance from map
                Futures.addCallback(destroyFuture, newAsyncCloseCallback(clusterLock, true));
            }
            /*
             * We wish to stop all possible EOS activities before we don't close
             * a close candidate registration that acts as a guard. So we don't want
             * to release Semaphore (clusterLock) before we are not fully finished.
             * Semaphore lock release has to be realized as FutureCallback after a service
             * instance has fully closed prior to relinquishing service ownership.
             */
            needReleaseLock = false;
        } catch (final InterruptedException e) {
            LOG.error("Unexpected exception state for service Provider {} in LostLeadership",
                    clusterSingletonGroupIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method for finalization every acquired functionality
     */
    @GuardedBy("clusterLock")
    private void closeResources(final boolean needReleaseLock, final boolean needCloseProvider) {
        if (needCloseProvider) {
            // The Game Over for this ClusterSingletonServiceGroup instance
            if (serviceEntityCandidateReg != null) {
                serviceEntityCandidateReg.close();
                serviceEntityCandidateReg = null;
            }
            // Remove instance immediately because actual state is follower or initialization
            if (asyncCloseEntityCandidateReg == null) {
                allServiceGroups.remove(clusterSingletonGroupIdentifier, this);
            }
        }

        if (needReleaseLock) {
            clusterLock.release();
        }
    }

    /*
     * Help method creates FutureCallback for suspend Future
     */
    private FutureCallback<List<Void>> newAsyncCloseCallback(@Nullable final Semaphore semaphore,
            final boolean isInCloseProcess) {
        final Consumer<Throwable> closeEntityCandidateRegistration = (@Nullable final Throwable throwable) -> {
            if (throwable != null) {
                LOG.warn("Unexpected error closing service instance {}", clusterSingletonGroupIdentifier, throwable);
            } else {
                LOG.debug("Destroy service Instance {} is success", clusterSingletonGroupIdentifier);
            }
            if (asyncCloseEntityCandidateReg != null) {
                asyncCloseEntityCandidateReg.close();
                asyncCloseEntityCandidateReg = null;
            }
            if (isInCloseProcess) {
                allServiceGroups.remove(clusterSingletonGroupIdentifier, this);
            }
            if (semaphore != null) {
                semaphore.release();
            }
        };

        return new FutureCallback<List<Void>>() {

            @Override
            public void onSuccess(final List<Void> result) {
                closeEntityCandidateRegistration.accept(null);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                closeEntityCandidateRegistration.accept(throwable);
            }
        };
    }
}
