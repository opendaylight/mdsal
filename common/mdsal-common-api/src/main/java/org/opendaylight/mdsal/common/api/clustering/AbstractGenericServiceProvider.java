/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class {@link AbstractGenericServiceProvider} represents a single cluster service instance.
 * There is only one fully instantiated service instance in a cluster at one time. This is realized via
 * a double candidate approach where a service instance maintains a candidate registration for ownership
 * of the service entity in the cluster and also a registration that acts as a guard to ensure a service
 * instance has fully closed prior to relinquishing service ownership. To achieve ownership of the
 * service, a service candidate must hold ownership of both these entities.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 * @param <R> the GenericEntityOwnershipListenerRegistration type
 */
public abstract class AbstractGenericServiceProvider<P extends Path<P>, E extends GenericEntity<P>,
                                                     C extends GenericEntityOwnershipChange<P, E>,
                                                     G extends GenericEntityOwnershipListener<P, C>,
                                                     S extends GenericEntityOwnershipService<P, E, G>,
                                                     R extends GenericEntityOwnershipListenerRegistration<P, G>>
        implements GenericEntityOwnershipListener<P, C>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGenericServiceProvider.class.getName());

    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final S entityOwnershipService;
    private final String serviceEntityIdentifier;
    private final Semaphore clusterLock = new Semaphore(1);

    /* Entity instances */
    private E serviceEntity;
    private E doubleCandidateEntity;

    /* EOS Entity Listeners Registration */
    private R serviceEntityListenerReg;
    private R asyncCloseEntityListenerReg;
    /* EOS Candidate Registrations */
    private GenericEntityOwnershipCandidateRegistration<P, E> serviceEntityCandidateReg;
    private GenericEntityOwnershipCandidateRegistration<P, E> asyncCloseEntityCandidateReg;

    /**
     * Initializes all internal instance members.
     *
     * @param entityOwnershipService the {@link GenericEntityOwnershipService} instance
     */
    protected AbstractGenericServiceProvider(@Nonnull final S entityOwnershipService,
            @Nonnull final String serviceEntityIdentifier) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.serviceEntityIdentifier = Preconditions.checkNotNull(serviceEntityIdentifier);
    }

    /**
     * Creates an extended {@link GenericEntity} instance.
     *
     * @param entityType the type of the entity
     * @param entityIdentifier the identifier of the entity
     * @return instance of Entity extended GenericEntity type
     */
    protected abstract E createEntity(final String entityType, final String entityIdentifier);

    /**
     * Method implementation registers a defined {@link GenericEntityOwnershipListenerRegistration} type
     * EntityOwnershipListenerRegistration.
     *
     * @param entityType the type of the entity
     * @param entityOwnershipServiceInst - EOS type
     * @return instance of EntityOwnershipListenerRegistration
     */
    protected abstract R registerListener(String entityType, S entityOwnershipServiceInst);

    /**
     * This method is invoked to instantiate an underlying service instance when
     * ownership has been granted for the service entity.
     */
    protected abstract void instantiateServiceInstance();

    /**
     * This method is invoked to close the underlying service instance when ownership has been lost
     * for the service entity. If the act of closing the instance may perform blocking operations or
     * take some time, it should be done asynchronously to avoid blocking the current thread.
     *
     * @return a ListenableFuture that is completed when the underlying instance close operation is complete.
     */
    protected abstract ListenableFuture<Void> closeServiceInstance();

    /**
     * This method must be called once on startup to initialize this provider.
     */
    public final void initializeProvider() {
        LOG.debug("Initialization method for service Provider {}", serviceEntityIdentifier);
        this.serviceEntity = createEntity(SERVICE_ENTITY_TYPE, serviceEntityIdentifier);
        this.doubleCandidateEntity = createEntity(CLOSE_SERVICE_ENTITY_TYPE, serviceEntityIdentifier);
        this.serviceEntityListenerReg = registerListener(SERVICE_ENTITY_TYPE, entityOwnershipService);
        this.asyncCloseEntityListenerReg = registerListener(CLOSE_SERVICE_ENTITY_TYPE, entityOwnershipService);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(serviceEntityCandidateReg == null);
            serviceEntityCandidateReg = entityOwnershipService.registerCandidate(serviceEntity);
        } catch (final Exception e) {
            LOG.debug("Unexpected error by registration service Provider {}", serviceEntityIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    @Override
    public final void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change for service Provider {}", ownershipChange);
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
                            serviceEntityIdentifier);
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
                            doubleCandidateEntity, serviceEntityIdentifier);
                    lostOwnership();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed doubleCandidate OwnershipChange {} in service Provider {}",
                            ownershipChange, serviceEntityIdentifier);
                }
            } else {
                LOG.warn("Unexpected EntityOwnershipChangeEvent for entity {}", ownershipChange);
            }
        } catch (final Exception e) {
            LOG.error("Unexpected Exception for service Provider {}", serviceEntityIdentifier, e);
            // TODO : think about close ... is it necessary?
        }
    }

    @Override
    public final void close() {
        LOG.debug("Close method for service Provider {}", serviceEntityIdentifier);
        boolean needReleaseLock = false;
        try {
            needReleaseLock = clusterLock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOG.warn("Unexpected Exception for service Provider {} in closing phase.", serviceEntityIdentifier, e);
        } finally {
            if (serviceEntityCandidateReg != null) {
                serviceEntityCandidateReg.close();
                serviceEntityCandidateReg = null;
            }
            if (serviceEntityListenerReg != null) {
                serviceEntityListenerReg.close();
                serviceEntityListenerReg = null;
            }
            if (asyncCloseEntityListenerReg != null) {
                asyncCloseEntityListenerReg.close();
                asyncCloseEntityListenerReg = null;
            }
            final ListenableFuture<Void> destroyFuture = closeServiceInstance();
            final Semaphore finalRelease = needReleaseLock ? clusterLock : null;
            Futures.addCallback(destroyFuture, newAsyncCloseCallback(finalRelease));
        }
    }

    /*
     * Help method creates FutureCallback for suspend Future
     */
    private FutureCallback<Void> newAsyncCloseCallback(@Nullable final Semaphore semaphore) {
        final Consumer<Throwable> closeEntityCandidateRegistration = (@Nullable final Throwable t) -> {
            if (t != null) {
                LOG.warn("Unexpected error closing service instance {}", serviceEntityIdentifier, t);
            } else {
                LOG.debug("Destroy service Instance {} is success", serviceEntityIdentifier);
            }
            if (asyncCloseEntityCandidateReg != null) {
                asyncCloseEntityCandidateReg.close();
                asyncCloseEntityCandidateReg = null;
            }
            if (semaphore != null) {
                semaphore.release();
            }
        };

        return new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                closeEntityCandidateRegistration.accept(null);
            }

            @Override
            public void onFailure(final Throwable t) {
                closeEntityCandidateRegistration.accept(t);
            }
        };
    }

    /*
     * Help method to registerated DoubleCandidateEntity. It is first step
     * before the actual instance take Leadership.
     */
    private void tryToTakeOwnership() {
        LOG.debug("TryToTakeLeadership method for service Provider {}", serviceEntityIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(serviceEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg == null);
            asyncCloseEntityCandidateReg = entityOwnershipService.registerCandidate(doubleCandidateEntity);
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for service Provider {} in TryToTakeLeadership",
                    serviceEntityIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls setupService method for create single cluster-wide service instance.
     */
    private void takeOwnership() {
        LOG.debug("TakeLeadership method for service Provider {}", serviceEntityIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(serviceEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg != null);
            instantiateServiceInstance();
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for service Provider {} in TakeLeadership", serviceEntityIdentifier,
                    e);
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
        LOG.debug("LostLeadership method for service Provider {}", serviceEntityIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(serviceEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg != null);
            final ListenableFuture<Void> destroyFuture = closeServiceInstance();
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
            LOG.error("Unexpected exception state for service Provider {} in LostLeadership", serviceEntityIdentifier,
                    e);
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
            this.close();
        }
    }

    /*
     * Method for verification not null initial property values
     */
    private void verifyListenerRegs() {
        Verify.verify(serviceEntity != null);
        Verify.verify(doubleCandidateEntity != null);
        Verify.verify(serviceEntityListenerReg != null);
        Verify.verify(asyncCloseEntityListenerReg != null);
    }
}
