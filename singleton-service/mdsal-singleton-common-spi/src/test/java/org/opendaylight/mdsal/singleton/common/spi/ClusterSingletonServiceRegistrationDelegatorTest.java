/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.common.spi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import com.google.common.util.concurrent.Futures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

/**
 * Testing {@link ClusterSingletonServiceRegistrationDelegator}
 */
public class ClusterSingletonServiceRegistrationDelegatorTest {

    private static final String SERVICE_IDENTIFIER_NAME = "TestServiceIdent";
    private static final ServiceGroupIdentifier SERVICE_IDENTIFIER = ServiceGroupIdentifier
            .create(SERVICE_IDENTIFIER_NAME);

    @Mock
    private ClusterSingletonServiceGroup<?, ?, ?> mockClusterSingletonServiceGroup;
    @Mock
    private ClusterSingletonService mockClusterSingletonService;

    private ClusterSingletonServiceRegistrationDelegator delegator;

    /**
     * Initialization functionality for every Tests in this suite
     *
     * @throws Exception - unexpected setup exception
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockClusterSingletonServiceGroup)
                .unregisterService(any(ClusterSingletonServiceRegistrationDelegator.class));
        doReturn(SERVICE_IDENTIFIER).when(mockClusterSingletonService).getIdentifier();
        doNothing().when(mockClusterSingletonService).instantiateServiceInstance();
        doReturn(Futures.immediateFuture(null)).when(mockClusterSingletonService).closeServiceInstance();
        delegator = new ClusterSingletonServiceRegistrationDelegator(mockClusterSingletonService,
                mockClusterSingletonServiceGroup);
    }

    /**
     * Test create input with {@link ClusterSingletonService} as null
     */
    @Test(expected = NullPointerException.class)
    public void testSetupNullService() {
        delegator = new ClusterSingletonServiceRegistrationDelegator(null, mockClusterSingletonServiceGroup);
    }

    /**
     * Test create input with {@link ClusterSingletonServiceGroupImpl} as null
     */
    @Test(expected = NullPointerException.class)
    public void testSetupNullGroup() {
        delegator = new ClusterSingletonServiceRegistrationDelegator(mockClusterSingletonService, null);
    }

    /**
     * Test a method delegation {@link ClusterSingletonService#instantiateServiceInstance()}
     */
    @Test
    public void testInstatiateServiceDelegMethod() {
        delegator.instantiateServiceInstance();
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }

    /**
     * Test a method delegation {@link ClusterSingletonService#instantiateServiceInstance()}
     */
    @Test
    public void testCloseServiceDelegMethod() {
        delegator.closeServiceInstance();
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test a method delegation {@link ClusterSingletonService#getIdentifier()}
     */
    @Test
    public void testGetServiceIdentifierDelegMethod() {
        final String serviceIdentifier = delegator.getServiceGroupIdentifier();
        Assert.assertEquals(SERVICE_IDENTIFIER_NAME, serviceIdentifier);
        verify(mockClusterSingletonService).getIdentifier();
    }

    /**
     * Test a close method delegation to {@link ClusterSingletonServiceGroupImpl#unregisterService(ClusterSingletonService)}
     *
     * @throws Exception is from AutoclosableInterface
     */
    @Test
    public void testCloseMethod() throws Exception {
        delegator.close();
        verify(mockClusterSingletonServiceGroup).unregisterService(delegator);
    }
}
