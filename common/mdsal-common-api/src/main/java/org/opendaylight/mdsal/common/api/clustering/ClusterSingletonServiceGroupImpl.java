/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ClusterSingletonServiceGroup}
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 */
final class ClusterSingletonServiceGroupImpl<P extends Path<P>, E extends GenericEntity<P>,
                                             C extends GenericEntityOwnershipChange<P, E>,
                                             G extends GenericEntityOwnershipListener<P, C>,
                                             S extends GenericEntityOwnershipService<P, E, G>>
        implements ClusterSingletonServiceGroup<P, E, C> {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterSingletonServiceGroupImpl.class.getName());

    private final S entityOwnershipService;
    private final String clusterSingletonGroupIdentifier;
    private final Semaphore clusterLock = new Semaphore(1);

    /* Entity instances */
    private final E serviceEntity;
    private final E doubleCandidateEntity;

    @GuardedBy("clusterLock")
    private boolean hasOwnership = false;
    @GuardedBy("clusterLock")
    private final List<ClusterSingletonServiceRegistrationDelegator> serviceGroup = new LinkedList<>();
    private final ConcurrentMap<String, ClusterSingletonServiceGroup<P, E, C>> allServiceGroups;

    /* EOS Candidate Registrations */
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityCandidateReg;
    private GenericEntityOwnershipCandidateRegistration<P, E> asyncCloseEntityCandidateReg;

    /**
     * Class constructor
     *
     * @param clusterSingletonServiceGroupIdentifier not empty string as identifier
     * @param mainEntity
     * @param closeEntity
     * @param entityOwnershipService
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

    @Override
    public final ListenableFuture<List<Void>> closingClusterSingletonGroup() {
        LOG.debug("Close method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        final ListenableFuture<List<Void>> destroyFuture;
        try {
            needReleaseLock = clusterLock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOG.warn("Unexpected Exception for service Provider {} in closing phase.", clusterSingletonGroupIdentifier, e);
        } finally {
            if (serviceEntityCandidateReg != null) {
                serviceEntityCandidateReg.close();
                serviceEntityCandidateReg = null;
            }
            final List<ListenableFuture<Void>> serviceCloseFutureList = new ArrayList<>();
            if (hasOwnership) {
                for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
                    serviceCloseFutureList.add(service.closeServiceInstance());
                }
            }
            destroyFuture = Futures.allAsList(serviceCloseFutureList);
            final Semaphore finalRelease = needReleaseLock ? clusterLock : null;
            Futures.addCallback(destroyFuture, newAsyncCloseCallback(finalRelease));
        }
        return destroyFuture;
    }

    @Override
    public final void initializationClusterSingletonGroup() {
        LOG.debug("Initialization ClusterSingletonGroup {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            Verify.verify(serviceGroup.isEmpty());
            Verify.verify(!hasOwnership);
            Verify.verify(serviceEntityCandidateReg == null);
            serviceEntityCandidateReg = entityOwnershipService.registerCandidate(serviceEntity);
        } catch (final Exception e) {
            LOG.debug("Unexpected error by registration service Provider {}", clusterSingletonGroupIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    @Override
    public final ClusterSingletonServiceRegistration registerService(final ClusterSingletonService service) {
        LOG.debug("RegisterService method call for ClusterSingletonServiceGroup {}", clusterSingletonGroupIdentifier);
        Verify.verify(clusterSingletonGroupIdentifier.equals(service.getServiceGroupIdentifier()));
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        ClusterSingletonServiceRegistrationDelegator reg = null;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            Verify.verify(serviceEntityCandidateReg != null);
            reg = new ClusterSingletonServiceRegistrationDelegator(service, this);
            serviceGroup.add(reg);
            if (hasOwnership) {
                service.instantiateServiceInstance();
            }
        } catch (final Exception e) {
            LOG.debug("Unexpected error by registration service Provider {}", clusterSingletonGroupIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
        return reg;
    }

    @Override
    public final void unregisterService(final ClusterSingletonService service) {
        LOG.debug("UnregisterService method call for ClusterSingletonServiceGroup {}", clusterSingletonGroupIdentifier);
        Verify.verify(clusterSingletonGroupIdentifier.equals(service.getServiceGroupIdentifier()));
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            serviceGroup.remove(service);
            if (hasOwnership) {
                service.closeServiceInstance();
            }
        } catch (final Exception e) {
            LOG.debug("Unexpected error by registration service Provider {}", clusterSingletonGroupIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
            if (serviceGroup.isEmpty()) {
                this.closingClusterSingletonGroup();
            }
        }
    }

    @Override
    public final void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change {} for ClusterSingletonServiceGrou {}", ownershipChange,
                clusterSingletonGroupIdentifier);
        try {
            if (ownershipChange.inJeopardy()) {
                LOG.warn("Cluster Node lost connection to another cluster nodes {}", ownershipChange);
                lostOwnership();
                return;
            }
            if (serviceEntity.equals(ownershipChange.getEntity())) {
                if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED.equals(ownershipChange.getState())) {
                    /*
                     * SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()
                     */
                    tryToTakeOwnership();
                } else if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER.equals(ownershipChange.getState())
                        || EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER
                                .equals(ownershipChange.getState())) {
                    /*
                     * MASTER to SLAVE : !ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner()
                     */
                    lostOwnership();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed entity OwnershipChange {} in service Provider {}", ownershipChange,
                            clusterSingletonGroupIdentifier);
                }
            } else if (doubleCandidateEntity.equals(ownershipChange.getEntity())) {
                if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED.equals(ownershipChange.getState())) {
                    /*
                     * SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()
                     */
                    takeOwnership();
                } else if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER.equals(ownershipChange.getState())
                        || EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER
                                .equals(ownershipChange.getState())) {
                    /*
                     * MASTER to SLAVE : !ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner()
                     */
                    LOG.warn("Unexpected lost doubleCandidate {} leadership. Close service instance {}",
                            doubleCandidateEntity, clusterSingletonGroupIdentifier);
                    lostOwnership();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed doubleCandidate OwnershipChange {} in service Provider {}",
                            ownershipChange, clusterSingletonGroupIdentifier);
                }
            } else {
                LOG.warn("Unexpected EntityOwnershipChangeEvent for entity {}", ownershipChange);
            }
        } catch (final Exception e) {
            LOG.error("Unexpected Exception for service Provider {}", clusterSingletonGroupIdentifier, e);
            // TODO : think about close ... is it necessary?
        }
    }

    /*
     * Help method to registerated DoubleCandidateEntity. It is first step
     * before the actual instance take Leadership.
     */
    private void tryToTakeOwnership() {
        LOG.debug("TryToTakeLeadership method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            Verify.verify(serviceEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg == null);
            asyncCloseEntityCandidateReg = entityOwnershipService.registerCandidate(doubleCandidateEntity);
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
    private void takeOwnership() {
        LOG.debug("TakeLeadership method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            Verify.verify(serviceEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg != null);
            for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
                service.instantiateServiceInstance();
            }
            hasOwnership = true;
        } catch (final Exception e) {
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
    private void lostOwnership() {
        LOG.debug("LostLeadership method for service Provider {}", clusterSingletonGroupIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            Verify.verify(serviceEntityCandidateReg != null);
            final List<ListenableFuture<Void>> serviceCloseFutureList = new ArrayList<>();
            if (hasOwnership) {
                Verify.verify(asyncCloseEntityCandidateReg != null);
                for (final ClusterSingletonServiceRegistrationDelegator service : serviceGroup) {
                    serviceCloseFutureList.add(service.closeServiceInstance());
                }
            }

            final ListenableFuture<List<Void>> destroyFuture = Futures.allAsList(serviceCloseFutureList);
            Futures.addCallback(destroyFuture, newAsyncCloseCallback(clusterLock));
            /*
             * We wish to stop all possible EOS activitis before we don't close
             * a close candidate registration that acts as a guard. So we don't want
             * to release Semaphore (clusterLock) before we are not fully finished.
             * Semaphore lock release has to be realized as FutureCallback after a service
             * instance has fully closed prior to relinquishing service ownership.
             */
            needReleaseLock = false;
        } catch (final Exception e) {
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
    private void closeResources(final boolean needReleaseLock, final boolean needCloseProvider) {
        if (needReleaseLock) {
            clusterLock.release();
        }
        if (needCloseProvider) {
            final ListenableFuture<List<Void>> closeFutureList = this.closingClusterSingletonGroup();
            Futures.addCallback(closeFutureList, new FutureCallback<List<Void>>() {

                @Override
                public void onSuccess(final List<Void> result) {
                    removeThisGroupFromProvider(null);
                }

                @Override
                public void onFailure(final Throwable t) {
                    removeThisGroupFromProvider(t);
                }
            });
        }
    }

    /*
     * Help method for final ClusterSingletonGroup removing
     */
    protected final void removeThisGroupFromProvider(@Nullable final Throwable t) {
        LOG.debug("Removing ClusterSingletonServiceGroup {}", clusterSingletonGroupIdentifier);
        if (t != null) {
            LOG.warn("Unexpected problem by closingResources ClusterSingletonServiceGroup {}",
                    clusterSingletonGroupIdentifier);
        }
        allServiceGroups.remove(clusterSingletonGroupIdentifier, this);
    }

    /*
     * Help method creates FutureCallback for suspend Future
     */
    private FutureCallback<List<Void>> newAsyncCloseCallback(@Nullable final Semaphore semaphore) {
        final Consumer<Throwable> closeEntityCandidateRegistration = (@Nullable final Throwable t) -> {
            if (t != null) {
                LOG.warn("Unexpected error closing service instance {}", clusterSingletonGroupIdentifier, t);
            } else {
                LOG.debug("Destroy service Instance {} is success", clusterSingletonGroupIdentifier);
            }
            if (asyncCloseEntityCandidateReg != null) {
                asyncCloseEntityCandidateReg.close();
                asyncCloseEntityCandidateReg = null;
            }
            if (semaphore != null) {
                semaphore.release();
            }
            allServiceGroups.remove(clusterSingletonGroupIdentifier, this);
        };

        return new FutureCallback<List<Void>>() {

            @Override
            public void onSuccess(final List<Void> result) {
                closeEntityCandidateRegistration.accept(null);
            }

            @Override
            public void onFailure(final Throwable t) {
                closeEntityCandidateRegistration.accept(t);
            }
        };
    }

}
