/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.clustering.util.TestEntity;
import org.opendaylight.mdsal.common.api.clustering.util.TestInstanceIdentifier;

/**
 * Testing {@link ClusterSingletonServiceGroupImpl}
 */
public class ClusterSingletonServiceGroupImplTest {

    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";
    private static final String SERVICE_IDENTIFIER = "TestServiceIdent";

    @Mock
    private ClusterSingletonService mockClusterSingletonService;
    @Mock
    private ClusterSingletonService mockClusterSingletonServiceSecond;
    @Mock
    private GenericEntityOwnershipCandidateRegistration<?, ?> mockEntityCandReg;
    @Mock
    private GenericEntityOwnershipCandidateRegistration<?, ?> mockCloseEntityCandReg;
    @Mock
    private GenericEntityOwnershipListener<TestInstanceIdentifier,GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>> mockEosListener;

    @Mock
    private GenericEntityOwnershipService<TestInstanceIdentifier,TestEntity, GenericEntityOwnershipListener<TestInstanceIdentifier,GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>> mockEosService;

    private ClusterSingletonServiceGroupImpl<TestInstanceIdentifier,TestEntity,GenericEntityOwnershipChange<TestInstanceIdentifier,TestEntity>,
                                         GenericEntityOwnershipListener<TestInstanceIdentifier, GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>,
                                         GenericEntityOwnershipService<TestInstanceIdentifier, TestEntity, GenericEntityOwnershipListener<TestInstanceIdentifier,
                                         GenericEntityOwnershipChange<TestInstanceIdentifier, TestEntity>>>> singletonServiceGroup;

    private final TestEntity mainEntity = new TestEntity(SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);
    private final TestEntity closeEntity = new TestEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_IDENTIFIER);
    private final ConcurrentMap<String, ClusterSingletonServiceGroup<?, ?, ?>> map = new ConcurrentHashMap<>();

    /**
     * Initialization functionality for every Tests in this suite
     *
     * @throws Exception
     */
   @Before
   public void setup() throws Exception {
       MockitoAnnotations.initMocks(this);

       doReturn(mockEntityCandReg).when(mockEosService).registerCandidate(mainEntity);
       doReturn(mockCloseEntityCandReg).when(mockEosService).registerCandidate(closeEntity);
       doNothing().when(mockEntityCandReg).close();
       doNothing().when(mockCloseEntityCandReg).close();
       doNothing().when(mockClusterSingletonService).instantiateServiceInstance();
       doReturn(Futures.immediateFuture(null)).when(mockClusterSingletonService).closeServiceInstance();

       doReturn(SERVICE_IDENTIFIER).when(mockClusterSingletonService).getServiceGroupIdentifier();
       doReturn(SERVICE_IDENTIFIER).when(mockClusterSingletonServiceSecond).getServiceGroupIdentifier();

       singletonServiceGroup = new ClusterSingletonServiceGroupImpl(SERVICE_IDENTIFIER, mainEntity, closeEntity, mockEosService, map);
    }

    /**
     * Test NULL ServiceIdent input for new ServiceGroup instance
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void instantiationClusterSingletonServiceGroupNullIdentTest() throws Exception {
        singletonServiceGroup = new ClusterSingletonServiceGroupImpl(null, mainEntity, closeEntity, mockEosService, map);
    }

    /**
     * Test empty ServiceIdent input for new ServiceGroup instance
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void instantiationClusterSingletonServiceGroupEmptyIdentTest() throws Exception {
        singletonServiceGroup = new ClusterSingletonServiceGroupImpl("", mainEntity, closeEntity, mockEosService, map);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullMainEntityTest() throws Exception {
        singletonServiceGroup = new ClusterSingletonServiceGroupImpl(SERVICE_IDENTIFIER, null, closeEntity, mockEosService, map);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullCloseEntityTest() throws Exception {
        singletonServiceGroup = new ClusterSingletonServiceGroupImpl(SERVICE_IDENTIFIER, mainEntity, null, mockEosService, map);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullEOS_Test() throws Exception {
        singletonServiceGroup = new ClusterSingletonServiceGroupImpl(SERVICE_IDENTIFIER, mainEntity, closeEntity, null, map);
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void instantiationClusterSingletonServiceGroupNullMapRefTest() throws Exception {
        singletonServiceGroup = new ClusterSingletonServiceGroupImpl(SERVICE_IDENTIFIER, mainEntity, closeEntity, mockEosService, null);
    }

    /**
     * Test GoldPath for initialization ServiceGroup
     *
     * @throws Exception
     */
    @Test
    public void initializationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void initializationSlaveTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE but without MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void initializationNoMasterTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToSlaveNoMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath for InJeopardy entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void initializationInJeopardyTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath for registration SingletonService
     *
     * @throws Exception
     */
    @Test
    public void serviceRegistrationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
    }

    /**
     * Test GoldPath for registration SingletonService
     *
     * @throws Exception
     */
    @Test
    public void serviceRegistrationClusterSingletonServiceGroupTwoServiceTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = singletonServiceGroup
                .registerService(mockClusterSingletonServiceSecond);
        Assert.assertNotNull(reg2);
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     * without mastership and don't remove ServiceGroup from map
     *
     * @throws Exception
     */
    @Test
    public void serviceUnregistrationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        reg.close();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     * without mastership and don't remove ServiceGroup from map
     *
     * @throws Exception
     */
    @Test
    public void serviceUnregistrationClusterSingletonServiceGroupTwoServicesTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = singletonServiceGroup
                .registerService(mockClusterSingletonServiceSecond);
        Assert.assertNotNull(reg2);
        reg.close();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNotNull(serviceGroup);
    }

    /**
     * Test GoldPath get Slave role for registered main entity
     *
     * @throws Exception
     */
    @Test
    public void getSlaveClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered main entity
     *
     * @throws Exception
     */
    @Test
    public void tryToTakeLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
    }

    /**
     * Test GoldPath get Master role for registered close entity
     *
     * @throws Exception
     */
    @Test
    public void takeMasterClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered entity but initial Slave
     * role for closeEntity
     *
     * @throws Exception
     */
    @Test
    public void waitToTakeMasterClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getInitDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNotNull(serviceGroup);
    }

    /**
     * Test inJeopardy validation during wait phase for Master role for closeEntity
     *
     * @throws Exception
     */
    @Test
    public void inJeopardyInWaitPhaseClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
    }

    /**
     * Test inJeopardy validation during wait phase for Master role for closeEntity
     *
     * @throws Exception
     */
    @Test
    public void inJeopardyInWaitPhaseClusterSingletonServiceGroupTwoServiceTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        final ClusterSingletonServiceRegistration reg2 = singletonServiceGroup
                .registerService(mockClusterSingletonServiceSecond);
        Assert.assertNotNull(reg2);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
    }

    /**
     * Test inJeopardy validation for holding leadership
     *
     * @throws Exception
     */
    @Test
    public void inJeopardyLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getEntityToJeopardy());
        verify(mockClusterSingletonService).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
    }

    /**
     * Test GoldPath for SLAVE-TO-MASTER entity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void lostLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
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
     * Not initialized provider has to close and remove all singletonServices from Group and
     * Group itself remove too.
     *
     * @throws Exception
     */
    @Test
    public void tryToTakeLeaderForNotInitializedGroupTest() throws Exception {
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNull(reg);
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
    }

    /**
     * Test checks closing procesing for close {@link ClusterSingletonServiceRegistration}
     *
     * @throws Exception
     */
    @Test
    public void checkClosingRegistrationTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        reg.close();
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test checks validation Error processing for MASTER-TO-SLAVE closeEntity Candidate role change
     *
     * @throws Exception
     */
    @Test
    public void checkClosingUnexpectedDoubleEntityForMasterOwnershipChangeRegistrationTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToMaster());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToMaster());
        verify(mockClusterSingletonService).instantiateServiceInstance();
        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        verify(mockClusterSingletonService).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
    }

    /**
     * Test checks validation Error processing for MASTER-TO-SLAVE closeEntity Candidate role change
     * without closeEntity registration
     *
     * @throws Exception
     */
    @Test
    public void checkClosingUnexpectedDoubleEntityForSlaveOwnershipChangeRegistrationTest() throws Exception {
        singletonServiceGroup.initializationClusterSingletonGroup();
        map.putIfAbsent(SERVICE_IDENTIFIER, singletonServiceGroup);
        verify(mockEosService).registerCandidate(mainEntity);
        final ClusterSingletonServiceRegistration reg = singletonServiceGroup
                .registerService(mockClusterSingletonService);
        Assert.assertNotNull(reg);
        singletonServiceGroup.ownershipChanged(getEntityToSlave());
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(closeEntity);
        singletonServiceGroup.ownershipChanged(getDoubleEntityToSlave());
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        final ClusterSingletonServiceGroup<?, ?, ?> serviceGroup = map.get(SERVICE_IDENTIFIER);
        Assert.assertNull(serviceGroup);
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
        return new GenericEntityOwnershipChange<>(mainEntity, EntityOwnershipChangeState.from(false, false, false), true);
    }

}
