/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.concurrent.Executor;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class SingleBindingDOMNotificationAdapter<N extends Notification> extends AbstractDOMNotificationListenerAdapter {
    private final Listener<N> delegate;
    private final Executor executor;
    private final Class<N> type;

    SingleBindingDOMNotificationAdapter(final AdapterContext adapterContext, final Class<N> type,
            final Listener<N> delegate, final Executor executor) {
        super(adapterContext);
        this.type = requireNonNull(type);
        this.delegate = requireNonNull(delegate);
        this.executor = requireNonNull(executor);
    }

    @Override
    void onNotification(final Absolute domType, final Notification notification) {
        executor.execute(() -> delegate.onNotification(type.cast(notification)));
    }

    @Override
    Set<Absolute> getSupportedNotifications() {
        return Set.of(Absolute.of(BindingReflections.findQName(type)));
    }
}
