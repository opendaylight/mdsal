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
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.util.ServiceLoader;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.CurrentAdapterSerializer;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

public final class MockSchemaService implements DOMSchemaService, EffectiveModelContextProvider, AdapterContext {
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

    private EffectiveModelContext schemaContext;
    private CurrentAdapterSerializer serializer;

    final ListenerRegistry<EffectiveModelContextListener> listeners = ListenerRegistry.create();

    @Override
    public synchronized EffectiveModelContext getGlobalContext() {
        return schemaContext;
    }

    @Override
    public ListenerRegistration<EffectiveModelContextListener> registerSchemaContextListener(
            final EffectiveModelContextListener listener) {
        return listeners.register(listener);
    }

    @Override
    public synchronized EffectiveModelContext getEffectiveModelContext() {
        return schemaContext;
    }

    @Override
    public ClassToInstanceMap<DOMSchemaServiceExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of();
    }

    public synchronized void changeSchema(final BindingRuntimeContext newContext) {
        serializer = new CurrentAdapterSerializer(CODEC_CACHE.getUnchecked(newContext));
        schemaContext = newContext.getEffectiveModelContext();
        listeners.streamListeners().forEach(listener -> listener.onModelContextUpdated(schemaContext));
    }

    @Override
    public synchronized CurrentAdapterSerializer currentSerializer() {
        return verifyNotNull(serializer);
    }
}
