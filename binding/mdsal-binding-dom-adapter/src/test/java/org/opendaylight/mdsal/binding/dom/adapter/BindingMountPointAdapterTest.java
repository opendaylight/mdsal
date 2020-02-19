/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;

public class BindingMountPointAdapterTest {
    @Test
    public void basicTest() throws Exception {
        final BindingToNormalizedNodeCodec codec = new BindingToNormalizedNodeCodec(
            new DefaultBindingRuntimeGenerator(), getTCCLClassLoadingStrategy(),
            mock(BindingNormalizedNodeCodecRegistry.class));
        final DOMMountPoint domMountPoint = mock(DOMMountPoint.class);
        final BindingMountPointAdapter bindingMountPointAdapter = new BindingMountPointAdapter(codec, domMountPoint);
        assertNull(bindingMountPointAdapter.getIdentifier());
    }
}
