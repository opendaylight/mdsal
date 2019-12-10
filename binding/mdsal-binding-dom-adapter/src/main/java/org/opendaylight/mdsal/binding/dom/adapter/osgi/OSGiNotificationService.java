/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.InstanceListener;
import org.opendaylight.mdsal.binding.api.NotificationService.KeyedListListener;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiNotificationService.FACTORY_NAME)
public final class OSGiNotificationService extends AbstractAdaptedService<NotificationService>
        implements NotificationService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiNotificationService";

    public OSGiNotificationService() {
        super(NotificationService.class);
    }

    @Override
    public <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(final T listener) {
        return delegate().registerNotificationListener(listener);
    }

    @Override
    public <N extends Notification, T extends Listener<N>> ListenerRegistration<T> registerListener(final Class<N> type,
            final T listener) {
        return delegate().registerListener(type, listener);
    }

    @Override
    public <P extends DataObject, N extends InstanceNotification<N, P>, T extends InstanceListener<P, N>>
            ListenerRegistration<T> registerListener(final Class<N> type, final InstanceIdentifier<P> path,
                final T listener) {
        return delegate().registerListener(type, path, listener);
    }

    @Override
    public <P extends DataObject & Identifiable<K>, N extends InstanceNotification<N, P>, K extends Identifier<P>,
            T extends KeyedListListener<P, N, K>> ListenerRegistration<T> registerListener(
                final Class<N> type, final KeyedInstanceIdentifier<P, K> path, final T listener) {
        return delegate().registerListener(type, path, listener);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        start(properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }
}
