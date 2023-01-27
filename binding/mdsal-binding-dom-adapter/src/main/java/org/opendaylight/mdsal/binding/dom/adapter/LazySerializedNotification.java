/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * {@link AbstractLazySerializedEvent} specialized to RFC6020 global notifications.
 */
final class LazySerializedNotification extends AbstractLazySerializedEvent<Notification<?>> {
    private static final LoadingCache<Class<?>, Absolute> PATHS = CacheBuilder.newBuilder().weakKeys()
        .build(new CacheLoader<>() {
            @Override
            public Absolute load(final Class<?> key) {
                final QName qname = BindingRuntimeHelpers.getQName(BindingRuntimeHelpers.createRuntimeContext(), key);
                return Absolute.of(qname).intern();
            }
        });

    LazySerializedNotification(final BindingNormalizedNodeSerializer codec, final Notification<?> data) {
        super(codec, data, PATHS.getUnchecked(data.implementedInterface()));
    }

    @Override
    ContainerNode loadBody(final BindingNormalizedNodeSerializer codec) {
        return codec.toNormalizedNodeNotification(getBindingData());
    }
}
