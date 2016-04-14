/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Testing {@link ClusterSingletonServiceRegistrationDelegator}
 */
public class ClusterSingletonServiceRegistrationDelegatorTest {

    private static final String SERVICE_IDENTIFIER = "TestServiceIdent";

    @Mock
    private ClusterSingletonServiceGroup<?, ?, ?> mockClusterSingletonServiceGroup;
    @Mock
    private ClusterSingletonService mockClusterSingletonService;

    private ClusterSingletonServiceRegistrationDelegator delegator;

    /**
     * Initialization functionality for every Tests in this suite
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(mockClusterSingletonServiceGroup)
                .unregisterService(any(ClusterSingletonServiceRegistrationDelegator.class));
        doReturn(SERVICE_IDENTIFIER).when(mockClusterSingletonService).getServiceGroupIdentifier();
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
     * Test a metod delegation {@link ClusterSingletonService#instantiateServiceInstance()}
     */
    @Test
    public void testInstatiateServiceDelegMethod() {
        delegator.instantiateServiceInstance();
        verify(mockClusterSingletonService).instantiateServiceInstance();
    }

    /**
     * Test a metod delegation {@link ClusterSingletonService#instantiateServiceInstance()}
     */
    @Test
    public void testCloseServiceDelegMethod() {
        delegator.closeServiceInstance();
        verify(mockClusterSingletonService).closeServiceInstance();
    }

    /**
     * Test a metod delegation {@link ClusterSingletonService#getServiceGroupIdentifier()}
     */
    @Test
    public void testGetServiceIdentifierDelegMethod() {
        final String serviceIdentifier = delegator.getServiceGroupIdentifier();
        Assert.assertEquals(SERVICE_IDENTIFIER, serviceIdentifier);
        verify(mockClusterSingletonService).getServiceGroupIdentifier();
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
