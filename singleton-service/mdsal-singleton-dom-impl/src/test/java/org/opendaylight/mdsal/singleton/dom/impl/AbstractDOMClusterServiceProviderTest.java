/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.SERVICE_ENTITY_TYPE;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

/**
 * Abstract {@link DOMClusterSingletonServiceProviderImpl} testing substrate.
 */
public abstract class AbstractDOMClusterServiceProviderTest {
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
        private static final ServiceGroupIdentifier SERVICE_ID = ServiceGroupIdentifier.create(SERVICE_NAME);

        private TestClusterSingletonServiceState serviceState = TestClusterSingletonServiceState.INITIALIZED;

        @Override
        public final ServiceGroupIdentifier getIdentifier() {
            return SERVICE_ID;
        }

        @Override
        public final void instantiateServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.STARTED;
        }

        final TestClusterSingletonServiceState getServiceState() {
            return serviceState;
        }

        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.DESTROYED;
            return Futures.immediateFuture(null);
        }
    }

    static final String SERVICE_NAME = "testServiceName";
    static final DOMEntity ENTITY = new DOMEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
    static final DOMEntity DOUBLE_ENTITY = new DOMEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);

    @Mock
    protected DOMEntityOwnershipService mockEos;
    @Mock
    protected DOMEntityOwnershipCandidateRegistration mockEntityCandReg;
    @Mock
    protected DOMEntityOwnershipCandidateRegistration mockDoubleEntityCandReg;
    @Mock
    protected DOMEntityOwnershipListenerRegistration mockEosEntityListReg;
    @Mock
    protected DOMEntityOwnershipListenerRegistration mockEosDoubleEntityListReg;

    protected DOMClusterSingletonServiceProviderImpl clusterSingletonServiceProvider;
    protected TestClusterSingletonService clusterSingletonService;
    protected TestClusterSingletonService clusterSingletonService2;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockEosEntityListReg).close();
        doNothing().when(mockEosDoubleEntityListReg).close();
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockDoubleEntityCandReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(SERVICE_ENTITY_TYPE),
                any(DOMClusterSingletonServiceProviderImpl.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_SERVICE_ENTITY_TYPE),
                any(DOMClusterSingletonServiceProviderImpl.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(ENTITY);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(DOUBLE_ENTITY);

        clusterSingletonServiceProvider = new DOMClusterSingletonServiceProviderImpl(mockEos);
        clusterSingletonServiceProvider.initializeProvider();
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

    static final DOMEntityOwnershipChange getEntityToMaster() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
    }

    static final DOMEntityOwnershipChange getEntityToSlave() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER);
    }

    static final DOMEntityOwnershipChange getInitEntityToSlave() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_CHANGED);
    }

    static final DOMEntityOwnershipChange getInitEntityToSlaveNoMaster() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_LOST_NO_OWNER);
    }

    static final DOMEntityOwnershipChange getDoubleEntityToMaster() {
        return new DOMEntityOwnershipChange(DOUBLE_ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
    }

    static final DOMEntityOwnershipChange getInitDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(DOUBLE_ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_CHANGED);
    }

    static final DOMEntityOwnershipChange getDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(DOUBLE_ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER);
    }

    static final DOMEntityOwnershipChange getEntityToJeopardy() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
    }

    /**
     * Test checks NullPointer for null {@link DOMEntityOwnershipService} input value.
     */
    @Test(expected = NullPointerException.class)
    public void initializationClusterSingletonServiceProviderNullInputTest() {
        new DOMClusterSingletonServiceProviderImpl(null).close();
    }

    /**
     * Test GoldPath for close {@link DOMClusterSingletonServiceProviderImpl}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceProviderTest() throws Exception {
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
    public void makeEntityClusterSingletonServiceProviderTest() {
        final DOMEntity testEntity = clusterSingletonServiceProvider.createEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
        assertEquals(ENTITY, testEntity);
        final DOMEntity testDbEn = clusterSingletonServiceProvider.createEntity(CLOSE_SERVICE_ENTITY_TYPE,
                SERVICE_NAME);
        assertEquals(DOUBLE_ENTITY, testDbEn);
    }

    /**
     * Test parser ServiceIdentifier from Entity.
     */
    @Test
    public void getIdentifierClusterSingletonServiceProviderTest() {
        final String entityIdentifier = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(ENTITY);
        assertEquals(SERVICE_NAME, entityIdentifier);
        final String doubleEntityId = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(DOUBLE_ENTITY);
        assertEquals(SERVICE_NAME, doubleEntityId);
    }

    /**
     * Test GoldPath for initialization {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void initializationClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void slaveInitClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE, but NO-MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void slaveInitNoMasterClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlaveNoMaster());
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void masterInitClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void masterInitSlaveDoubleCandidateClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void masterInitClusterSingletonServiceTwoServicesTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTwoAddDuringWaitPhaseServicesTest()
            throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks CandidateAlreadyRegisteredException processing in initialization phase.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test(expected = RuntimeException.class)
    public void initializationClusterSingletonServiceCandidateAlreadyRegistredTest()
            throws CandidateAlreadyRegisteredException {
        doThrow(CandidateAlreadyRegisteredException.class).when(mockEos).registerCandidate(ENTITY);
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNull(reg);
    }

    /**
     * Test GoldPath for lostLeadership during tryToTakeLeadership with ownership result MASTER
     * {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void lostLeadershipDuringTryToTakeLeadershipClusterSingletonServiceTest()
            throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for lostLeadership with ownership result MASTER-TO-SLAVE {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void lostLeadershipClusterSingletonServiceTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Slave Instance.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void inJeopardySlaveTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToJeopardy());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTowServicesTest() throws CandidateAlreadyRegisteredException {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationNoRoleTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationNoRoleTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
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
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationSlaveTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationSlaveTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
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
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
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
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void tryToTakeLeaderForClosedServiceRegistrationTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        assertNotNull(reg2);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        reg.close();
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
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
