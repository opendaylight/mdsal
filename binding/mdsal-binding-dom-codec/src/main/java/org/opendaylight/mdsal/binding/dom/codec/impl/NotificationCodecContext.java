/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkState;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.EventInstantAware;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

final class NotificationCodecContext<D extends DataObject & Notification>
        extends DataObjectCodecContext<D, NotificationDefinition> {
    private static final Method EVENT_INSTANT;

    static {
        try {
            EVENT_INSTANT = EventInstantAware.class.getDeclaredMethod("eventInstant");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final MethodHandle eventProxy;

    NotificationCodecContext(final Class<?> key, final NotificationDefinition schema,
            final CodecContextFactory factory) {
        super(DataContainerCodecPrototype.from(key, schema, factory));
        final Class<D> bindingClass = getBindingClass();
        eventProxy = createProxyConstructor(bindingClass, bindingClass, AugmentationHolder.class,
            EventInstantAware.class);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        checkState(data instanceof ContainerNode);
        return createBindingProxy((ContainerNode) data);
    }

    Notification deserialize(final @NonNull ContainerNode data, final @NonNull Instant eventInstant) {
        final LazyDataObject<?> lazyDataObject = new LazyDataObject<>(this, data);

        return createBindingProxy(eventProxy, (proxy, method, args) -> EVENT_INSTANT.equals(method) ? eventInstant
                : lazyDataObject.invoke(proxy, method, args));
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }
}