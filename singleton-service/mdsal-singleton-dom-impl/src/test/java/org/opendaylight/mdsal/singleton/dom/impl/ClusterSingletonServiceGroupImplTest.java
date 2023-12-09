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
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NEW_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NO_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_CHANGED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_LOST_NO_OWNER;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.SERVICE_ENTITY_TYPE;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.mdsal.singleton.dom.impl.util.TestEntity;
import org.opendaylight.mdsal.singleton.dom.impl.util.TestInstanceIdentifier;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Testing {@link ClusterSingletonServiceGroupImpl}.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClusterSingletonServiceGroupImplTest {
    public static final String SERVICE_IDENTIFIER = "TestServiceIdent";
    public static final ServiceGroupIdentifier SERVICE_GROUP_IDENT = ServiceGroupIdentifier.create(SERVICE_IDENTIFIER);

    public static final @NonNull TestEntity MAIN_ENTITY = new TestEntity(SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);
    public static final @NonNull TestEntity CLOSE_ENTITY =
        new TestEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);

    @Mock
    public ClusterSingletonService mockClusterSingletonService;
    @Mock
    public ClusterSingletonService mockClusterSingletonServiceSecond;
    @Mock
    public Registration mockEntityCandReg;
    @Mock
    public Registration mockCloseEntityCandReg;
    @Mock
    public GenericEntityOwnershipListener<TestEntity> mockEosListener;

    @Mock
    public GenericEntityOwnershipService<TestEntity, GenericEntityOwnershipListener<TestEntity>> mockEosService;

    public ClusterSingletonServiceGroupImpl<TestInstanceIdentifier, TestEntity,
        GenericEntityOwnershipListener<TestEntity>,
        GenericEntityOwnershipService<TestEntity, GenericEntityOwnershipListener<TestEntity>>> singletonServiceGroup;

    public ClusterSingletonServiceRegistration firstReg;
    public ClusterSingletonServiceRegistration secondReg;

    /**
     * Initialization functionality for every Tests in this suite.
     *
     * @throws CandidateAlreadyRegisteredException unexpected exception.
     */
    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        doReturn(mockEntityCandReg).when(mockEosService).registerCandidate(MAIN_ENTITY);
        doReturn(mockCloseEntityCandReg).when(mockEosService).registerCandidate(CLOSE_ENTITY);
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockCloseEntityCandReg).close();
        doNothing().when(mockClusterSingletonService).instantiateServiceInstance();
        doReturn(Futures.immediateFuture(null)).when(mockClusterSingletonService).closeServiceInstance();

        doReturn(SERVICE_GROUP_IDENT).when(mockClusterSingletonService).getIdentifier();
        doReturn(SERVICE_GROUP_IDENT).when(mockClusterSingletonServiceSecond).getIdentifier();

        firstReg = new AbstractClusterSingletonServiceRegistration(mockClusterSingletonService) {
            @Override
            protected void removeRegistration() {
                // No-op
            }
        };
        secondReg = new AbstractClusterSingletonServiceRegistration(mockClusterSingletonServiceSecond) {
            @Override
            protected void removeRegistration() {
                // No-op
            }
        };

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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NO_OWNER, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
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
        singletonServiceGroup.registerService(firstReg);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.registerService(secondReg);
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
        singletonServiceGroup.registerService(firstReg);
        assertNotNull(singletonServiceGroup.unregisterService(firstReg));
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.registerService(secondReg);
        assertNull(singletonServiceGroup.unregisterService(firstReg));
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_CHANGED, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.registerService(secondReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService).instantiateServiceInstance();

        // Base entity in jeopardy should not matter...
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, true);
        verify(mockClusterSingletonService, never()).closeServiceInstance();

        // ... application state is actually guarded by cleanup
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change.
     *     Not initialized provider has to close and remove all singletonServices from Group and
     *     Group itself remove too.
     */
    @Test(expected = IllegalStateException.class)
    public void tryToTakeLeaderForNotInitializedGroupTest() {
        singletonServiceGroup.registerService(firstReg);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService).instantiateServiceInstance();
        assertNotNull(singletonServiceGroup.unregisterService(firstReg));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NO_OWNER, false);
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockClusterSingletonService).closeServiceInstance();
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
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    @Test
    public void testRegisterCloseShutdown() throws CandidateAlreadyRegisteredException, InterruptedException,
            ExecutionException {
        initializeGroupAndStartService();

        assertNotNull(singletonServiceGroup.unregisterService(firstReg));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockEntityCandReg).close();

        final ListenableFuture<?> future = singletonServiceGroup.closeClusterSingletonGroup();
        assertNotNull(future);
        assertFalse(future.isDone());
        verify(mockClusterSingletonService).closeServiceInstance();

        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockCloseEntityCandReg).close();

        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertTrue(future.isDone());
        assertNull(future.get());
    }

    private void initialize() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
    }

    private void initializeGroupAndStartService() throws CandidateAlreadyRegisteredException {
        initialize();
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }
}
