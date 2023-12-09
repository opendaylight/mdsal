/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NEW_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_CHANGED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_LOST_NO_OWNER;
import static org.opendaylight.mdsal.singleton.dom.impl.EOSClusterSingletonServiceProvider.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.EOSClusterSingletonServiceProvider.SERVICE_ENTITY_TYPE;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Abstract {@link EOSClusterSingletonServiceProvider} testing substrate.
 */
abstract class AbstractEOSClusterSingletonServiceProviderTest extends AbstractTest {
    /**
     * Base states for AbstractClusterProjectProvider.
     */
    enum TestClusterSingletonServiceState {
        /**
         * State represents a correct Instantiated process.
         */
        STARTED,
        /**
         * State represents a correct call abstract method instantiatingProject.
         */
        INITIALIZED,
        /**
         * State represents a correct call abstract method destryingProject.
         */
        DESTROYED,
    }

    static class TestClusterSingletonService implements ClusterSingletonService {
        private TestClusterSingletonServiceState serviceState = TestClusterSingletonServiceState.INITIALIZED;

        @Override
        public final ServiceGroupIdentifier getIdentifier() {
            return SERVICE_ID;
        }

        @Override
        public final void instantiateServiceInstance() {
            serviceState = TestClusterSingletonServiceState.STARTED;
        }

        final TestClusterSingletonServiceState getServiceState() {
            return serviceState;
        }

        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            serviceState = TestClusterSingletonServiceState.DESTROYED;
            return Futures.immediateFuture(null);
        }
    }

    @Mock
    DOMEntityOwnershipService mockEos;
    @Mock
    Registration mockEntityCandReg;
    @Mock
    Registration mockDoubleEntityCandReg;
    @Mock
    Registration mockEosEntityListReg;
    @Mock
    Registration mockEosDoubleEntityListReg;

    EOSClusterSingletonServiceProvider clusterSingletonServiceProvider;
    TestClusterSingletonService clusterSingletonService;
    TestClusterSingletonService clusterSingletonService2;

    @BeforeEach
    void setup() throws Exception {
        doNothing().when(mockEosEntityListReg).close();
        doNothing().when(mockEosDoubleEntityListReg).close();
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockDoubleEntityCandReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(SERVICE_ENTITY_TYPE),
                any(EOSClusterSingletonServiceProvider.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_SERVICE_ENTITY_TYPE),
                any(EOSClusterSingletonServiceProvider.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(MAIN_ENTITY);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(CLOSE_ENTITY);

        clusterSingletonServiceProvider = new EOSClusterSingletonServiceProvider(mockEos);
        verify(mockEos).registerListener(SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);
        verify(mockEos).registerListener(CLOSE_SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);

        clusterSingletonService = instantiateService();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonService2 = instantiateService();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    TestClusterSingletonService instantiateService() {
        return new TestClusterSingletonService();
    }

    /**
     * Test checks NullPointer for null {@link DOMEntityOwnershipService} input value.
     */
    @Test
    void initializationClusterSingletonServiceProviderNullInputTest() {
        assertThrows(NullPointerException.class, () -> new EOSClusterSingletonServiceProvider(null));
    }

    /**
     * Test GoldPath for close {@link EOSClusterSingletonServiceProvider}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    void closeClusterSingletonServiceProviderTest() throws Exception {
        verify(mockEos).registerListener(SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);
        verify(mockEos).registerListener(CLOSE_SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);
        clusterSingletonServiceProvider.close();
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
    }

    /**
     * Test parser ServiceIdentifier from Entity.
     */
    @Test
    void makeEntityClusterSingletonServiceProviderTest() {
        final var testEntity = EOSClusterSingletonServiceProvider.createEntity(SERVICE_ENTITY_TYPE,
            SERVICE_NAME);
        assertEquals(MAIN_ENTITY, testEntity);
        final var testDbEn = EOSClusterSingletonServiceProvider.createEntity(CLOSE_SERVICE_ENTITY_TYPE,
                SERVICE_NAME);
        assertEquals(CLOSE_ENTITY, testDbEn);
    }

    /**
     * Test parser ServiceIdentifier from Entity.
     */
    @Test
    void getIdentifierClusterSingletonServiceProviderTest() {
        assertEquals(SERVICE_NAME, EOSClusterSingletonServiceProvider.getServiceIdentifierFromEntity(MAIN_ENTITY));
        assertEquals(SERVICE_NAME, EOSClusterSingletonServiceProvider.getServiceIdentifierFromEntity(CLOSE_ENTITY));
    }

    /**
     * Test GoldPath for initialization {@link ClusterSingletonService}.
     */
    @Test
    void initializationClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE {@link ClusterSingletonService}.
     */
    @Test
    void slaveInitClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        verify(mockEos, never()).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE, but NO-MASTER {@link ClusterSingletonService}.
     */
    @Test
    void slaveInitNoMasterClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, false);
        verify(mockEos, never()).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void masterInitClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void masterInitSlaveDoubleCandidateClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void takeLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void masterInitClusterSingletonServiceTwoServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void takeLeadershipClusterSingletonServiceTwoAddDuringWaitPhaseServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks CandidateAlreadyRegisteredException processing in initialization phase.
     */
    @Test
    public void initializationClusterSingletonServiceCandidateAlreadyRegistredTest() throws Exception {
        doThrow(CandidateAlreadyRegisteredException.class).when(mockEos).registerCandidate(MAIN_ENTITY);
        assertThrows(RuntimeException.class,
            () -> clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService));
    }

    /**
     * Test GoldPath for lostLeadership during tryToTakeLeadership with ownership result MASTER
     * {@link ClusterSingletonService}.
     */
    @Test
    public void lostLeadershipDuringTryToTakeLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for lostLeadership with ownership result MASTER-TO-SLAVE {@link ClusterSingletonService}.
     */
    @Test
    public void lostLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Slave Instance.
     */
    @Test
    public void inJeopardySlaveTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        verify(mockEos, never()).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTowServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    public void closeClusterSingletonServiceRegistrationNoRoleTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    public void closeClusterSingletonServiceRegistrationNoRoleTwoServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    public void closeClusterSingletonServiceRegistrationSlaveTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    public void closeClusterSingletonServiceRegistrationSlaveTwoServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterTwoServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change.
     */
    @Test
    public void tryToTakeLeaderForClosedServiceRegistrationTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        reg.close();
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }
}
