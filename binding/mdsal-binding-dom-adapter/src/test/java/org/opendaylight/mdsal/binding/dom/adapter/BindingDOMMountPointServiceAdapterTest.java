/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.MountPointService.MountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@ExtendWith(MockitoExtension.class)
class BindingDOMMountPointServiceAdapterTest {
    @Mock
    private BindingDOMCodecServices registry;
    @Mock
    private DOMMountPointService mountPointService;
    @Mock
    private DOMMountPoint mountPoint;
    @Mock
    private MountPointListener mountPointListener;

    @Test
    void basicTest() {
        final var codec = new ConstantAdapterContext(registry);
        final var yiid = YangInstanceIdentifier.of(BooleanContainer.QNAME);
        doReturn(yiid).when(registry).toYangInstanceIdentifier(any());
        doReturn(InstanceIdentifier.create(BooleanContainer.class)).when(registry).fromYangInstanceIdentifier(yiid);

        final var adapter = new BindingDOMMountPointServiceAdapter(codec, mountPointService);

        doReturn(Optional.empty()).when(mountPointService).getMountPoint(any());
        assertFalse(adapter.getMountPoint(InstanceIdentifier.create(BooleanContainer.class)).isPresent());

        doReturn(Optional.of(mountPoint)).when(mountPointService).getMountPoint(any());
        doReturn(yiid).when(mountPoint).getIdentifier();
        assertTrue(adapter.getMountPoint(InstanceIdentifier.create(BooleanContainer.class)).isPresent());

        assertNotNull(adapter.registerListener(InstanceIdentifier.create(BooleanContainer.class), mountPointListener));
    }
}