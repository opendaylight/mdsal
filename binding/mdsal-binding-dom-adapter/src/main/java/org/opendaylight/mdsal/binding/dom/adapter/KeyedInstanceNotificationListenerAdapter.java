/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verify;

import java.util.concurrent.Executor;
import org.opendaylight.mdsal.binding.api.InstanceNotificationService.KeyedListListener;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyedListNotification;

final class KeyedInstanceNotificationListenerAdapter<P extends EntryObject<P, K>, K extends Key<P>,
            N extends KeyedListNotification<N, P, K>>
        extends AbstractInstanceNotificationListenerAdapter<P, N, KeyedListListener<P, N, K>> {
    KeyedInstanceNotificationListenerAdapter(final AdapterContext adapterContext, final Class<N> notificationClass,
            final KeyedListListener<P, N, K> delegate, final Executor executor) {
        super(adapterContext, notificationClass, delegate, executor);
    }

    @Override
    @SuppressWarnings("unchecked")
    void onNotification(final KeyedListListener<P, N, K> delegate, final DataObjectIdentifier<?> path,
            final N notification) {
        verify(path instanceof DataObjectIdentifier.WithKey, "Unexpected path %s", path);
        delegate.onNotification((DataObjectIdentifier.WithKey<P, K>) path, notification);
    }
}
