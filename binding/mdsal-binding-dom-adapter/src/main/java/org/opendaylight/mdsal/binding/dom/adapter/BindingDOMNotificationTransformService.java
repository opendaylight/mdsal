/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationTransformService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListenerRegistry;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.Notification;

final class BindingDOMNotificationTransformService implements NotificationTransformService {
    public static final Factory<NotificationTransformService> BUILDER_FACTORY = Builder::new;

    private final AdapterContext adapterContext;
    private final DOMNotificationSubscriptionListenerRegistry subscriptionRegistry;

    BindingDOMNotificationTransformService(final AdapterContext adapterContext,
            final DOMNotificationSubscriptionListenerRegistry subscriptionRegistry) {
        this.adapterContext = requireNonNull(adapterContext);
        this.subscriptionRegistry = requireNonNull(subscriptionRegistry);
    }

    @Override
    public <I extends Notification, O extends Notification> Registration registerNotificationTransformer(
            final Class<I> input, final Class<O> output, final OneToOneTransformer<I, O> transformer) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <I extends Notification, O extends Notification> Registration registerNotificationTransformer(
            final Class<I> input, final Class<O> output, final OneToOptionalTransformer<I, O> transformer) {
        // TODO Auto-generated method stub
        return null;
    }


    private static class Builder extends BindingDOMAdapterBuilder<NotificationTransformService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        protected NotificationService createInstance(final ClassToInstanceMap<DOMService> delegates) {
            final DOMNotificationService domNotification = delegates.getInstance(DOMNotificationService.class);
            return new BindingDOMNotificationServiceAdapter(adapterContext(), domNotification);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMNotificationSubscriptionListenerRegistry.class);
        }
    }

}
