/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.InstanceNotificationService;
import org.opendaylight.mdsal.binding.api.InstanceNotificationSpec;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

final class InstanceNotificationServiceAdapter implements InstanceNotificationService {
    private static final class Builder extends BindingDOMAdapterBuilder<InstanceNotificationService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        protected InstanceNotificationService createInstance(final ClassToInstanceMap<DOMService<?, ?>> delegates) {
            return new InstanceNotificationServiceAdapter(adapterContext(),
                delegates.getInstance(DOMInstanceNotificationService.class));
        }

        @Override
        public Set<? extends Class<? extends DOMService<?, ?>>> getRequiredDelegates() {
            return ImmutableSet.of(DOMInstanceNotificationService.class);
        }
    }

    static final Factory<InstanceNotificationService> BUILDER_FACTORY = Builder::new;

    private final AdapterContext adapterContext;
    private final DOMInstanceNotificationService domNotifService;

    private InstanceNotificationServiceAdapter(final AdapterContext adapterContext,
            final DOMInstanceNotificationService domNotifService) {
        this.adapterContext = requireNonNull(adapterContext);
        this.domNotifService = requireNonNull(domNotifService);
    }

    @Override
    public <P extends DataObject, N extends InstanceNotification<N, P>> Registration registerListener(
            final InstanceNotificationSpec<N, P> spec, final InstanceIdentifier<P> path, final Listener<P, N> listener,
            final Executor executor) {
        return registerListener(spec, path,
            new InstanceNotificationListenerAdapter<>(adapterContext, spec.type(), listener, executor));
    }

    @Override
    public <P extends DataObject & KeyAware<K>, N extends KeyedListNotification<N, P, K>, K extends Key<P>>
            Registration registerListener(final InstanceNotificationSpec<N, P> spec,
                final KeyedInstanceIdentifier<P, K> path, final KeyedListListener<P, N, K> listener,
                final Executor executor) {
        return registerListener(spec, path,
            new KeyedInstanceNotificationListenerAdapter<>(adapterContext, spec.type(), listener, executor));
    }

    private @NonNull Registration registerListener(final @NonNull InstanceNotificationSpec<?, ?> spec,
            final @NonNull InstanceIdentifier<?> path, final @NonNull DOMInstanceNotificationListener listener) {
        final var serializer = adapterContext.currentSerializer();
        return domNotifService.registerNotificationListener(
            DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, serializer.toYangInstanceIdentifier(path)),
            serializer.getNotificationPath(spec).lastNodeIdentifier(), listener);
    }
}
