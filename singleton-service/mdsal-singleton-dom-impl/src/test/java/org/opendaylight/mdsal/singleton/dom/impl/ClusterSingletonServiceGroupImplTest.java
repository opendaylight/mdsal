/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.SERVICE_ENTITY_TYPE;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.mdsal.singleton.dom.impl.util.TestEntity;
import org.opendaylight.mdsal.singleton.dom.impl.util.TestInstanceIdentifier;

/**
 * Testing {@link ClusterSingletonServiceGroupImpl}.
 */
public class ClusterSingletonServiceGroupImplTest {
    private static final String SERVICE_IDENTIFIER = "TestServiceIdent";
    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENT = ServiceGroupIdentifier.create(SERVICE_IDENTIFIER);

    private static final TestEntity MAIN_ENTITY = new TestEntity(SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);
    private static final TestEntity CLOSE_ENTITY = new TestEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);

    @Mock
    private ClusterSingletonService mockClusterSingletonService;
    @Mock
    private ClusterSingletonService mockClusterSingletonServiceSecond;
    @Mock
    private GenericEntityOwnershipCandidateRegistration<?, ?> mockEntityCandReg;
    @Mock
    private GenericEntityOwnershipCandidateRegistration<?, ?> mockCloseEntityCandReg;
    @Mock
    private GenericEntityOwnershipListener<TestInstanceIdentifier,
        GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>> mockEosListener;

    @Mock
    private GenericEntityOwnershipService<TestInstanceIdentifier,TestEntity,
        GenericEntityOwnershipListener<TestInstanceIdentifier,
            GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>> mockEosService;

    private ClusterSingletonServiceGroupImpl<TestInstanceIdentifier,TestEntity,
        GenericEntityOwnershipChange<TestInstanceIdentifier,TestEntity>,
            GenericEntityOwnershipListener<TestInstanceIdentifier,
                GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>,
                    GenericEntityOwnershipService<TestInstanceIdentifier, TestEntity,
                        GenericEntityOwnershipListener<TestInstanceIdentifier,
                            GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>>> singletonServiceGroup;

    /**
     * Initialization functionality for every Tests in this suite.
     *
     * @throws CandidateAlreadyRegisteredException unexpected exception.
     */
    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        MockitoAnnotations.initMocks(this);

        doReturn(mockEntityCandReg).when(mockEosService).registerCandidate(MAIN_ENTITY);
        doReturn(mockCloseEntityCandReg).when(mockEosService).registerCandidate(CLOSE_ENTITY);
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockCloseEntityCandReg).close();
        doNothing().when(mockClusterSingletonService).instantiateServiceInstance();
        doReturn(Futures.immediateFuture(null)).when(mockClusterSingletonService).closeServiceInstance();

        doReturn(SERVICE_GROUP_IDENT).when(mockClusterSingletonService).getIdentifier();
        doReturn(SERVICE_GROUP_IDENT).when(mockClusterSingletonServiceSecond).getIdentifier();

        singletonServiceGroup = new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, MAIN_ENTITY, CLOSE_ENTITY,
            mockEosService);
    }

    /**
     * Test NULL ServiceIdent input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullIdentTest() {
        new ClusterSingletonServiceGroupImpl<>(null, MAIN_ENTITY, CLOSE_ENTITY, mockEosService);
    }

    /**
     * Test empty ServiceIdent input for new ServiceGroup instance.
     */
    @Test(expected = IllegalArgumentException.class)
    public void instantiationClusterSingletonServiceGroupEmptyIdentTest() {
        new ClusterSingletonServiceGroupImpl<>("", MAIN_ENTITY, CLOSE_ENTITY, mockEosService);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullMainEntityTest() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, null, CLOSE_ENTITY, mockEosService);
    }

    /**
     * Test NULL CloseEntity input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullCloseEntityTest() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, MAIN_ENTITY, null, mockEosService);
    }

    /**
     * Test NULL EntityOwnershipService input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullEOS_Test() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, MAIN_ENTITY, CLOSE_ENTITY, null);
    }

    /**
     * Test GoldPath for initialization ServiceGroup.
     */
    @Test
    public void initializationClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE entity Candidate role change.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void initializationSlaveTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE but without MASTER entity Candidate role change.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void initializationNoMasterTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlaveNoMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath for InJeopardy entity Candidate role change.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void initializationInJeopardyTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath for registration SingletonService.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void serviceRegistrationClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
    }

    /**
     * Test GoldPath for registration SingletonService.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void serviceRegistrationClusterSingletonServiceGroupTwoServiceTest()
            throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.registerService(mockClusterSingletonServiceSecond);
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     * without mastership and don't remove ServiceGroup from map.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void serviceUnregistrationClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        assertTrue(singletonServiceGroup.unregisterService(mockClusterSingletonService));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     *     without mastership and don't remove ServiceGroup from map.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void serviceUnregistrationClusterSingletonServiceGroupTwoServicesTest()
            throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.registerService(mockClusterSingletonServiceSecond);
        assertFalse(singletonServiceGroup.unregisterService(mockClusterSingletonService));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test GoldPath get Slave role for registered main entity.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void getSlaveClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered main entity.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void tryToTakeLeaderClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath get Master role for registered close entity.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void takeMasterClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered entity but initial Slave
     *     role for closeEntity.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void waitToTakeMasterClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getInitDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test inJeopardy validation during wait phase for Master role for closeEntity.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void inJeopardyInWaitPhaseClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test inJeopardy validation during wait phase for Master role for closeEntity.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void inJeopardyInWaitPhaseClusterSingletonServiceGroupTwoServiceTest()
            throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.registerService(mockClusterSingletonServiceSecond);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test inJeopardy validation for holding leadership.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void inJeopardyLeaderClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER entity Candidate role change.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void lostLeaderClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change.
     *     Not initialized provider has to close and remove all singletonServices from Group and
     *     Group itself remove too.
     */
    @Test(expected = RuntimeException.class)
    public void tryToTakeLeaderForNotInitializedGroupTest() {
        singletonServiceGroup.registerService(mockClusterSingletonService);
    }

    /**
     * Test checks closing processing for close {@link ClusterSingletonServiceRegistration}.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void checkClosingRegistrationTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        assertTrue(singletonServiceGroup.unregisterService(mockClusterSingletonService));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToSlaveNoMaster());
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for MASTER-TO-SLAVE closeEntity Candidate role change.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void checkClosingUnexpectedDoubleEntityForMasterOwnershipChangeRegistrationTest()
            throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for MASTER-TO-SLAVE closeEntity Candidate role change
     *     without closeEntity registration.
     *
     * @throws CandidateAlreadyRegisteredException - unexpected exception
     */
    @Test
    public void checkClosingUnexpectedDoubleEntityForSlaveOwnershipChangeRegistrationTest()
            throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    @Test
    public void testRegisterCloseShutdown() throws CandidateAlreadyRegisteredException, InterruptedException,
            ExecutionException {
        initializeGroupAndStartService();

        assertTrue(singletonServiceGroup.unregisterService(mockClusterSingletonService));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockEntityCandReg, never()).close();

        final ListenableFuture<?> future = singletonServiceGroup.closeClusterSingletonGroup();
        assertNotNull(future);
        assertFalse(future.isDone());
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockEntityCandReg).close();

        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService).closeServiceInstance();
        verify(mockCloseEntityCandReg).close();

        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        assertTrue(future.isDone());
        assertNull(future.get());
    }

    private void initialize() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
    }

    private void initializeGroupAndStartService() throws CandidateAlreadyRegisteredException {
        initialize();
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToMaster() {
        return new GenericEntityOwnershipChange<>(MAIN_ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToSlave() {
        return new GenericEntityOwnershipChange<>(MAIN_ENTITY,
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER);
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToSlaveNoMaster() {
        return new GenericEntityOwnershipChange<>(MAIN_ENTITY,
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER);
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getDoubleEntityToMaster() {
        return new GenericEntityOwnershipChange<>(CLOSE_ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getDoubleEntityToSlave() {
        return new GenericEntityOwnershipChange<>(CLOSE_ENTITY,
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER);
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getInitDoubleEntityToSlave() {
        return new GenericEntityOwnershipChange<>(CLOSE_ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_CHANGED);
    }

    private static GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToJeopardy() {
        return new GenericEntityOwnershipChange<>(MAIN_ENTITY,
                EntityOwnershipChangeState.REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
    }
}
