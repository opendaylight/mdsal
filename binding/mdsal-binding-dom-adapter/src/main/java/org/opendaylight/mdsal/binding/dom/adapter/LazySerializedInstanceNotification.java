/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * {@link AbstractLazySerializedEvent} specialized to RFC7950 instance notifications.
 */
final class LazySerializedInstanceNotification extends AbstractLazySerializedEvent<InstanceNotification<?, ?>> {
    LazySerializedInstanceNotification(final BindingNormalizedNodeSerializer codec, final Absolute path,
            final InstanceNotification<?, ?> data) {
        super(codec, data, path);
    }

    @Override
    ContainerNode loadBody(final BindingNormalizedNodeSerializer codec) {
        return codec.toNormalizedNodeNotification(getType(), getBindingData());
    }
}
