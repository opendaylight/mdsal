/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

public final class MockSchemaService implements DOMSchemaService, EffectiveModelContextProvider {

    private EffectiveModelContext schemaContext;

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

    public synchronized void changeSchema(final EffectiveModelContext newContext) {
        schemaContext = newContext;
        listeners.streamListeners().forEach(listener -> listener.onModelContextUpdated(schemaContext));
    }
}
