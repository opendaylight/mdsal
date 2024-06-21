/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.CurrentAdapterSerializer;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ObjectRegistry;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public final class MockSchemaService implements DOMSchemaService, AdapterContext {
    // Codec has some amount of non-trivial state, such as generated classes. Its operation should not be affected by
    // anything except BindingRuntimeContext, hence we should be able to reuse it.
    private static final LoadingCache<BindingRuntimeContext, BindingDOMCodecServices> CODEC_CACHE =
        CacheBuilder.newBuilder().weakKeys().weakValues().build(
            new CacheLoader<BindingRuntimeContext, BindingDOMCodecServices>() {
                @Override
                public BindingDOMCodecServices load(final BindingRuntimeContext key) {
                    return ServiceLoader.load(BindingDOMCodecFactory.class)
                            .findFirst().orElseThrow().createBindingDOMCodec(key);
                }
            });

    private EffectiveModelContext modelContext;
    private CurrentAdapterSerializer serializer;

    final ObjectRegistry<Consumer<EffectiveModelContext>> listeners = ObjectRegistry.createConcurrent("mock schema");

    @Override
    public synchronized EffectiveModelContext getGlobalContext() {
        return modelContext;
    }

    @Override
    public Registration registerSchemaContextListener(final Consumer<EffectiveModelContext> listener) {
        return listeners.register(listener);
    }

    public synchronized void changeSchema(final BindingRuntimeContext newContext) {
        serializer = new CurrentAdapterSerializer(CODEC_CACHE.getUnchecked(newContext));
        modelContext = newContext.modelContext();
        listeners.streamObjects().forEach(listener -> listener.accept(modelContext));
    }

    @Override
    public synchronized CurrentAdapterSerializer currentSerializer() {
        return verifyNotNull(serializer);
    }
}
