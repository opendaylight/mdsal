/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api.clustering;

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
import org.opendaylight.mdsal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.common.api.clustering.ClusterSingletonService;
import org.opendaylight.mdsal.common.api.clustering.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipChangeState;

/**
 * Testing {@link ClusterSingletonServiceProviderImpl}
 */
public class ClusterSingletonServiceProviderImplTest {

    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";
    private static final String SERVICE_NAME = "testServiceName";

    @Mock
    private EntityOwnershipService mockEos;
    @Mock
    private EntityOwnershipCandidateRegistration mockEntityCandReg;
    @Mock
    private EntityOwnershipCandidateRegistration mockDoubleEntityCandReg;
    @Mock
    private EntityOwnershipListenerRegistration mockEosEntityListReg;
    @Mock
    private EntityOwnershipListenerRegistration mockEosDoubleEntityListReg;

    private ClusterSingletonServiceProviderImpl clusterSingletonServiceProvider;
    private TestClusterSingletonServiceInstance clusterSingletonService;
    private TestClusterSingletonServiceInstance clusterSingletonService2;

    private final Entity entity = new Entity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
    private final Entity doubleEntity = new Entity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);

    /**
     * Initialization functionality for every Tests in this suite
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockEosEntityListReg).close();
        doNothing().when(mockEosDoubleEntityListReg).close();
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockDoubleEntityCandReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(SERVICE_ENTITY_TYPE),
                any(ClusterSingletonServiceProviderImpl.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_SERVICE_ENTITY_TYPE),
                any(ClusterSingletonServiceProviderImpl.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(entity);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(doubleEntity);

        clusterSingletonServiceProvider = new ClusterSingletonServiceProviderImpl(mockEos);
        clusterSingletonServiceProvider.initializeProvider();
        verify(mockEos).registerListener(SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);
        verify(mockEos).registerListener(CLOSE_SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);

        clusterSingletonService = new TestClusterSingletonServiceInstance();
        clusterSingletonService2 = new TestClusterSingletonServiceInstance();

        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    /**
     * Test checks NullPointer for null {@link EntityOwnershipService} input value
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void initializationClusterSingletonServiceProviderNullInputTest() throws Exception {
        clusterSingletonServiceProvider = new ClusterSingletonServiceProviderImpl(null);
    }

    /**
     * Test GoldPath for close {@link ClusterSingletonServiceProviderImpl}
     *
     * @throws Exception
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
     * Test parser ServiceIdentifier from Entity
     *
     * @throws Exception
     */
    @Test
    public void makeEntityClusterSingletonServiceProviderTest() throws Exception {
        final Entity testEntity = clusterSingletonServiceProvider.createEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
        Assert.assertEquals(entity, testEntity);
        final Entity testDbEn = clusterSingletonServiceProvider.createEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);
        Assert.assertEquals(doubleEntity, testDbEn);
    }

    /**
     * Test parser ServiceIdentifier from Entity
     *
     * @throws Exception
     */
    @Test
    public void getIdentifierClusterSingletonServiceProviderTest() throws Exception {
        final String entityIdentifier = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(entity);
        Assert.assertEquals(SERVICE_NAME, entityIdentifier);
        final String doubleEntityId = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(doubleEntity);
        Assert.assertEquals(SERVICE_NAME, doubleEntityId);
    }

    /**
     * Test GoldPath for initialization {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for initialization with init ownership result SLAVE {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for initialization with init ownership result SLAVE, but NO-MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for initialization with init ownership result MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test checks CandidateAlreadyRegisteredException processing in initialization phase
     *
     * @throws Exception
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
     * {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test GoldPath for lostLeadership with ownership result MASTER-TO-SLAVE {@link ClusterSingletonService}
     *
     * @throws Exception
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
     * Test checks umexpected change for MASTER-TO-SLAVE double Candidate role change
     *
     * @throws Exception
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
     * Test checks inJeopardy Cluster Node state for Master Instance
     *
     * @throws Exception
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
     * Test checks inJeopardy Cluster Node state for Slave Instance
     *
     * @throws Exception
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
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
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
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
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
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
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
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
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
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
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
     * Test checks close procesing for {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
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
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
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

    private EntityOwnershipChange getEntityToMaster() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, true, true));
    }

    private EntityOwnershipChange getEntityToSlave() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(true, false, true));
    }

    private EntityOwnershipChange getInitEntityToSlave() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, true));
    }

    private EntityOwnershipChange getInitEntityToSlaveNoMaster() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, false));
    }

    private EntityOwnershipChange getDoubleEntityToMaster() {
        return new EntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(false, true, true));
    }

    private EntityOwnershipChange getInitDoubleEntityToSlave() {
        return new EntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(false, false, true));
    }

    private EntityOwnershipChange getDoubleEntityToSlave() {
        return new EntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(true, false, true));
    }

    private EntityOwnershipChange getEntityToJeopardy() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, false), true);
    }

    /**
     * Base states for AbstractClusterProjectProvider
     */
    static enum TestClusterSingletonServiceState {
        /**
         * State represents a correct Instantiated process
         */
        STARTED,
        /**
         * State represents a correct call abstract method instantiatingProject
         */
        INITIALIZED,
        /**
         * State represents a correct call abstract method destryingProject
         */
        DESTROYED;
    }

    /**
     * Test implementation of {@link ClusterSingletonService}
     */
    class TestClusterSingletonServiceInstance implements ClusterSingletonService {

        private static final String SERVICE_IDENT = SERVICE_NAME;
        private TestClusterSingletonServiceState serviceState;

        public TestClusterSingletonServiceInstance() {
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
        public String getServiceGroupIdentifier() {
            return SERVICE_IDENT;
        }
    }

}
