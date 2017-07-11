/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.mountpoint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.base.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.api.MountPointListener;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class BindingDOMMountPointServiceAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec((GeneratedClassLoadingStrategy)
                        GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), registry);
        doReturn(YangInstanceIdentifier.EMPTY).when(registry).toYangInstanceIdentifier(any());
        final DOMMountPointService mountPointService = mock(DOMMountPointService.class);

        final BindingDOMMountPointServiceAdapter adapter =
                new BindingDOMMountPointServiceAdapter(mountPointService, codec);

        doReturn(Optional.absent()).when(mountPointService).getMountPoint(any());
        assertFalse(adapter.getMountPoint(InstanceIdentifier.create(TreeNode.class)).isPresent());

        doReturn(Optional.of(mock(DOMMountPoint.class))).when(mountPointService).getMountPoint(any());
        assertTrue(adapter.getMountPoint(InstanceIdentifier.create(TreeNode.class)).isPresent());

        assertNotNull(adapter.registerListener(InstanceIdentifier.create(TreeNode.class),
                mock(MountPointListener.class)));
    }
}