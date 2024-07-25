/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@ExtendWith(MockitoExtension.class)
class DOMMountPointServiceImplTest {
    private static final YangInstanceIdentifier PATH =
        YangInstanceIdentifier.of(QName.create("namespace", "2012-12-12", "top"));

    @Mock
    private DOMMountPointListener listener;
    @Mock
    private DOMRpcService rpcService;
    @Captor
    private ArgumentCaptor<DOMMountPoint> captor;

    private final DOMMountPointService mountPointService = new DOMMountPointServiceImpl();

    @Test
    void testMountPointRegistration() {
        doNothing().when(listener).onMountPointCreated(any());
        mountPointService.registerProvisionListener(listener);

        // Create a mount point with schema context and a DOMService
        try (var reg = mountPointService.createMountPoint(PATH)
                .addService(DOMRpcService.class, rpcService)
                .register()) {
            // Verify listener has been notified and mount point is accessible from mount point service
            verify(listener).onMountPointCreated(captor.capture());
            assertEquals(PATH, captor.getValue().getIdentifier());

            // Verify mount point schema context and service
            final var mountPoint = mountPointService.getMountPoint(PATH).orElseThrow();
            assertEquals(Optional.of(rpcService), mountPoint.getService(DOMRpcService.class));
        }
    }

    @Test
    void testMountPointDestruction() {
        doNothing().when(listener).onMountPointRemoved(PATH);

        try (var reg = mountPointService.createMountPoint(PATH).register()) {
            mountPointService.registerProvisionListener(listener);
        }

        // Verify listener has been notified and mount point is not present in mount point service
        verify(listener).onMountPointRemoved(eq(PATH));
        assertEquals(Optional.empty(), mountPointService.getMountPoint(PATH));
    }
}
