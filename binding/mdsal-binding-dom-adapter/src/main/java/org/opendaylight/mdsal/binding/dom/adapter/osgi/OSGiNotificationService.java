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
import java.util.concurrent.Executor;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
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
    public <N extends Notification<N> & DataObject> Registration registerListener(final Class<N> type,
            final NotificationService.Listener<N> listener, final Executor executor) {
        return delegate().registerListener(type, listener, executor);
    }

    @Override
    public Registration registerCompositeListener(final NotificationService.CompositeListener listener,
            final Executor executor) {
        return delegate().registerCompositeListener(listener, executor);
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
