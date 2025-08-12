/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * {@link AbstractLazySerializedEvent} specialized to RFC6020 global notifications.
 */
final class LazySerializedNotification extends AbstractLazySerializedEvent<Notification<?>> {
    LazySerializedNotification(final CurrentAdapterSerializer serializer, final Notification<?> data) {
        super(serializer, data, serializer.getNotificationPath(data.implementedInterface()));
    }

    @Override
    ContainerNode loadBody(final BindingNormalizedNodeSerializer codec) {
        return codec.toNormalizedNodeNotification(getBindingData());
    }
}
