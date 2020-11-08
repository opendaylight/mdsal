/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;

/*
 * Testing {@link DOMClusterSingletonServiceProviderImpl} implementation
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public final class DOMClusterSingletonServiceProviderAsyncImplTest extends AbstractDOMClusterServiceProviderTest {
    /*
     * Test implementation of {@link ClusterSingletonService}
     */
    public static class TestClusterSingletonAsyncServiceInstance extends TestClusterSingletonService {
        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            super.closeServiceInstance();

            final SettableFuture<Void> future = SettableFuture.create();
            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    future.set(null);
                }
            }, ASYNC_TIME_DELAY_MILLIS);
            return future;
        }
    }

    public static final long ASYNC_TIME_DELAY_MILLIS = 100L;
    public static Timer TIMER;

    @BeforeClass
    public static void asyncInitTest() {
        TIMER = new Timer();
    }

    @AfterClass
    public static void cleanTest() {
        TIMER.cancel();
    }

    @Override
    TestClusterSingletonService instantiateService() {
        return new TestClusterSingletonAsyncServiceInstance();
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
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getEntityToSlave());
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
        verify(mockDoubleEntityCandReg).close();
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEosEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
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
        assertNotNull(reg);
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getInitDoubleEntityToSlave());
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToSlave());
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
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void inJeopardyMasterTest() throws Exception {
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
        clusterSingletonServiceProvider.ownershipChanged(getEntityMasterJeopardy());
        assertEquals(TestClusterSingletonServiceState.STARTED, clusterSingletonService.getServiceState());
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityMasterJeopardy());
        assertEquals(TestClusterSingletonServiceState.DESTROYED, clusterSingletonService.getServiceState());
        Thread.sleep(ASYNC_TIME_DELAY_MILLIS * 2);
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
    public void closeClusterSingletonServiceRegistrationMasterTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
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
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterCloseWithNotificationTimesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
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
     * Test checks close processing for {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception if the condition does not meet
     */
    @Test
    public void closeClusterSingletonServiceRegistrationMasterCloseCoupleTimesTest() throws Exception {
        final ClusterSingletonServiceRegistration reg = clusterSingletonServiceProvider
                .registerClusterSingletonService(clusterSingletonService);
        assertNotNull(reg);
        assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        verify(mockEos).registerCandidate(ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(DOUBLE_ENTITY);
        clusterSingletonServiceProvider.ownershipChanged(getDoubleEntityToMaster());
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
