/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
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
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
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
}
