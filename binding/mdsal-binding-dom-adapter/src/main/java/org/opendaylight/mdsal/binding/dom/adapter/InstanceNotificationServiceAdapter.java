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
import org.opendaylight.mdsal.binding.api.InstanceNotificationService;
import org.opendaylight.mdsal.binding.api.InstanceNotificationSpec;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.binding.KeyedListNotification;

final class InstanceNotificationServiceAdapter implements InstanceNotificationService {
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
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public <P extends DataObject & Identifiable<K>, N extends KeyedListNotification<N, P, K>, K extends Identifier<P>>
            Registration registerListener(final InstanceNotificationSpec<N, P> spec, final InstanceIdentifier<P> path,
                final KeyedListListener<P, N, K> listener, final Executor executor) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    private static final class Builder extends BindingDOMAdapterBuilder<InstanceNotificationService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        protected InstanceNotificationService createInstance(final ClassToInstanceMap<DOMService> delegates) {
            return new InstanceNotificationServiceAdapter(adapterContext(),
                delegates.getInstance(DOMInstanceNotificationService.class));
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMInstanceNotificationService.class);
        }
    }
}
