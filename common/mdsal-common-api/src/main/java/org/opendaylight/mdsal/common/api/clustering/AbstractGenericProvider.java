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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class {@link AbstractGenericProvider} provide a single cluster project instance
 * functionality for every ProjectProvider. So we will have only one full instantiated project in
 * whole cluster. Single instance functionality is realized by a Double candidate aproach.
 * First candidate represent a cluster role for every instance and second candidate represent
 * a quard for changing role in cluster. Master has registrated both candidate and both are holding
 * mastership for their entities. Main candidate hold the project role in cluster and second has to be
 * closed after full finish async. closing instance.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 * @param <R> the GenericEntityOwnershipListenerRegistration type
 */
public abstract class AbstractGenericProvider<P extends Path<P>, E extends GenericEntity<P>,
                                              C extends GenericEntityOwnershipChange<P, E>,
                                              G extends GenericEntityOwnershipListener<P, C>,
                                              S extends GenericEntityOwnershipService<P, E, G>,
                                              R extends GenericEntityOwnershipListenerRegistration<P,G>>
        implements GenericEntityOwnershipListener<P, C>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGenericProvider.class.getName());

    private static final String PROJECT_ENTITY = "bundle";
    private static final String DOUBLE_CANDIDATE_PROJECT_ENTITY = "bundleDoubleCandidate";

    private final S eos;
    private final E entity;
    private final E doubleCandidateEntity;
    private final String projectIdentificator;
    private final Semaphore clusterLock = new Semaphore(1);

    /* EOS Entity Listeners Registration */
    private R projectEntityReg;
    private R doubleCandidateProjectEntityReg;
    /* EOS Candidate Registrations */
    private GenericEntityOwnershipCandidateRegistration<P, E> candidateRegistration;
    private GenericEntityOwnershipCandidateRegistration<P, E> doubleCandidateRegistration;

    /**
     * Initialization all needed class internal property for {@link AbstractGenericProvider}
     *
     * @param entityOwnershipService - we need only {@link GenericEntityOwnershipService}
     */
    public AbstractGenericProvider(@Nonnull final S entityOwnershipService) {
        this.eos = Preconditions.checkNotNull(entityOwnershipService);
        this.projectIdentificator = this.getClass().getCanonicalName();
        this.entity = createEntity(PROJECT_ENTITY, projectIdentificator);
        this.projectEntityReg = registerListener(PROJECT_ENTITY, eos);
        this.doubleCandidateEntity = createEntity(DOUBLE_CANDIDATE_PROJECT_ENTITY, projectIdentificator);
        this.doubleCandidateProjectEntityReg = registerListener(DOUBLE_CANDIDATE_PROJECT_ENTITY, eos);
    }

    /**
     * Method implementation creates a defined {@link GenericEntity} type Entity instance
     *
     * @param type - Entity registration type
     * @param ident - identificator
     * @return instance of Entity extended GenericEntity type
     */
    protected abstract E createEntity(final String type, final String ident);

    /**
     * Method implementation registers a defined {@link GenericEntityOwnershipListenerRegistration} type
     * EntityOwnershipListenerRegistration.
     *
     * @param type - entityType
     * @param entityOwnershipService - EOS type
     * @return instance of EntityOwnershipListenerRegistration
     */
    protected abstract R registerListener(String type, S entityOwnershipService);

    /**
     * Method implementation has to be realized by sub class. It represents
     * standard Project instantiating. This method is called from
     * {@link AbstractGenericProvider#ownershipChanged(GenericEntityOwnershipChange)}
     * for take DoubleCandidateEntity Leadership.
     */
    protected abstract void setupProject();

    /**
     * Method implementation has to be realized by sub class. It represents
     * standard Project closing. This method is called from
     * {@link AbstractGenericProvider#ownershipChanged(GenericEntityOwnershipChange)}
     * for lost MainCandidateEntity Leadership. The last async. step has to close
     * DoubleCandidateRegistration.
     * 
     * @return Future represent all possible async. functionality which has to be done (e.g. Transaction.submit)
     */
    protected abstract ListenableFuture<Void> suspendProject();

    /**
     * Cluster-wide initialization ProjectProvider. Method registers Main Candidate Entity
     * to {@link GenericEntityOwnershipService}
     */
    public final void initializationClusterProjectProvider() {
        LOG.debug("Initialization method for project Provider {}", projectIdentificator);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(candidateRegistration == null);
            candidateRegistration = eos.registerCandidate(entity);
        } catch (final Exception e) {
            LOG.debug("Unexpected error by registration project Provider {}", projectIdentificator, e);
            needCloseProviderInstance = true;
        } finally {
            finalizeEveryAcquiredFunctions(needReleaseLock, needCloseProviderInstance);
        }
    }

    @Override
    public final void ownershipChanged(final C ownershipChange) {
        try {
            Preconditions.checkArgument(ownershipChange != null);
            if (ownershipChange.inJeopardy()) {
                LOG.warn("Cluster Node lost connection to another cluster nodes {}", ownershipChange);
                this.close();
                return;
            }
            if (entity.equals(ownershipChange.getEntity())) {
                if (ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()) {
                    /* SLAVE to MASTER */
                    tryToTakeLeadership();
                } else if (!ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner()) {
                    /* MASTER to SLAVE */
                    lostLeadership();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed entity OwnershipChange {} in project Provider {}", ownershipChange,
                            projectIdentificator);
                }
            } else if (doubleCandidateEntity.equals(ownershipChange.getEntity())) {
                if (ownershipChange.getState().isOwner() && !ownershipChange.getState().wasOwner()) {
                    /* SLAVE to MASTER */
                    takeLeadership();
                } else if (!ownershipChange.getState().isOwner() && ownershipChange.getState().wasOwner()) {
                    /* MASTER to SLAVE */
                    LOG.error("Unexpected lost doubleCandidate {} leadership. Close whole provider {}",
                            doubleCandidateEntity, projectIdentificator);
                    this.close();
                } else {
                    /* Not needed notifications */
                    LOG.debug("Not processed doubleCandidate OwnershipChange {} in project Provider {}",
                            ownershipChange, projectIdentificator);
                }
            } else {
                LOG.warn("Unexpected EntityOwnershipChangeEvent for entity {}", ownershipChange);
            }
        } catch (final Exception e) {
            LOG.error("Unexpected Exception for project Provider {}", projectIdentificator, e);
            // TODO : think about close ... is it necessary?
        }
    }

    @Override
    public final void close() {
        LOG.debug("Close method for project Provider {}", projectIdentificator);
        boolean needReleaseLock = false;
        try {
            clusterLock.tryAcquire(10, TimeUnit.SECONDS);
            needReleaseLock = true;
        } catch (final Exception e) {
            LOG.warn("Unexpected Exception for project Provider {} in closing phase.", projectIdentificator, e);
        } finally {
            if (candidateRegistration != null) {
                candidateRegistration.close();
                candidateRegistration = null;
            }
            if (projectEntityReg != null) {
                projectEntityReg.close();
                projectEntityReg = null;
            }
            if (doubleCandidateProjectEntityReg != null) {
                doubleCandidateProjectEntityReg.close();
                doubleCandidateProjectEntityReg = null;
            }
            final ListenableFuture<Void> destroyFuture = suspendProject();
            final Semaphore finalRelease = needReleaseLock ? clusterLock : null;
            Futures.addCallback(destroyFuture, getSuspendCallback(doubleCandidateRegistration, finalRelease));
        }
    }

    /*
     * Help method creates FutureCallback for suspend Future
     */
    private FutureCallback<Void> getSuspendCallback(
            final GenericEntityOwnershipCandidateRegistration<P, E> candidateReg,
            final Semaphore semaphore) {

        return new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Destroy project Instance {} is success", projectIdentificator);
                if (candidateReg != null) {
                    doubleCandidateRegistration = null;
                    candidateReg.close();
                }
                if (semaphore != null) {
                    semaphore.release();
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Unexpected Error by destroying project Instance {}", projectIdentificator, t);
                if (candidateReg != null) {
                    doubleCandidateRegistration = null;
                    candidateReg.close();
                }
                if (semaphore != null) {
                    semaphore.release();
                }
            }
        };
    }

    /*
     * Help method to registerated DoubleCandidateEntity. It is first step
     * before the actual instance take Leadership.
     */
    private void tryToTakeLeadership() {
        LOG.debug("TryToTakeLeadership method for project Provider {}", projectIdentificator);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(candidateRegistration != null);
            Verify.verify(doubleCandidateRegistration == null);
            doubleCandidateRegistration = eos.registerCandidate(doubleCandidateEntity);
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for project Provider {} in TryToTakeLeadership",
                    projectIdentificator, e);
            needCloseProviderInstance = true;
        } finally {
            finalizeEveryAcquiredFunctions(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls setupProject method for create single cluster-wide project instance.
     */
    private void takeLeadership() {
        LOG.debug("TakeLeadership method for project Provider {}", projectIdentificator);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(candidateRegistration != null);
            Verify.verify(doubleCandidateRegistration != null);
            setupProject();
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for project Provider {} in TakeLeadership", projectIdentificator, e);
            needCloseProviderInstance = true;
        } finally {
            finalizeEveryAcquiredFunctions(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method calls suspendProject method for stop this single cluster-wide project instance.
     * The last async. step has to close DoubleCandidateRegistration reference what should initialize
     * new election for DoubleCandidateEntity.
     */
    private void lostLeadership() {
        LOG.debug("LostLeadership method for project Provider {}", projectIdentificator);
        boolean needReleaseLock = false;
        boolean needCloseProviderInstance = false;
        try {
            clusterLock.acquire();
            needReleaseLock = true;
            verifyListenerRegs();
            Verify.verify(candidateRegistration != null);
            Verify.verify(doubleCandidateRegistration != null);
            final ListenableFuture<Void> destroyFuture = suspendProject();
            Futures.addCallback(destroyFuture, getSuspendCallback(doubleCandidateRegistration, clusterLock));
            needReleaseLock = false;
        } catch (final Exception e) {
            LOG.error("Unexpected exception state for project Provider {} in LostLeadership", projectIdentificator, e);
            needCloseProviderInstance = true;
        } finally {
            finalizeEveryAcquiredFunctions(needReleaseLock, needCloseProviderInstance);
        }
    }

    /*
     * Help method for finalization every acquired functionality
     */
    private void finalizeEveryAcquiredFunctions(final boolean needReleaseLock, final boolean needCloseProvider) {
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
        Verify.verify(entity != null);
        Verify.verify(doubleCandidateEntity != null);
        Verify.verify(projectEntityReg != null);
        Verify.verify(doubleCandidateProjectEntityReg != null);
    }
}
