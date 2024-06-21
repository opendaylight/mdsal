/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executor;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@VisibleForTesting
// FIXME: 10.0.0: make this class final
public class BindingDOMNotificationServiceAdapter implements NotificationService {
    public static final Factory<NotificationService> BUILDER_FACTORY = Builder::new;

    private final AdapterContext adapterContext;
    private final DOMNotificationService domNotifService;

    public BindingDOMNotificationServiceAdapter(final AdapterContext adapterContext,
            final DOMNotificationService domNotifService) {
        this.adapterContext = requireNonNull(adapterContext);
        this.domNotifService = domNotifService;
    }

    @Override
    public <N extends Notification<N> & DataObject> Registration registerListener(final Class<N> type,
            final Listener<N> listener, final Executor executor) {
        final var domListener = new BindingDOMNotificationListenerAdapter<>(adapterContext, type, listener, executor);
        return domNotifService.registerNotificationListener(domListener, Set.of(domListener.schemaPath()));
    }

    @Override
    public Registration registerCompositeListener(final CompositeListener listener, final Executor executor) {
        final var exec = requireNonNull(executor);
        final var listeners = new HashMap<Absolute, DOMNotificationListener>();
        for (var constituent : listener.constituents()) {
            final var domListener = new BindingDOMNotificationListenerAdapter<>(adapterContext, constituent, exec);
            listeners.put(domListener.schemaPath(), domListener);
        }

        return domNotifService.registerNotificationListeners(listeners);
    }

    private static class Builder extends BindingDOMAdapterBuilder<NotificationService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        protected NotificationService createInstance(final ClassToInstanceMap<DOMService<?, ?>> delegates) {
            return new BindingDOMNotificationServiceAdapter(adapterContext(),
                delegates.getInstance(DOMNotificationService.class));
        }

        @Override
        public Set<? extends Class<? extends DOMService<?, ?>>> getRequiredDelegates() {
            return ImmutableSet.of(DOMNotificationService.class);
        }
    }
}
