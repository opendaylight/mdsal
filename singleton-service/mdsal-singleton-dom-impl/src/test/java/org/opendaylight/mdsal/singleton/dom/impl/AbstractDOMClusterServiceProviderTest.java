/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.AbstractClusterSingletonServiceProviderImpl.SERVICE_ENTITY_TYPE;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

/**
 * Abstract {@link DOMClusterSingletonServiceProviderImpl} testing substrate.
 */
abstract class AbstractDOMClusterServiceProviderTest {
    /**
     * Base states for AbstractClusterProjectProvider.
     */
    enum TestClusterSingletonServiceState {
        /**
         * State represents a correct Instantiated process.
         */
        STARTED,
        /**
         * State represents a correct call abstract method instantiatingProject.
         */
        INITIALIZED,
        /**
         * State represents a correct call abstract method destryingProject.
         */
        DESTROYED,
    }

    static class TestClusterSingletonService implements ClusterSingletonService {
        private static final ServiceGroupIdentifier SERVICE_ID = ServiceGroupIdentifier.create(SERVICE_NAME);

        private TestClusterSingletonServiceState serviceState = TestClusterSingletonServiceState.INITIALIZED;

        @Override
        public final ServiceGroupIdentifier getIdentifier() {
            return SERVICE_ID;
        }

        @Override
        public final void instantiateServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.STARTED;
        }

        final TestClusterSingletonServiceState getServiceState() {
            return serviceState;
        }

        @Override
        public ListenableFuture<Void> closeServiceInstance() {
            this.serviceState = TestClusterSingletonServiceState.DESTROYED;
            return Futures.immediateFuture(null);
        }
    }


    static final String SERVICE_NAME = "testServiceName";

    static final DOMEntity ENTITY = new DOMEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
    static final DOMEntity DOUBLE_ENTITY = new DOMEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);

    @Mock
    protected DOMEntityOwnershipService mockEos;
    @Mock
    protected DOMEntityOwnershipCandidateRegistration mockEntityCandReg;
    @Mock
    protected DOMEntityOwnershipCandidateRegistration mockDoubleEntityCandReg;
    @Mock
    protected DOMEntityOwnershipListenerRegistration mockEosEntityListReg;
    @Mock
    protected DOMEntityOwnershipListenerRegistration mockEosDoubleEntityListReg;

    protected DOMClusterSingletonServiceProviderImpl clusterSingletonServiceProvider;
    protected TestClusterSingletonService clusterSingletonService;
    protected TestClusterSingletonService clusterSingletonService2;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockEosEntityListReg).close();
        doNothing().when(mockEosDoubleEntityListReg).close();
        doNothing().when(mockEntityCandReg).close();
        doNothing().when(mockDoubleEntityCandReg).close();
        doReturn(mockEosEntityListReg).when(mockEos).registerListener(eq(SERVICE_ENTITY_TYPE),
                any(DOMClusterSingletonServiceProviderImpl.class));
        doReturn(mockEosDoubleEntityListReg).when(mockEos).registerListener(eq(CLOSE_SERVICE_ENTITY_TYPE),
                any(DOMClusterSingletonServiceProviderImpl.class));
        doReturn(mockEntityCandReg).when(mockEos).registerCandidate(ENTITY);
        doReturn(mockDoubleEntityCandReg).when(mockEos).registerCandidate(DOUBLE_ENTITY);

        clusterSingletonServiceProvider = new DOMClusterSingletonServiceProviderImpl(mockEos);
        clusterSingletonServiceProvider.initializeProvider();
        verify(mockEos).registerListener(SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);
        verify(mockEos).registerListener(CLOSE_SERVICE_ENTITY_TYPE, clusterSingletonServiceProvider);

        clusterSingletonService = instantiateService();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService.getServiceState());
        clusterSingletonService2 = instantiateService();
        Assert.assertEquals(TestClusterSingletonServiceState.INITIALIZED, clusterSingletonService2.getServiceState());
    }

    TestClusterSingletonService instantiateService() {
        return new TestClusterSingletonService();
    }

    static final DOMEntityOwnershipChange getEntityToMaster() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
    }

    static final DOMEntityOwnershipChange getEntityToSlave() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER);
    }

    static final DOMEntityOwnershipChange getInitEntityToSlave() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_CHANGED);
    }

    static final DOMEntityOwnershipChange getInitEntityToSlaveNoMaster() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_LOST_NO_OWNER);
    }

    static final DOMEntityOwnershipChange getDoubleEntityToMaster() {
        return new DOMEntityOwnershipChange(DOUBLE_ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
    }

    static final DOMEntityOwnershipChange getInitDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(DOUBLE_ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_CHANGED);
    }

    static final DOMEntityOwnershipChange getDoubleEntityToSlave() {
        return new DOMEntityOwnershipChange(DOUBLE_ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER);
    }

    static final DOMEntityOwnershipChange getEntityToJeopardy() {
        return new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.REMOTE_OWNERSHIP_LOST_NO_OWNER, true);
    }
}
