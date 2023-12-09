/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NEW_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_CHANGED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_LOST_NO_OWNER;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;

/**
 * Synchronous test suite.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SyncEOSClusterSingletonServiceProviderTest extends AbstractEOSClusterSingletonServiceProviderTest {
    /**
     * Test GoldPath for takeLeadership with ownership result MASTER {@link ClusterSingletonService}.
     */
    @Test
    void takeDoubleLeadershipClusterSingletonServiceTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
    }

    /**
     * Test checks unexpected change for MASTER-TO-SLAVE double Candidate role change.
     */
    @Test
    void unexpectedLostLeadershipDoubleCandidateTest() throws Exception {
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
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockEntityCandReg).close();
        verify(mockEosDoubleEntityListReg, never()).close();
    }

    /**
     * Test checks inJeopardy Cluster Node state for Master Instance.
     */
    @Test
    void inJeopardyMasterTest() throws Exception {
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
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    void closeClusterSingletonServiceRegistrationMasterTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    void closeClusterSingletonServiceRegistrationMasterCloseWithNotificationTimesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, atLeastOnce()).close();
        verify(mockDoubleEntityCandReg).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
    }

    /**
     * Test checks close processing for {@link ServiceRegistration}.
     */
    @Test
    void closeClusterSingletonServiceRegistrationMasterCloseCoupleTimesTest() throws Exception {
        final var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        reg.close();
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockEosDoubleEntityListReg, never()).close();
    }

    /**
     * Verify that closing a group does not prevent next incarnation of it to be registered and the next incarnation
     * will become active once the old incarnation finishes cleaning up.
     */
    @Test
    void testTwoIncarnations() throws Exception {
        var reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());

        // Close, triggers unregistration, but we will not continue with it.
        reg.close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());

        // Instantiate the next incarnation
        reg = clusterSingletonServiceProvider.registerClusterSingletonService(clusterSingletonService2);
        verify(mockEos).registerCandidate(MAIN_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());

        // Drive the old incarnation to closure, resetting mocks as needed
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockEosDoubleEntityListReg, never()).close();

        // Reset mocks for reuse. The next change should see the previous group terminate and the next incarnation
        // to start coming up
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockEos, times(2)).registerCandidate(MAIN_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEos, times(2)).registerCandidate(CLOSE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService2.getServiceState());

        // Check for potential service mixup
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());

        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
    }
}
