/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NEW_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NO_OWNER;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_CHANGED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.REMOTE_OWNERSHIP_LOST_NO_OWNER;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Testing {@link ActiveServiceGroup}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActiveServiceGroupTest extends AbstractTest {
    @Mock
    private ClusterSingletonService mockClusterSingletonService;
    @Mock
    private ClusterSingletonService mockClusterSingletonServiceSecond;
    @Mock
    private Registration mockEntityCandReg;
    @Mock
    private Registration mockCloseEntityCandReg;
    @Mock
    private DOMEntityOwnershipListener mockEosListener;
    @Mock
    private DOMEntityOwnershipService mockEosService;

    private ActiveServiceGroup singletonServiceGroup;
    private ServiceRegistration firstReg;
    private ServiceRegistration secondReg;

    /**
     * Initialization functionality for every Tests in this suite.
     */
    @BeforeEach
    void setup() throws Exception {
        doReturn(mockEntityCandReg).when(mockEosService).registerCandidate(MAIN_ENTITY);
        doReturn(mockCloseEntityCandReg).when(mockEosService).registerCandidate(CLOSE_ENTITY);
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockCloseEntityCandReg).close();
        doNothing().when(mockClusterSingletonService).instantiateServiceInstance();
        doReturn(Futures.immediateFuture(null)).when(mockClusterSingletonService).closeServiceInstance();

        doReturn(SERVICE_ID).when(mockClusterSingletonService).getIdentifier();

        firstReg = new ServiceRegistration(mockClusterSingletonService) {
            @Override
            protected void removeRegistration() {
                // No-op
            }
        };
        secondReg = new ServiceRegistration(mockClusterSingletonServiceSecond) {
            @Override
            protected void removeRegistration() {
                // No-op
            }
        };

        singletonServiceGroup = new ActiveServiceGroup(SERVICE_NAME, MAIN_ENTITY, CLOSE_ENTITY, mockEosService);
    }

    private void mockSecond() {
        doReturn(SERVICE_ID).when(mockClusterSingletonServiceSecond).getIdentifier();
    }

    /**
     * Test GoldPath for initialization ServiceGroup.
     */
    @Test
    void initializationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE entity Candidate role change.
     */
    @Test
    void initializationSlaveTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath for NO-TO-SLAVE but without MASTER entity Candidate role change.
     */
    @Test
    void initializationNoMasterTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NO_OWNER, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath for InJeopardy entity Candidate role change.
     */
    @Test
    void initializationInJeopardyTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService, never()).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath for registration SingletonService.
     */
    @Test
    void serviceRegistrationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
    }

    /**
     * Test GoldPath for registration SingletonService.
     */
    @Test
    void serviceRegistrationClusterSingletonServiceGroupTwoServiceTest() throws Exception {
        mockSecond();

        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.registerService(secondReg);
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     * without mastership and don't remove ServiceGroup from map.
     */
    @Test
    void serviceUnregistrationClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        assertNotNull(singletonServiceGroup.unregisterService(firstReg));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test GoldPath for unregistration SingletonService don't call closeServiceInstance
     *     without mastership and don't remove ServiceGroup from map.
     */
    @Test
    void serviceUnregistrationClusterSingletonServiceGroupTwoServicesTest() throws Exception {
        mockSecond();

        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.registerService(secondReg);
        assertNull(singletonServiceGroup.unregisterService(firstReg));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
    }

    /**
     * Test GoldPath get Slave role for registered main entity.
     */
    @Test
    void getSlaveClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
    }

    /**
     * Test GoldPath get Master role for registered main entity.
     */
    @Test
    void tryToTakeLeaderClusterSingletonServiceGroupTest() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService, never()).instantiateServiceInstance();
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
    }

    /**
     * Test GoldPath get Master role for registered close entity.
     */
    @Test
    void takeMasterClusterSingletonServiceGroupTest() throws Exception {
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
     */
    @Test
    void waitToTakeMasterClusterSingletonServiceGroupTest() throws Exception {
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
     */
    @Test
    void inJeopardyInWaitPhaseClusterSingletonServiceGroupTest() throws Exception {
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
     */
    @Test
    void inJeopardyInWaitPhaseClusterSingletonServiceGroupTwoServiceTest() throws Exception {
        mockSecond();

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
     */
    @Test
    void inJeopardyLeaderClusterSingletonServiceGroupTest() throws Exception {
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
     */
    @Test
    void lostLeaderClusterSingletonServiceGroupTest() throws Exception {
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
    @Test
    void tryToTakeLeaderForNotInitializedGroupTest() {
        assertThrows(IllegalStateException.class, () -> singletonServiceGroup.registerService(firstReg));
    }

    /**
     * Test checks closing processing for close {@link ServiceRegistration}.
     */
    @Test
    void checkClosingRegistrationTest() throws Exception {
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
     */
    @Test
    void checkClosingUnexpectedDoubleEntityForMasterOwnershipChangeRegistrationTest() throws Exception {
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
     */
    @Test
    void checkClosingUnexpectedDoubleEntityForSlaveOwnershipChangeRegistrationTest() throws Exception {
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
    void testRegisterCloseShutdown() throws Exception {
        initializeGroupAndStartService();

        assertNotNull(singletonServiceGroup.unregisterService(firstReg));
        verify(mockClusterSingletonService, never()).closeServiceInstance();
        verify(mockEntityCandReg).close();

        final var future = singletonServiceGroup.closeClusterSingletonGroup();
        assertNotNull(future);
        assertFalse(future.isDone());
        verify(mockClusterSingletonService).closeServiceInstance();

        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        verify(mockCloseEntityCandReg).close();

        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_LOST_NEW_OWNER, false);
        assertTrue(future.isDone());
        assertNull(future.get());
    }

    private void initialize() throws Exception {
        singletonServiceGroup.initialize();
        verify(mockEosService).registerCandidate(MAIN_ENTITY);
    }

    private void initializeGroupAndStartService() throws Exception {
        initialize();
        singletonServiceGroup.registerService(firstReg);
        singletonServiceGroup.ownershipChanged(MAIN_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockEosService).registerCandidate(CLOSE_ENTITY);
        singletonServiceGroup.ownershipChanged(CLOSE_ENTITY, LOCAL_OWNERSHIP_GRANTED, false);
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }
}
