/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

/**
 * Integration test with simple (single-node) DOM EntityOwnershipService.
 */
public class ClusterSingletonServiceIntegrationTest {
    private static final ServiceGroupIdentifier GROUP_IDENTIFIER = ServiceGroupIdentifier.create("service1");

    private ClusterSingletonServiceProvider singleton;
    private DOMEntityOwnershipService eos;
    @Mock
    private ClusterSingletonService service1;
    @Mock
    private ClusterSingletonService service2;
    @Mock
    private ClusterSingletonService service3;
    @Mock
    private ClusterSingletonService service4;

    private SettableFuture<Void> serviceFuture1;
    private SettableFuture<Void> serviceFuture2;

    @Before
    public void setup() {
        initMocks(this);

        doReturn(GROUP_IDENTIFIER).when(service1).getIdentifier();
        serviceFuture1 = SettableFuture.create();
        doReturn(serviceFuture1).when(service1).closeServiceInstance();
        doNothing().when(service1).instantiateServiceInstance();

        doReturn(GROUP_IDENTIFIER).when(service2).getIdentifier();
        serviceFuture2 = SettableFuture.create();
        doReturn(serviceFuture2).when(service2).closeServiceInstance();
        doNothing().when(service2).instantiateServiceInstance();

        eos = new SimpleDOMEntityOwnershipService();
        final DOMClusterSingletonServiceProviderImpl impl = new DOMClusterSingletonServiceProviderImpl(eos);
        impl.initializeProvider();
        singleton = impl;
    }

    @After
    public void tearDown() throws Exception {
        singleton.close();
    }

    @Test
    public void testServiceBringupImmediate() throws Exception {
        ClusterSingletonServiceRegistration reg = singleton.registerClusterSingletonService(service1);
        assertNotNull(reg);
        verify(service1).instantiateServiceInstance();
        serviceFuture1.set(null);
        reg.close();
        verify(service1).closeServiceInstance();

        reg = singleton.registerClusterSingletonService(service2);
        assertNotNull(reg);
        verify(service2).instantiateServiceInstance();
        serviceFuture2.set(null);
        reg.close();
        verify(service2).closeServiceInstance();
    }

    @Test
    public void testServiceBringupPostClose() throws Exception {
        ClusterSingletonServiceRegistration reg = singleton.registerClusterSingletonService(service1);
        assertNotNull(reg);
        verify(service1).instantiateServiceInstance();
        reg.close();
        verify(service1).closeServiceInstance();
        serviceFuture1.set(null);

        reg = singleton.registerClusterSingletonService(service2);
        assertNotNull(reg);
        verify(service2).instantiateServiceInstance();
        reg.close();
        verify(service2).closeServiceInstance();
        serviceFuture2.set(null);
    }

    @Test
    public void testServiceBringupPostSecond() throws Exception {
        ClusterSingletonServiceRegistration reg = singleton.registerClusterSingletonService(service1);
        assertNotNull(reg);
        verify(service1).instantiateServiceInstance();
        reg.close();
        verify(service1).closeServiceInstance();

        reg = singleton.registerClusterSingletonService(service2);
        assertNotNull(reg);
        serviceFuture1.set(null);

        verify(service2).instantiateServiceInstance();
        reg.close();
        verify(service2).closeServiceInstance();
        serviceFuture2.set(null);
    }

    @Test
    public void testServiceNoBringup() throws Exception {
        ClusterSingletonServiceRegistration reg = singleton.registerClusterSingletonService(service1);
        assertNotNull(reg);
        verify(service1).instantiateServiceInstance();
        reg.close();
        verify(service1).closeServiceInstance();

        reg = singleton.registerClusterSingletonService(service2);
        assertNotNull(reg);
        reg.close();
        serviceFuture1.set(null);

        verify(service2, never()).instantiateServiceInstance();
        verify(service2, never()).closeServiceInstance();

        reg = singleton.registerClusterSingletonService(service2);
        assertNotNull(reg);
        verify(service2).instantiateServiceInstance();
        reg.close();
        verify(service2).closeServiceInstance();
        serviceFuture2.set(null);
    }
}
