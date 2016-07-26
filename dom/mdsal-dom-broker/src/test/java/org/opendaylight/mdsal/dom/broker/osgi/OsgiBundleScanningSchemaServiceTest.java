/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

public class OsgiBundleScanningSchemaServiceTest {

    private OsgiBundleScanningSchemaService osgiService;

    @Before
    public void setUp() throws Exception {
        final BundleContext bundleContext = mock(BundleContext.class, "bundleContext");
        doReturn(mock(Filter.class)).when(bundleContext).createFilter(any());
        doNothing().when(bundleContext).addBundleListener(any());
        doReturn(new Bundle[] {}).when(bundleContext).getBundles();
        doNothing().when(bundleContext).addServiceListener(any(), any());
        doReturn(new ServiceReference<?>[] {}).when(bundleContext).getServiceReferences(anyString(), any());
        doNothing().when(bundleContext).removeBundleListener(any());
        doNothing().when(bundleContext).removeServiceListener(any());
        osgiService = OsgiBundleScanningSchemaService.createInstance(bundleContext);
        assertEquals(osgiService, OsgiBundleScanningSchemaService.getInstance());
        assertEquals(bundleContext, osgiService.getContext());
    }

    @After
    public void destroyInstance() throws Exception {
        try {
            OsgiBundleScanningSchemaService.getInstance();
            OsgiBundleScanningSchemaService.destroyInstance();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void basicTest() throws Exception {
        final SchemaContext schemaContext = TestModel.createTestContext();
        final SchemaContextListener schemaContextListener = mock(SchemaContextListener.class);
        doNothing().when(schemaContextListener).onGlobalContextUpdated(schemaContext);
        osgiService.registerSchemaContextListener(schemaContextListener);

        final Method schemaContextUpdate =
                OsgiBundleScanningSchemaService.class.getDeclaredMethod("updateContext", SchemaContext.class);
        schemaContextUpdate.setAccessible(true);
        schemaContextUpdate.invoke(osgiService, schemaContext);

        osgiService.registerSchemaContextListener(schemaContextListener);

        assertNull(osgiService.getSchemaContext());
        osgiService.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unimplementedTest() throws Exception {
        osgiService.getSessionContext();
    }
}