/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.SERVICE_ENTITY_TYPE;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;

/**
 * Synchronous test suite.
 */
public class DOMClusterSingletonServiceProviderImplTest extends AbstractDOMClusterServiceProviderTest {
    /**
     * Initialization functionality for every Tests in this suite.
     *
     * @throws CandidateAlreadyRegisteredException if the condition does not meet
     */
    @Override
    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        super.setup();
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
        Assert.assertEquals(ENTITY, testEntity);
        final DOMEntity testDbEn = clusterSingletonServiceProvider.createEntity(CLOSE_SERVICE_ENTITY_TYPE,
                SERVICE_NAME);
        Assert.assertEquals(DOUBLE_ENTITY, testDbEn);
    }

    /**
     * Test parser ServiceIdentifier from Entity.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void getIdentifierClusterSingletonServiceProviderTest() throws Exception {
        final String entityIdentifier = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(ENTITY);
        Assert.assertEquals(SERVICE_NAME, entityIdentifier);
        final String doubleEntityId = clusterSingletonServiceProvider.getServiceIdentifierFromEntity(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlaveNoMaster());
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
    public void takeDoubleLeadershipClusterSingletonServiceTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
    @Test(expected = RuntimeException.class)
    public void initializationClusterSingletonServiceCandidateAlreadyRegistredTest() throws Exception {
        doThrow(CandidateAlreadyRegisteredException.class).when(mockEos).registerCandidate(ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks unexpected change for MASTER-TO-SLAVE double Candidate role change.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void unexpectedLostLeadershipDoubleCandidateTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToSlave());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg, atLeastOnce()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getInitEntityToSlave());
        verify(mockEos, never()).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterCloseWithNotificationTimesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg, atLeastOnce()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterCloseCoupleTimesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        Assert.assertNotNull(reg);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg, atLeastOnce()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        Assert.assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
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
        Assert.assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService2);
        Assert.assertNotNull(reg2);
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
        verify(mockEos).registerCandidate(ENTITY);
        reg.close();
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
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
}
