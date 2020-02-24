/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.spi.AbstractDOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DOMSchemaService.class, immediate = true)
public final class OSGiDOMSchemaService extends AbstractDOMSchemaService {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiDOMSchemaService.class);

    @Reference(target = "(component.factory=" + SchemaSchemaContextListenerImpl.FACTORY_NAME + ")")
    ComponentFactory listenerFactory = null;

    private final List<SchemaContextListener> listeners = new CopyOnWriteArrayList<>();

    private volatile SchemaContext currentContext;

    @Override
    public SchemaContext getGlobalContext() {
        return verifyNotNull(currentContext);
    }

    @Override
    public ListenerRegistration<SchemaContextListener> registerSchemaContextListener(
            final SchemaContextListener listener) {
        return registerListener(requireNonNull(listener));
    }

    @Reference(fieldOption = FieldOption.REPLACE)
    void bindContext(final BindingRuntimeContext newContext) {
        final SchemaContext ctx = newContext.getSchemaContext();
        LOG.trace("Updating context to {}", ctx);
        currentContext = ctx;
        listeners.forEach(listener -> notifyListener(ctx, listener));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    void addListener(final SchemaContextListener listener) {
        LOG.trace("Adding listener {}", listener);
        listeners.add(listener);
        listener.onGlobalContextUpdated(getGlobalContext());
    }

    void removeListener(final SchemaContextListener listener) {
        LOG.trace("Removing listener {}", listener);
        listeners.remove(listener);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("DOM Schema services activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("DOM Schema services deactivated");
    }

    private @NonNull ListenerRegistration<SchemaContextListener> registerListener(
            final @NonNull SchemaContextListener listener) {
        final ComponentInstance reg = listenerFactory.newInstance(SchemaSchemaContextListenerImpl.props(listener));
        return new ListenerRegistration<>() {
            @Override
            public SchemaContextListener getInstance() {
                return listener;
            }

            @Override
            public void close() {
                reg.dispose();
            }
        };
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static void notifyListener(final SchemaContext context, final SchemaContextListener listener) {
        try {
            listener.onGlobalContextUpdated(context);
        } catch (RuntimeException e) {
            LOG.warn("Failed to notify listener {}", listener, e);
        }
    }
}
