/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import static com.google.common.base.Verify.verifyNotNull;

import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.CurrentAdapterSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;

public final class MockAdapterContext implements AdapterContext, EffectiveModelContextListener {
    private volatile CurrentAdapterSerializer serializer = null;

    @Override
    public CurrentAdapterSerializer currentSerializer() {
        return verifyNotNull(serializer);
    }

    @Override
    public void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        serializer = new CurrentAdapterSerializer(new BindingCodecContext(DefaultBindingRuntimeContext.create(
            new DefaultBindingRuntimeGenerator().generateTypeMapping(newModelContext),
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy())));
    }
}
