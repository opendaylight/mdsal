/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.service.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.schema.service.osgi.util.TestModel;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

public class OsgiBundleScanningSchemaServiceTest {

    private OsgiBundleScanningSchemaService osgiService;
    private final BundleContext bundleContext = mock(BundleContext.class, "bundleContext");

    @Before
    public void setUp() throws Exception {
        destroyInstance();
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

    @SuppressWarnings("checkstyle:IllegalCatch")
    @After
    public void destroyInstance() throws Exception {
        try {
            OsgiBundleScanningSchemaService.getInstance();
            OsgiBundleScanningSchemaService.destroyInstance();
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void basicTest() throws Exception {
        final SchemaContext schemaContext = TestModel.createTestContext();

        final SchemaContextListener schemaContextListener = mock(SchemaContextListener.class);
        doNothing().when(schemaContextListener).onGlobalContextUpdated(schemaContext);
        osgiService.registerSchemaContextListener(schemaContextListener);

        osgiService.notifyListeners(schemaContext);

        doReturn(schemaContextListener).when(bundleContext).getService(null);
        assertEquals(schemaContextListener, osgiService.addingService(null));

        osgiService.registerSchemaContextListener(schemaContextListener);
        assertNull(osgiService.getSchemaContext());

        doReturn(false).when(bundleContext).ungetService(null);
        osgiService.removedService(null, null);
        verify(bundleContext).ungetService(any());

        osgiService.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sessionContextTest() throws Exception {
        osgiService.getSessionContext();
    }
}
