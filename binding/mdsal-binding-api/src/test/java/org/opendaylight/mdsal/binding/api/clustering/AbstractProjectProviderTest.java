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
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipChangeState;

/**
 * Testing {@link AbstractProjectProvider} implementation
 */
public class AbstractProjectProviderTest {

    private static final String PROJECT_ENTITY = "bundle";
    private static final String DOUBLE_CANDIDATE_PROJECT_ENTITY = "bundleDoubleCandidate";

    @Mock
    private EntityOwnershipService mockEos;
    @Mock
    private EntityOwnershipCandidateRegistration mockEntityReg;
    @Mock
    private EntityOwnershipCandidateRegistration mockDoubleEntityReg;
    @Mock
    private EntityOwnershipListenerRegistration mockEosEntityListReg;
    @Mock
    private EntityOwnershipListenerRegistration mockEosDoubleEntityListReg;

    private TestClusterProjectProvider testProjectProvider;
    private final Entity entity = new Entity(PROJECT_ENTITY, TestClusterProjectProvider.class.getCanonicalName());
    private final Entity doubleEntity = new Entity(DOUBLE_CANDIDATE_PROJECT_ENTITY,
            TestClusterProjectProvider.class.getCanonicalName());

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
        doNothing().when(mockEntityReg).close();
        doNothing().when(mockDoubleEntityReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(PROJECT_ENTITY),
                any(AbstractProjectProvider.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(DOUBLE_CANDIDATE_PROJECT_ENTITY),
                any(AbstractProjectProvider.class));
        doReturn(mockEntityReg).when(mockEos).registerCandidate(entity);
        doReturn(mockDoubleEntityReg).when(mockEos).registerCandidate(doubleEntity);

        testProjectProvider = new TestClusterProjectProvider(mockEos);
        verify(mockEos).registerListener(PROJECT_ENTITY, testProjectProvider);
        verify(mockEos).registerListener(DOUBLE_CANDIDATE_PROJECT_ENTITY, testProjectProvider);
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
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
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
        verify(mockDoubleEntityReg, never()).close();
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
        verify(mockDoubleEntityReg).close();
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
        verify(mockEntityReg).close();
        verify(mockDoubleEntityReg).close();
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
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        verify(mockEntityReg).close();
        verify(mockDoubleEntityReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Master Instance
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
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        verify(mockEntityReg).close();
        verify(mockDoubleEntityReg).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    /**
     * Test checks inJeopardy Cluster Node state for Slave Instance
     * @throws Exception
     */
    @Test
    public void inJeopardySlaveTest() throws Exception {
        testProjectProvider.initializeProvider();
        testProjectProvider.ownershipChanged(getEntityToSlave());
        testProjectProvider.ownershipChanged(getEntityToJeopardy());
        verify(mockEosEntityListReg).close();
        verify(mockEosDoubleEntityListReg).close();
        verify(mockEntityReg).close();
        verify(mockDoubleEntityReg, never()).close();
        Assert.assertEquals(ClusterProjectProviderState.DESTROYED, testProjectProvider.getTestProjectProviderState());
    }

    private EntityOwnershipChange getEntityToMaster() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, true, true), false);
    }

    private EntityOwnershipChange getEntityToSlave() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(true, false, true), false);
    }

    private EntityOwnershipChange getDoubleEntityToMaster() {
        return new EntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(false, true, true), false);
    }

    private EntityOwnershipChange getDoubleEntityToSlave() {
        return new EntityOwnershipChange(doubleEntity, EntityOwnershipChangeState.from(true, false, true), false);
    }

    private EntityOwnershipChange getEntityToJeopardy() {
        return new EntityOwnershipChange(entity, EntityOwnershipChangeState.from(false, false, false), true);
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
     * Test implementation of {@link AbstractClusterProjectProvider}
     */
    class TestClusterProjectProvider extends AbstractProjectProvider {

        private ClusterProjectProviderState testProjectProviderState;

        public TestClusterProjectProvider(final EntityOwnershipService entityOwnershipService) {
            super(entityOwnershipService);
            testProjectProviderState = ClusterProjectProviderState.STARTED;
        }

        @Override
        protected void instantiateProjectInstance() {
            testProjectProviderState = ClusterProjectProviderState.INITIALIZED;
        }

        @Override
        protected ListenableFuture<Void> closeProjectInstance() {
            testProjectProviderState = ClusterProjectProviderState.DESTROYED;
            return Futures.immediateFuture(null);
        }

        public ClusterProjectProviderState getTestProjectProviderState() {
            return testProjectProviderState;
        }
    }
}
