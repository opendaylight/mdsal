/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.concurrent.Executor;
import org.opendaylight.mdsal.binding.api.InstanceNotificationService.Listener;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.InstanceNotification;

final class InstanceNotificationListenerAdapter<P extends DataObject, N extends InstanceNotification<N, P>>
        extends AbstractInstanceNotificationListenerAdapter<P, N, Listener<P, N>> {
    InstanceNotificationListenerAdapter(final AdapterContext adapterContext, final Class<N> notificationClass,
            final Listener<P, N> delegate, final Executor executor) {
        super(adapterContext, notificationClass, delegate, executor);
    }

    @Override
    @SuppressWarnings("unchecked")
    void onNotification(final Listener<P, N> delegate, final DataObjectIdentifier<?> path, final N notification) {
        delegate.onNotification((DataObjectIdentifier<P>) path, notification);
    }
}
