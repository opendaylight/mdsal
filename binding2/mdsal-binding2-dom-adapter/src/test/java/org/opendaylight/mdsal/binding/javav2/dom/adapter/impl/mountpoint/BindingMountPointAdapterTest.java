/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.mountpoint;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy;

import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;

public class BindingMountPointAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final GeneratedClassLoadingStrategy loading = (GeneratedClassLoadingStrategy) getTCCLClassLoadingStrategy();
        final BindingNormalizedNodeCodecRegistry codecRegistry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec = new BindingToNormalizedNodeCodec(loading, codecRegistry);
        final DOMMountPoint domMountPoint = mock(DOMMountPoint.class);
        final BindingMountPointAdapter bindingMountPointAdapter = new BindingMountPointAdapter(codec, domMountPoint);
        assertNull(bindingMountPointAdapter.getIdentifier());
    }
}
