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
import static org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;

public class BindingMountPointAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final GeneratedClassLoadingStrategy loading = getTCCLClassLoadingStrategy();
        final BindingNormalizedNodeCodecRegistry codecRegistry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec = new BindingToNormalizedNodeCodec(loading, codecRegistry);
        final DOMMountPoint domMountPoint = mock(DOMMountPoint.class);
        final BindingMountPointAdapter bindingMountPointAdapter = new BindingMountPointAdapter(codec, domMountPoint);
        assertNull(bindingMountPointAdapter.getIdentifier());
    }
}