/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.InstanceNotificationPublishService;
import org.opendaylight.mdsal.binding.api.InstanceNotificationSpec;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.reflect.BindingReflections;

final class InstanceNotificationPublishServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMInstanceNotificationPublishService, InstanceNotificationSpec<?, ?>,
            PublisherAdapter<?, ?>>
        implements InstanceNotificationPublishService {
    private static final class Builder extends BindingDOMAdapterBuilder<InstanceNotificationPublishService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<Class<? extends DOMService<?, ?>>> getRequiredDelegates() {
            return ImmutableSet.of(DOMInstanceNotificationPublishService.class);
        }

        @Override
        protected InstanceNotificationPublishService createInstance(
                final ClassToInstanceMap<DOMService<?, ?>> delegates) {
            return new InstanceNotificationPublishServiceAdapter(adapterContext(),
                delegates.getInstance(DOMInstanceNotificationPublishService.class));
        }
    }

    static final Factory<InstanceNotificationPublishService> BUILDER_FACTORY = Builder::new;

    private InstanceNotificationPublishServiceAdapter(final AdapterContext adapterContext,
            final DOMInstanceNotificationPublishService domPublishService) {
        super(adapterContext, domPublishService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends InstanceNotification<N, P>, P extends DataObject> Publisher<N, P> newPublisher(
            final InstanceNotificationSpec<N, P> notificationSpec) {
        return (Publisher<N, P>) getAdapter(notificationSpec);
    }

    @Override
    PublisherAdapter<?, ?> loadAdapter(final InstanceNotificationSpec<?, ?> key) {
        final var type = key.type();
        checkArgument(BindingReflections.isBindingClass(type));
        checkArgument(type.isInterface(), "Supplied Notification type must be an interface.");
        checkArgument(InstanceNotification.class.isAssignableFrom(type), "Illegal instance notification class %s",
            type);
        return new PublisherAdapter<>(adapterContext(), getDelegate(), key);
    }
}
