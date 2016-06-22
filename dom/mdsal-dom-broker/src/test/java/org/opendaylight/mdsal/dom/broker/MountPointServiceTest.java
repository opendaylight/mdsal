/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ClassToInstanceMap;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService.DOMMountPointBuilder;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl.DOMMountPointBuilderImpl;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class MountPointServiceTest {

    private static DOMMountPointService mountService;
    private static final YangInstanceIdentifier PATH =
            YangInstanceIdentifier.of(QName.create("namespace", "12-12-2012", "top"));

    @Before
    public void setup() {
        mountService = new DOMMountPointServiceImpl();
    }

    @Test
    public void createSimpleMountPoint() throws Exception {
        final DOMMountPointListener listener = mock(DOMMountPointListener.class);
        doNothing().when(listener).onMountPointCreated(PATH);
        mountService.registerProvisionListener(listener);

        assertFalse(mountService.getMountPoint(PATH).isPresent());

        mountService.createMountPoint(PATH).register();

        assertTrue(mountService.getMountPoint(PATH).isPresent());
    }

    @Test
    public void unregisterTest() throws Exception {
        final DOMMountPointListener listener = mock(DOMMountPointListener.class);
        doNothing().when(listener).onMountPointCreated(PATH);
        doNothing().when(listener).onMountPointRemoved(PATH);
        final DOMMountPointServiceImpl mountService = new DOMMountPointServiceImpl();
        mountService.registerProvisionListener(listener);
        mountService.createMountPoint(PATH).register();

        assertTrue(mountService.getMountPoint(PATH).isPresent());

        mountService.unregisterMountPoint(PATH);

        assertFalse(mountService.getMountPoint(PATH).isPresent());
    }

    @Test
    public void mountRegistrationTest() throws Exception {
        final DOMMountPointBuilder mountBuilder = mountService.createMountPoint(PATH);
        final ObjectRegistration<DOMMountPoint> objectRegistration = mountBuilder.register();

        assertTrue(mountService.getMountPoint(PATH).isPresent());
        assertSame(objectRegistration.getInstance(), mountService.getMountPoint(PATH).get());

        objectRegistration.close();

        assertFalse(mountService.getMountPoint(PATH).isPresent());
    }

    @Test
    public void mountBuilderTest() throws Exception {
        final DOMMountPointBuilderImpl mountBuilder = (DOMMountPointBuilderImpl) mountService.createMountPoint(PATH);
        mountBuilder.register();

        final SchemaContext mockSchemaContext = mock(SchemaContext.class);
        mountBuilder.addInitialSchemaContext(mockSchemaContext);

        final Field schemaContextField = DOMMountPointBuilderImpl.class.getDeclaredField("schemaContext");
        schemaContextField.setAccessible(true);

        final SchemaContext schemaContext = (SchemaContext) schemaContextField.get(mountBuilder);

        assertSame(mockSchemaContext, schemaContext);

        final Field servicesField = DOMMountPointBuilderImpl.class.getDeclaredField("services");
        servicesField.setAccessible(true);

        final ClassToInstanceMap<DOMService> services =
                (ClassToInstanceMap<DOMService>) servicesField.get(mountBuilder);
        assertTrue(services.isEmpty());
        assertFalse(services.containsKey(DOMService.class));
        mountBuilder.addService(DOMService.class, null);
        assertFalse(services.isEmpty());
        assertTrue(services.containsKey(DOMService.class));
    }
}
