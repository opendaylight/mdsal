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
 * Abstract class {@link AbstractGenericProjectProvider} represents a single cluster project instance.
 * There is only one fully instantiated project instance in a cluster at one time. This is realized via
 * a double candidate approach where a project instance maintains a candidate registration for ownership
 * of the project entity in the cluster and also a registration that acts as a guard to ensure a project
 * instance has fully closed prior to relinquishing project ownership. To achieve ownership of the
 * project, a project candidate must hold ownership of both these entities.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 * @param <R> the GenericEntityOwnershipListenerRegistration type
 */
public abstract class AbstractGenericProjectProvider<P extends Path<P>, E extends GenericEntity<P>,
                                                     C extends GenericEntityOwnershipChange<P, E>,
                                                     G extends GenericEntityOwnershipListener<P, C>,
                                                     S extends GenericEntityOwnershipService<P, E, G>,
                                                     R extends GenericEntityOwnershipListenerRegistration<P,G>>
        implements GenericEntityOwnershipListener<P, C>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGenericProjectProvider.class.getName());

    private static final String PROJECT_ENTITY_TYPE = "org.opendaylight.mdsal.projectEntityType";
    private static final String CLOSE_PROJECT_ENTITY_TYPE = "org.opendaylight.mdsal.project.asyncCloseEntityType";

    private final S entityOwnershipService;
    private final String projectEntityIdentifier;
    private final Semaphore clusterLock = new Semaphore(1);

    /* Entity instances */
    private E projectEntity;
    private E doubleCandidateEntity;

    /* EOS Entity Listeners Registration */
    private R projectEntityListenerReg;
    private R asyncCloseEntityListenerReg;
    /* EOS Candidate Registrations */
    private GenericEntityOwnershipCandidateRegistration<P, E> projectEntityCandidateReg;
    private GenericEntityOwnershipCandidateRegistration<P, E> asyncCloseEntityCandidateReg;

    /**
     * Initializes all internal instance members.
     *
     * @param entityOwnershipService the {@link GenericEntityOwnershipService} instance
     */
    protected AbstractGenericProjectProvider(@Nonnull final S entityOwnershipService) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.projectEntityIdentifier = this.getClass().getCanonicalName();
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
     * This method is invoked to instantiate an underlying project instance when
     * ownership has been granted for the project entity.
     */
    protected abstract void instantiateProjectInstance();

    /**
     * This method is invoked to close the underlying project instance when ownership has been lost
     * for the project entity. If the act of closing the instance may perform blocking operations or
     * take some time, it should be done asynchronously to avoid blocking the current thread.
     *
     * @return a ListenableFuture that is completed when the underlying instance close operation is complete.
     */
    protected abstract ListenableFuture<Void> closeProjectInstance();

    /**
     * This method must be called once on startup to initialize this provider.
     */
    public final void initializeProvider() {
        LOG.debug("Initialization method for project Provider {}", projectEntityIdentifier);
        this.projectEntity = createEntity(PROJECT_ENTITY_TYPE, projectEntityIdentifier);
        this.doubleCandidateEntity = createEntity(CLOSE_PROJECT_ENTITY_TYPE, projectEntityIdentifier);
        this.projectEntityListenerReg = registerListener(PROJECT_ENTITY_TYPE, entityOwnershipService);
        this.asyncCloseEntityListenerReg = registerListener(CLOSE_PROJECT_ENTITY_TYPE, entityOwnershipService);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(projectEntityCandidateReg == null);
            projectEntityCandidateReg = entityOwnershipService.registerCandidate(projectEntity);
        } catch (final Exception e) {
            LOG.debug("Unexpected error by registration project Provider {}", projectEntityIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    @Override
    public final void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change for project Provider {}", ownershipChange);
        try {
            if (ownershipChange.inJeopardy()) {
                LOG.warn("Cluster Node lost connection to another cluster nodes {}", ownershipChange);
                lostOwnership();
                return;
            }
            if (projectEntity.equals(ownershipChange.getEntity())) {
                if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED.equals(ownershipChange.getState())) {
                    /* SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner() */
                    tryToTakeOwnership();
                } else if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER.equals(ownershipChange.getState())
                        || EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER.equals(ownershipChange.getState())) {
                    /* MASTER to SLAVE : !ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner() */
                    lostOwnership();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed entity OwnershipChange {} in project Provider {}", ownershipChange,
                            projectEntityIdentifier);
                }
            } else if (doubleCandidateEntity.equals(ownershipChange.getEntity())) {
                if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED.equals(ownershipChange.getState())) {
                    /* SLAVE to MASTER : ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner() */
                    takeOwnership();
                } else if (EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER.equals(ownershipChange.getState())
                        || EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER.equals(ownershipChange.getState())) {
                    /* MASTER to SLAVE : !ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner() */
                    LOG.warn("Unexpected lost doubleCandidate {} leadership. Close project instance {}",
                            doubleCandidateEntity, projectEntityIdentifier);
                    lostOwnership();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed doubleCandidate OwnershipChange {} in project Provider {}",
                            ownershipChange, projectEntityIdentifier);
                }
            } else {
                LOG.warn("Unexpected EntityOwnershipChangeEvent for entity {}", ownershipChange);
            }
        } catch (final Exception e) {
            LOG.error("Unexpected Exception for project Provider {}", projectEntityIdentifier, e);
            // TODO : think about close ... is it necessary?
        }
    }

    @Override
    public final void close() {
        LOG.debug("Close method for project Provider {}", projectEntityIdentifier);
        boolean needReleaseLock = false;
        try {
            needReleaseLock = clusterLock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOG.warn("Unexpected Exception for project Provider {} in closing phase.", projectEntityIdentifier, e);
        } finally {
            if (projectEntityCandidateReg != null) {
                projectEntityCandidateReg.close();
                projectEntityCandidateReg = null;
            }
            if (projectEntityListenerReg != null) {
                projectEntityListenerReg.close();
                projectEntityListenerReg = null;
            }
            if (asyncCloseEntityListenerReg != null) {
                asyncCloseEntityListenerReg.close();
                asyncCloseEntityListenerReg = null;
            }
            final ListenableFuture<Void> destroyFuture = closeProjectInstance();
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
                LOG.warn("Unexpected error closing project instance {}", projectEntityIdentifier, t);
            } else {
                LOG.debug("Destroy project Instance {} is success", projectEntityIdentifier);
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
        LOG.debug("TryToTakeLeadership method for project Provider {}", projectEntityIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(projectEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg == null);
            asyncCloseEntityCandidateReg = entityOwnershipService.registerCandidate(doubleCandidateEntity);
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for project Provider {} in TryToTakeLeadership",
                    projectEntityIdentifier, e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls setupProject method for create single cluster-wide project instance.
     */
    private void takeOwnership() {
        LOG.debug("TakeLeadership method for project Provider {}", projectEntityIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(projectEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg != null);
            instantiateProjectInstance();
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for project Provider {} in TakeLeadership", projectEntityIdentifier,
                    e);
            needCloseProviderInstance = true;
        } finally {
            closeResources(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls suspendProject method for stop this single cluster-wide project instance.
     * The last async. step has to close DoubleCandidateRegistration reference what should initialize
     * new election for DoubleCandidateEntity.
     */
    private void lostOwnership() {
        LOG.debug("LostLeadership method for project Provider {}", projectEntityIdentifier);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(projectEntityCandidateReg != null);
            Verify.verify(asyncCloseEntityCandidateReg != null);
            final ListenableFuture<Void> destroyFuture = closeProjectInstance();
            Futures.addCallback(destroyFuture, newAsyncCloseCallback(clusterLock));
            needReleaseLock = false;
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for project Provider {} in LostLeadership", projectEntityIdentifier,
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
        Verify.verify(projectEntity != null);
        Verify.verify(doubleCandidateEntity != null);
        Verify.verify(projectEntityListenerReg != null);
        Verify.verify(asyncCloseEntityListenerReg != null);
    }
}
