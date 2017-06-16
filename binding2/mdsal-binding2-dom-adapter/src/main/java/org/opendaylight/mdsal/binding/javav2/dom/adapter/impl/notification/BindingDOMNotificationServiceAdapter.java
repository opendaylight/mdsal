/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.api.NotificationService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.serializer.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Notification service adapter.
 */
@Beta
public class BindingDOMNotificationServiceAdapter implements NotificationService, AutoCloseable {

    public static final Factory<NotificationService> BUILDER_FACTORY = Builder::new;

    private final BindingNormalizedNodeSerializer codec;
    private final DOMNotificationService domNotifService;

    public BindingDOMNotificationServiceAdapter(final BindingNormalizedNodeSerializer codec,
            final DOMNotificationService domNotifService) {
        this.codec = codec;
        this.domNotifService = domNotifService;
    }

    @Override
    public <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(final T listener) {
        final BindingDOMNotificationListenerAdapter domListener =
                new BindingDOMNotificationListenerAdapter(codec, listener);
        final ListenerRegistration<BindingDOMNotificationListenerAdapter> domRegistration =
                domNotifService.registerNotificationListener(domListener, domListener.getSupportedNotifications());
        return new ListenerRegistrationImpl<>(listener, domRegistration);
    }

    @Override
    public void close() throws Exception {
        // NOOP
    }

    private static class ListenerRegistrationImpl<T extends NotificationListener>
            extends AbstractListenerRegistration<T> {
        private final ListenerRegistration<?> listenerRegistration;

        ListenerRegistrationImpl(final T listener, final ListenerRegistration<?> listenerRegistration) {
            super(listener);
            this.listenerRegistration = listenerRegistration;
        }

        @Override
        protected void removeRegistration() {
            listenerRegistration.close();
        }
    }

    private static class Builder extends BindingDOMAdapterBuilder<NotificationService> {

        @Override
        protected NotificationService createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMNotificationService domNotification = delegates.getInstance(DOMNotificationService.class);
            return new BindingDOMNotificationServiceAdapter(codec.getCodecRegistry(), domNotification);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMNotificationService.class);
        }
    }

    /**
     * Get DOM notification service.
     *
     * @return DOM notification service
     */
    public DOMNotificationService getDomService() {
        return domNotifService;
    }
}

