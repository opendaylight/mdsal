/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Assert;
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
 * Testing {@link DOMClusterSingletonServiceProviderImpl} implementation.
 */
public class DOMClusterSingletonServiceProviderImplTest {

    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";
    private static final String SERVICE_NAME = "testServiceName";

    @Mock
    private DOMEntityOwnershipService mockEos;
    @Mock
    private DOMEntityOwnershipCandidateRegistration mockEntityCandReg;
    @Mock
    private DOMEntityOwnershipCandidateRegistration mockDoubleEntityCandReg;
    @Mock
    private DOMEntityOwnershipListenerRegistration mockEosEntityListReg;
    @Mock
    private DOMEntityOwnershipListenerRegistration mockEosDoubleEntityListReg;

    private DOMClusterSingletonServiceProviderImpl clusterSingletonServiceProvider;
    private TestClusterSingletonServiceInstance clusterSingletonService;
    private TestClusterSingletonServiceInstance clusterSingletonService2;

    private final DOMEntity entity = new DOMEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
    private final DOMEntity doubleEntity = new DOMEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);

    /**
     * Initialization functionality for every Tests in this suite.
     *
     * @throws Exception if the condition does not meet
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockEosEntityListReg).close();
        doNothing().when(mockEosDoubleEntityListReg).close();
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockDoubleEntityCandReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(SERVICE_ENTITY_TYPE),
                any(DOMClusterSingletonServiceProviderImpl.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_SERVICE_ENTITY_TYPE),
                any(DOMClusterSingletonServiceProviderImpl.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(entity);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(doubleEntity);

        clusterSingletonServiceProvider = new DOMClusterSingletonServiceProviderImpl(mockEos);
        clusterSingletonServiceProvider.initializeProvider();
        verify(mockEos).registerListener(SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);
        verify(mockEos).registerListener(CLOSE_SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);

        clusterSingletonService = new TestClusterSingletonServiceInstance();
        clusterSingletonService2 = new TestClusterSingletonServiceInstance();

        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks NullPointer for null {@link DOMEntityOwnershipService} input value.
     *
     * @throws Exception if the condition does not meet
     */
    @Test(expected = NullPointerException.class)
    public void initializationClusterSingletonServiceProviderNullInputTest() throws Exception {
        clusterSingletonServiceProvider = new DOMClusterSingletonServiceProviderImpl(null);
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
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void makeEntityClusterSingletonServiceProviderTest() throws Exception {
        final DOMEntity testEntity = clusterSingletonServiceProvider.createEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
        Assert.assertEquals(entity, testEntity);
        final DOMEntity testDbEn = clusterSingletonServiceProvider.createEntity(CLOSE_SERVICE_ENTITY_TYPE,
                SERVICE_NAME);
        Assert.assertEquals(doubleEntity, testDbEn);
    }

    /**
     * Test parser ServiceIdentifier from Entity.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void getIdentifierClusterSingletonServiceProviderTest() throws Exception {
        final String entityIdentifier = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(entity);
        Assert.assertEquals(SERVICE_NAME, entityIdentifier);
        final String doubleEntityId = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(doubleEntity);
        Assert.assertEquals(SERVICE_NAME, doubleEntityId);
    }

    /**
     * Test GoldPath for initialization {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void initializationClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void slaveInitClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        verify(mockEos, never()).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result SLAVE, but NO-MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void slaveInitNoMasterClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlaveNoMaster());
        verify(mockEos, never()).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void masterInitClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void masterInitSlaveDoubleCandidateClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void masterInitClusterSingletonServiceTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTwoAddDuringWaitPhaseServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void takeLeadershipClusterSingletonServiceTowServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks CandidateAlreadyRegisteredException processing in initialization phase.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void initializationClusterSingletonServiceCandidateAlreadyRegistredTest() throws Exception {
        doThrow(CandidateAlreadyRegisteredException.class).when(mockEos).registerCandidate(entity);
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNull(reg);
    }

    /**
     * Test GoldPath for lostLeadership during tryToTakeLeadership with ownership result MASTER
     *     {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void lostLeadershipDuringTryToTakeLeadershipClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test GoldPath for lostLeadership with ownership result MASTER-TO-SLAVE {@link ClusterSingletonService}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void lostLeadershipClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks umexpected change for MASTER-TO-SLAVE double Candidate role change.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void unexpectedLostLeadershipDoubleCandidateTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
    }

    /**
     * Test checks inJeopardy Cluster Node state for Master Instance.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void inJeopardyMasterTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToJeopardy());
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
    }

    /**
     * Test checks inJeopardy Cluster Node state for Slave Instance.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void inJeopardySlaveTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        verify(mockEos, never()).registerCandidate(doubleEntity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToJeopardy());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
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
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationNoRoleTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationSlaveTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationSlaveTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(entity);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterTwoServicesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(entity);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
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
        Assert.assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(entity);
        reg.close();
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());
    }

    private DOMEntityOwnershipChange getEntityToMaster() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, true, true));
    }

    private DOMEntityOwnershipChange getEntityToSlave() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(true, false, true));
    }

    private DOMEntityOwnershipChange getInitEntityToSlave() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, true));
    }

    private DOMEntityOwnershipChange getInitEntityToSlaveNoMaster() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, false));
    }

    private DOMEntityOwnershipChange getDoubleEntityToMaster() {
        return new DOMEntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(false, true, true));
    }

    private DOMEntityOwnershipChange getInitDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(false, false, true));
    }

    private DOMEntityOwnershipChange getDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(true, false, true));
    }

    private DOMEntityOwnershipChange getEntityToJeopardy() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, false), true);
    }

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
        DESTROYED;
    }

    /**
     * Test implementation of {@link ClusterSingletonService}.
     */
    class TestClusterSingletonServiceInstance implements ClusterSingletonService {

        private final ServiceGroupIdentifier serviceIndent = ServiceGroupIdentifier.create(SERVICE_NAME);
        private TestClusterSingletonServiceState serviceState;

        TestClusterSingletonServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.INITIALIZED;
        }

        @Override
        public void instantiateServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.STARTED;
        }

        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.DESTROYED;
            return Futures.immediateFuture(null);
        }

        public TestClusterSingletonServiceState getServiceState() {
            return serviceState;
        }

        @Override
        public ServiceGroupIdentifier getIdentifier() {
            return serviceIndent;
        }
    }
}
