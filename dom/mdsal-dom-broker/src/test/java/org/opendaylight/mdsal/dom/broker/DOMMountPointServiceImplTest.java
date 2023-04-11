/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService.DOMMountPointBuilder;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class DOMMountPointServiceImplTest {

    private static final YangInstanceIdentifier PATH =
            YangInstanceIdentifier.of(QName.create("namespace", "2012-12-12",
        "top"));

    private DOMMountPointService mountPointService;

    @Before
    public void setup() {
        mountPointService = new DOMMountPointServiceImpl();
    }

    @Test
    public void testMountPointRegistration() {
        final DOMMountPointListener mountPointListener = mock(DOMMountPointListener.class);
        doNothing().when(mountPointListener).onMountPointCreated(PATH);
        mountPointService.registerProvisionListener(mountPointListener);

        // Create a mount point with schema context and a DOMService
        final DOMMountPointBuilder mountPointBuilder = mountPointService.createMountPoint(PATH);

        final DOMRpcService rpcService = mock(DOMRpcService.class);
        mountPointBuilder.addService(DOMRpcService.class, rpcService);

        mountPointBuilder.register();

        // Verify listener has been notified and mount point is accessible from mount point service
        verify(mountPointListener).onMountPointCreated(eq(PATH));
        assertTrue(mountPointService.getMountPoint(PATH).isPresent());

        // Verify mount point schema context and service
        final DOMMountPoint mountPoint = mountPointService.getMountPoint(PATH).orElseThrow();
        assertEquals(Optional.of(rpcService), mountPoint.getService(DOMRpcService.class));
    }

    @Test
    public void testMountPointDestruction() {
        final DOMMountPointListener mountPointListener = mock(DOMMountPointListener.class);
        doNothing().when(mountPointListener).onMountPointRemoved(PATH);

        final ObjectRegistration<DOMMountPoint> mountPointRegistration =
                mountPointService.createMountPoint(PATH).register();

        mountPointService.registerProvisionListener(mountPointListener);

        mountPointRegistration.close();

        // Verify listener has been notified and mount point is not present in mount point service
        verify(mountPointListener).onMountPointRemoved(eq(PATH));
        assertFalse(mountPointService.getMountPoint(PATH).isPresent());
    }
}
