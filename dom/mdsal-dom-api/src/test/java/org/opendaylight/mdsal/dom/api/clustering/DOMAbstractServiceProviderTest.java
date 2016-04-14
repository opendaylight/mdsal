/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api.clustering;

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
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipChangeState;

/**
 * Testing {@link DOMAbstractServiceProvider} implementation
 */
public class DOMAbstractServiceProviderTest {

    private static final String PROJECT_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_PROJECT_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private static final String SERVICE_NAME = "testServiceEntityName";

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

    private TestClusterProjectProvider testProjectProvider;
    private final DOMEntity entity = new DOMEntity(PROJECT_ENTITY_TYPE, SERVICE_NAME);
    private final DOMEntity doubleEntity = new DOMEntity(CLOSE_PROJECT_ENTITY_TYPE, SERVICE_NAME);

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
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(PROJECT_ENTITY_TYPE),
                any(DOMAbstractServiceProvider.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_PROJECT_ENTITY_TYPE),
                any(DOMAbstractServiceProvider.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(entity);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(doubleEntity);

        testProjectProvider = new TestClusterProjectProvider(mockEos, SERVICE_NAME);

        Assert.assertEquals(ClusterProjectProviderState.STARTED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test GoldPath for initialization ProjectProvider
     *
     * @throws Exception
     */
    @Test
    public void initializationClusterProjectProviderTest() throws Exception {
        testProjectProvider.initializeProvider();
        verify(mockEos).registerListener(PROJECT_ENTITY_TYPE, testProjectProvider);
        verify(mockEos).registerListener(CLOSE_PROJECT_ENTITY_TYPE, testProjectProvider);
        verify(mockEos).registerCandidate(entity);
        Assert.assertEquals(ClusterProjectProviderState.STARTED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks CandidateAlreadyRegisteredException processing in initialization phase
     *
     * @throws Exception
     */
    @Test
    public void initializationClusterProjectProviderCandidateAlreadyRegistredTest() throws Exception {
        doThrow(CandidateAlreadyRegisteredException.class).when(mockEos).registerCandidate(entity);
        testProjectProvider.initializeProvider();
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void initializationNoMasterTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToSlaveNoMaster());
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void tryToTakeLeaderTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        Assert.assertEquals(ClusterProjectProviderState.STARTED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void tryToTakeLeaderForNotInitializedProviderTest() throws Exception {
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        Assert.assertEquals(ClusterProjectProviderState.STARTED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void tryToTakeLeaderForClosedProviderTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.close();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER double Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void takeLeaderTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        testProjectProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(ClusterProjectProviderState.INITIALIZED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER double Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void takeLeaderForNotMasterProviderTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getDoubleEntityToMaster());
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void lostLeaderTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        testProjectProvider.ownershipChanged(getDoubleEntityToMaster());
        testProjectProvider.ownershipChanged(getEntityToSlave());
        verify(mockDoubleEntityCandReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER double Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void lostLeaderForClosedProviderTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        testProjectProvider.ownershipChanged(getDoubleEntityToMaster());
        testProjectProvider.close();
        testProjectProvider.ownershipChanged(getEntityToSlave());
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks umexpected change for MASTER-TO-SLAVE double Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void unexpectedLostLeadershipDoubleCandidateTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        testProjectProvider.ownershipChanged(getDoubleEntityToMaster());
        testProjectProvider.ownershipChanged(getDoubleEntityToSlave());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Master Instance
     *
     * @throws Exception
     */
    @Test
    public void inJeopardyMasterTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToMaster());
        verify(mockEos).registerCandidate(doubleEntity);
        testProjectProvider.ownershipChanged(getDoubleEntityToMaster());
        Assert.assertEquals(ClusterProjectProviderState.INITIALIZED, testProjectProvider.getTestProjectProviderState());
        testProjectProvider.ownershipChanged(getEntityToJeopardy());
        verify(mockEosEntityListReg, never()).close();
        verify(mockEosDoubleEntityListReg, never()).close();
        verify(mockEntityCandReg, never()).close();
        verify(mockDoubleEntityCandReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Slave Instance
     *
     * @throws Exception
     */
    @Test
    public void inJeopardySlaveTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToSlave());
        testProjectProvider.ownershipChanged(getEntityToJeopardy());
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        verify(mockEntityCandReg).close();
        verify(mockDoubleEntityCandReg, never()).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    private DOMEntityOwnershipChange getEntityToMaster() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, true, true));
    }

    private DOMEntityOwnershipChange getEntityToSlave() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(true, false, true));
    }

    private DOMEntityOwnershipChange getEntityToSlaveNoMaster() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(true, false, false));
    }

    private DOMEntityOwnershipChange getDoubleEntityToMaster() {
        return new DOMEntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(false, true, true));
    }

    private DOMEntityOwnershipChange getDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(true, false, true));
    }

    private DOMEntityOwnershipChange getEntityToJeopardy() {
        return new DOMEntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, false), true);
    }

    /**
     * Base states for AbstractClusterProjectProvider
     */
    public static enum ClusterProjectProviderState {
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
     * Test implementation of {@link AbstractClusterServiceProvider}
     */
    class TestClusterProjectProvider extends DOMAbstractServiceProvider {

        private ClusterProjectProviderState testProjectProviderState;

        public TestClusterProjectProvider(final DOMEntityOwnershipService entityOwnershipService,
                final String serviceIdentifier) {
            super(entityOwnershipService, serviceIdentifier);
            testProjectProviderState = ClusterProjectProviderState.STARTED;
        }

        @Override
        protected void instantiateServiceInstance() {
            testProjectProviderState = ClusterProjectProviderState.INITIALIZED;
        }

        @Override
        protected ListenableFuture<Void> closeServiceInstance() {
            testProjectProviderState = ClusterProjectProviderState.DESTROYED;
            return Futures.immediateFuture(null);
        }

        public ClusterProjectProviderState getTestProjectProviderState() {
            return testProjectProviderState;
        }
    }

}
