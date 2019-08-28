/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.MountPointService.MountPointListener;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class BindingDOMMountPointServiceAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), registry);
        doReturn(YangInstanceIdentifier.empty()).when(registry).toYangInstanceIdentifier(any());
        final DOMMountPointService mountPointService = mock(DOMMountPointService.class);

        final BindingDOMMountPointServiceAdapter adapter =
                new BindingDOMMountPointServiceAdapter(mountPointService, codec);

        doReturn(Optional.empty()).when(mountPointService).getMountPoint(any());
        assertFalse(adapter.getMountPoint(InstanceIdentifier.create(DataObject.class)).isPresent());

        doReturn(Optional.of(mock(DOMMountPoint.class))).when(mountPointService).getMountPoint(any());
        assertTrue(adapter.getMountPoint(InstanceIdentifier.create(DataObject.class)).isPresent());

        assertNotNull(adapter.registerListener(InstanceIdentifier.create(DataObject.class),
                mock(MountPointListener.class)));
    }
}