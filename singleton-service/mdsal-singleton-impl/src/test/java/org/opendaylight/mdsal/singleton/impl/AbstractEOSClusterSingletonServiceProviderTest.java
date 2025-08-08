/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import static org.opendaylight.mdsal.singleton.impl.EOSClusterSingletonServiceProvider.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.impl.EOSClusterSingletonServiceProvider.SERVICE_ENTITY_TYPE;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Abstract {@link EOSClusterSingletonServiceProvider} testing substrate.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
abstract class AbstractEOSClusterSingletonServiceProviderTest {
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
        public ServiceGroupIdentifier getIdentifier() {
            return SERVICE_ID;
        }

        @Override
        public void instantiateServiceInstance() {
            serviceState = TestClusterSingletonServiceState.STARTED;
        }

        TestClusterSingletonServiceState getServiceState() {
            return serviceState;
        }

        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            serviceState = TestClusterSingletonServiceState.DESTROYED;
            return Futures.immediateFuture(null);
        }
    }

    private static final @NonNull String SERVICE_NAME = "testServiceName";
    private static final @NonNull ServiceGroupIdentifier SERVICE_ID = new ServiceGroupIdentifier(SERVICE_NAME);
    static final @NonNull DOMEntity ENTITY = new DOMEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
    static final @NonNull DOMEntity DOUBLE_ENTITY = new DOMEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);

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
    void beforeEach() throws Exception {
        doNothing().when(mockEosEntityListReg).close();
        doNothing().when(mockEosDoubleEntityListReg).close();
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockDoubleEntityCandReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(SERVICE_ENTITY_TYPE),
                any(EOSClusterSingletonServiceProvider.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_SERVICE_ENTITY_TYPE),
                any(EOSClusterSingletonServiceProvider.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(ENTITY);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(DOUBLE_ENTITY);

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
        assertEquals(ENTITY, EOSClusterSingletonServiceProvider.createEntity(SERVICE_ENTITY_TYPE, SERVICE_ID));
        assertEquals(DOUBLE_ENTITY,
            EOSClusterSingletonServiceProvider.createEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_ID));
    }

    /**
     * Test parser ServiceIdentifier from Entity.
     */
    @Test
    void getIdentifierClusterSingletonServiceProviderTest() {
        assertEquals(SERVICE_NAME, EOSClusterSingletonServiceProvider.getServiceIdentifierFromEntity(ENTITY));
        assertEquals(SERVICE_NAME, EOSClusterSingletonServiceProvider.getServiceIdentifierFromEntity(DOUBLE_ENTITY));
    }

    /**
     * Test GoldPath for initialization {@link ClusterSingletonService}.
     */
    @Test
    void initializationClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE {@link ClusterSingletonService}.
     */
    @Test
    void slaveInitClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE, but NO-MASTER {@link ClusterSingletonService}.
     */
    @Test
    void slaveInitNoMasterClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, false);
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void masterInitClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void masterInitSlaveDoubleCandidateClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void takeLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void masterInitClusterSingletonServiceTwoServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks CandidateAlreadyRegisteredException processing in initialization phase.
     */
    @Test
    void initializationClusterSingletonServiceCandidateAlreadyRegistredTest() throws Exception {
        doThrow(CandidateAlreadyRegisteredException.class).when(mockEos).registerCandidate(ENTITY);
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService));
        assertEquals("Service group already registered", ex.getMessage());
        assertInstanceOf(CandidateAlreadyRegisteredException.class, ex.getCause());
    }

    /**
     * Test GoldPath for lostLeadership during tryToTakeLeadership with ownership result MASTER
     * {@link ClusterSingletonService}.
     */
    @Test
    void lostLeadershipDuringTryToTakeLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for lostLeadership with ownership result MASTER-TO-SLAVE {@link ClusterSingletonService}.
     */
    @Test
    void lostLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Slave Instance.
     */
    @Test
    void inJeopardySlaveTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
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
    void takeLeadershipClusterSingletonServiceTowServicesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    void closeClusterSingletonServiceRegistrationNoRoleTest() throws Exception {
        try (var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService)) {
            assertNotNull(reg);
            verify(mockEos).registerCandidate(ENTITY);
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        }
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
    void closeClusterSingletonServiceRegistrationNoRoleTwoServicesTest() throws Exception {
        try (var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService)) {
            assertNotNull(reg);
            verify(mockEos).registerCandidate(ENTITY);
            final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
            assertNotNull(reg2);
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        }
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
    void closeClusterSingletonServiceRegistrationSlaveTest() throws Exception {
        try (var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService)) {
            assertNotNull(reg);
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
            verify(mockEos).registerCandidate(ENTITY);
            clusterSingletonServiceProvider.ownershipChanged(ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        }
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
    void closeClusterSingletonServiceRegistrationSlaveTwoServicesTest() throws Exception {
        try (var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService)) {
            assertNotNull(reg);
            verify(mockEos).registerCandidate(ENTITY);
            final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
            assertNotNull(reg2);
            clusterSingletonServiceProvider.ownershipChanged(ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        }
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
    void closeClusterSingletonServiceRegistrationMasterTwoServicesTest() throws Exception {
        try (var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService)) {
            assertNotNull(reg);
            final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
            assertNotNull(reg2);
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
            verify(mockEos).registerCandidate(ENTITY);
            clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
            verify(mockEos).registerCandidate(DOUBLE_ENTITY);
            clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
            assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
            assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
        }
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
    void tryToTakeLeaderForClosedServiceRegistrationTest() throws Exception {
        try (var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService)) {
            assertNotNull(reg);
            final var reg2 = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
            assertNotNull(reg2);
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
            assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
            verify(mockEos).registerCandidate(ENTITY);
        }
        clusterSingletonServiceProvider.ownershipChanged(ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(DOUBLE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
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
