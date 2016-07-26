/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.osgi;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class SchemaServiceActivatorTest {

    @Test
    public void basicTest() throws Exception {
        final BundleContext bundleContext = mock(BundleContext.class);
        doReturn(mock(Filter.class)).when(bundleContext).createFilter(any());
        doNothing().when(bundleContext).addBundleListener(any());
        doReturn(new Bundle[] {}).when(bundleContext).getBundles();
        doNothing().when(bundleContext).addServiceListener(any(), any());
        doReturn(new ServiceReference<?>[] {}).when(bundleContext).getServiceReferences(anyString(), any());
        doReturn(mock(ServiceRegistration.class)).when(bundleContext).registerService(any(Class.class), any(), any());
        doNothing().when(bundleContext).removeBundleListener(any());
        doNothing().when(bundleContext).removeServiceListener(any());
        SchemaServiceActivator schemaServiceActivator = new SchemaServiceActivator();
        schemaServiceActivator.start(bundleContext);

        final ServiceRegistration registration = mock(ServiceRegistration.class);
        final OsgiBundleScanningSchemaService osgiBundle =
                mock(OsgiBundleScanningSchemaService.class, CALLS_REAL_METHODS);

        final Field schemaServiceRegField = SchemaServiceActivator.class.getDeclaredField("schemaServiceReg");
        schemaServiceRegField.setAccessible(true);
        schemaServiceRegField.set(schemaServiceActivator, registration);

        final Field schemaServiceField = SchemaServiceActivator.class.getDeclaredField("schemaService");
        schemaServiceField.setAccessible(true);
        schemaServiceField.set(schemaServiceActivator, osgiBundle);

        doNothing().when(registration).unregister();
        doNothing().when(osgiBundle).close();

        schemaServiceActivator.stop(bundleContext);
        verify(registration).unregister();
        verify(osgiBundle).close();
    }

    @After
    @Before
    public void destroyInstance() throws Exception {
        try {
            OsgiBundleScanningSchemaService.getInstance();
            OsgiBundleScanningSchemaService.destroyInstance();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }
}