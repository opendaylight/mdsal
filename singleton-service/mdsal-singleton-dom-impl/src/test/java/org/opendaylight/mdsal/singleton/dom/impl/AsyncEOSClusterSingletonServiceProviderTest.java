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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NEW_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_CHANGED;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;

/*
 * Testing {@link DOMClusterSingletonServiceProviderImpl} implementation
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
final class AsyncEOSClusterSingletonServiceProviderTest extends AbstractEOSClusterSingletonServiceProviderTest {
    /*
     * Test implementation of {@link ClusterSingletonService}
     */
    private static final class TestClusterSingletonAsyncServiceInstance extends TestClusterSingletonService {
        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            super.closeServiceInstance();

            final var future = SettableFuture.<Void>create();
            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    future.set(null);
                }
            }, ASYNC_TIME_DELAY_MILLIS);
            return future;
        }
    }

    private static final long ASYNC_TIME_DELAY_MILLIS = 100L;
    private static Timer TIMER;

    @BeforeAll
    static void asyncInitTest() {
        TIMER = new Timer();
    }

    @AfterAll
    static void cleanTest() {
        TIMER.cancel();
    }

    @Override
    TestClusterSingletonService instantiateService() {
        return new TestClusterSingletonAsyncServiceInstance();
    }

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
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
        verify(mockDoubleEntityCandReg).close();
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
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
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg, never()).close();
        reg.close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
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
        clusterSingletonServiceProvider.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE, true);
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE, true);
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
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
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
        verify(mockDoubleEntityCandReg).close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
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
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
        verify(mockDoubleEntityCandReg).close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
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
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());

        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
    }
}
