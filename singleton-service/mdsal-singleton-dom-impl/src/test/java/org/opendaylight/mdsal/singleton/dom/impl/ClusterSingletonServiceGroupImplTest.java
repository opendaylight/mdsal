/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
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

    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";
    private static final String SERVICE_IDENTIFIER = "TestServiceIdent";
    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENT = ServiceGroupIdentifier.create(SERVICE_IDENTIFIER);

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
    private AbstractClusterSingletonServiceProviderImpl<TestInstanceIdentifier, TestEntity,
        GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>,
        GenericEntityOwnershipListener<TestInstanceIdentifier,
        GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>, ?, ?> mockParent;

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

    private final TestEntity mainEntity = new TestEntity(SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);
    private final TestEntity closeEntity = new TestEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);

    /**
     * Initialization functionality for every Tests in this suite.
     *
     * @throws CandidateAlreadyRegisteredException unexpected exception.
     */
    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        MockitoAnnotations.initMocks(this);

        doReturn(mockEntityCandReg).when(mockEosService).registerCandidate(mainEntity);
        doReturn(mockCloseEntityCandReg).when(mockEosService).registerCandidate(closeEntity);
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockCloseEntityCandReg).close();
        doNothing().when(mockClusterSingletonService).instantiateServiceInstance();
        doReturn(Futures.immediateFuture(null)).when(mockClusterSingletonService).closeServiceInstance();
        doNothing().when(mockParent).onGroupClosed(any(ClusterSingletonServiceGroupImpl.class));

        doReturn(SERVICE_GROUP_IDENT).when(mockClusterSingletonService).getIdentifier();
        doReturn(SERVICE_GROUP_IDENT).when(mockClusterSingletonServiceSecond).getIdentifier();

        singletonServiceGroup = new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, mainEntity, closeEntity,
            mockEosService, mockParent);
    }

    /**
     * Test NULL ServiceIdent input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullIdentTest() {
        new ClusterSingletonServiceGroupImpl<>(null, mainEntity, closeEntity, mockEosService, mockParent);
    }

    /**
     * Test empty ServiceIdent input for new ServiceGroup instance.
     */
    @Test(expected = IllegalArgumentException.class)
    public void instantiationClusterSingletonServiceGroupEmptyIdentTest() {
        new ClusterSingletonServiceGroupImpl<>("", mainEntity, closeEntity, mockEosService, mockParent);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullMainEntityTest() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, null, closeEntity, mockEosService, mockParent);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullCloseEntityTest() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, mainEntity, null, mockEosService, mockParent);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullEOS_Test() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, mainEntity, closeEntity, null, mockParent);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance.
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullMapRefTest() {
        new ClusterSingletonServiceGroupImpl<>(SERVICE_IDENTIFIER, mainEntity, closeEntity, mockEosService, null);
    }

    /**
     * Test GoldPath for initialization ServiceGroup.
     */
    @Test
    public void initializationClusterSingletonServiceGroupTest() throws CandidateAlreadyRegisteredException {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE entity Candidate role change.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void initializationSlaveTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE but without MASTER entity Candidate role change.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void initializationNoMasterTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlaveNoMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath for InJeopardy entity Candidate role change.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void initializationInJeopardyTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockParent, never()).onGroupClosed(any());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath for registration SingletonService.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void serviceRegistrationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
    }

    /**
     * Test GoldPath for registration SingletonService.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void serviceRegistrationClusterSingletonServiceGroupTwoServiceTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.registerService(mockClusterSingletonServiceSecond);
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     * without mastership and don't remove ServiceGroup from map.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void serviceUnregistrationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        assertTrue(singletonServiceGroup.unregisterService(mockClusterSingletonService));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     *     without mastership and don't remove ServiceGroup from map.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void serviceUnregistrationClusterSingletonServiceGroupTwoServicesTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.registerService(mockClusterSingletonServiceSecond);
        singletonServiceGroup.unregisterService(mockClusterSingletonService);
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    /**
     * Test GoldPath get Slave role for registered main entity.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void getSlaveClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered main entity.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void tryToTakeLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath get Master role for registered close entity.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void takeMasterClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered entity but initial Slave
     *     role for closeEntity.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void waitToTakeMasterClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getInitDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    /**
     * Test inJeopardy validation during wait phase for Master role for closeEntity.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void inJeopardyInWaitPhaseClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    /**
     * Test inJeopardy validation during wait phase for Master role for closeEntity.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void inJeopardyInWaitPhaseClusterSingletonServiceGroupTwoServiceTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.registerService(mockClusterSingletonServiceSecond);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    /**
     * Test inJeopardy validation for holding leadership.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void inJeopardyLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER entity Candidate role change.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void lostLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for SLAVE-TO-MASTER entity Candidate role change.
     *     Not initialized provider has to close and remove all singletonServices from Group and
     *     Group itself remove too.
     *
     * @throws Exception - unexpected exception
     */
    @Test(expected = RuntimeException.class)
    public void tryToTakeLeaderForNotInitializedGroupTest() throws Exception {
        singletonServiceGroup.registerService(mockClusterSingletonService);
        verify(mockParent).onGroupClosed(singletonServiceGroup);
    }

    /**
     * Test checks closing procesing for close {@link ClusterSingletonServiceRegistration}.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void checkClosingRegistrationTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.unregisterService(mockClusterSingletonService);
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToSlaveNoMaster());
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for MASTER-TO-SLAVE closeEntity Candidate role change.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void checkClosingUnexpectedDoubleEntityForMasterOwnershipChangeRegistrationTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    /**
     * Test checks validation Error processing for MASTER-TO-SLAVE closeEntity Candidate role change
     *     without closeEntity registration.
     *
     * @throws Exception - unexpected exception
     */
    @Test
    public void checkClosingUnexpectedDoubleEntityForSlaveOwnershipChangeRegistrationTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        singletonServiceGroup.registerService(mockClusterSingletonService);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockParent, never()).onGroupClosed(any());
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToMaster() {
        return new GenericEntityOwnershipChange<>(mainEntity, EntityOwnershipChangeState.from(false, true, true));
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToSlave() {
        return new GenericEntityOwnershipChange<>(mainEntity, EntityOwnershipChangeState.from(true, false, true));
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToSlaveNoMaster() {
        return new GenericEntityOwnershipChange<>(mainEntity, EntityOwnershipChangeState.from(true, false, false));
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getDoubleEntityToMaster() {
        return new GenericEntityOwnershipChange<>(closeEntity, EntityOwnershipChangeState.from(false, true, true));
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getDoubleEntityToSlave() {
        return new GenericEntityOwnershipChange<>(closeEntity, EntityOwnershipChangeState.from(true, false, true));
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getInitDoubleEntityToSlave() {
        return new GenericEntityOwnershipChange<>(closeEntity, EntityOwnershipChangeState.from(false, false, true));
    }

    private GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity> getEntityToJeopardy() {
        return new GenericEntityOwnershipChange<>(mainEntity,
            EntityOwnershipChangeState.from(false, false, false), true);
    }

}
