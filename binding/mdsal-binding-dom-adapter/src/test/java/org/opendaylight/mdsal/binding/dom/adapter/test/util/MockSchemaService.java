/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.CurrentAdapterSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

public final class MockSchemaService implements DOMSchemaService, EffectiveModelContextProvider, AdapterContext {
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
        serializer = new CurrentAdapterSerializer(new BindingCodecContext(newContext));
        schemaContext = newContext.getEffectiveModelContext();
        listeners.streamListeners().forEach(listener -> listener.onModelContextUpdated(schemaContext));
    }

    @Override
    public synchronized CurrentAdapterSerializer currentSerializer() {
        return verifyNotNull(serializer);
    }
}
