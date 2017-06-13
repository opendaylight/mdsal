/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.serialized;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.serializer.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Lazy serialized implementation of DOM Notification.
 *
 * <p>
 * This implementation performs serialization of data, only if receiver of notification actually accessed data
 * from notification.
 *
 */
@Beta
public final class LazySerializedDOMNotification implements DOMNotification {

    private final BindingNormalizedNodeSerializer codec;
    private final Notification<?> data;
    private final SchemaPath type;

    private ContainerNode domBody;

    private LazySerializedDOMNotification(final BindingNormalizedNodeSerializer codec, final Notification<?> data,
            final SchemaPath type) {
        this.codec = codec;
        this.data = data;
        this.type = type;
    }

    /**
     * Create serializer of Binding notification data to DOM notification with specific codec.
     *
     * @param codec
     *            - specific codec
     * @param data
     *            - binding notification data
     * @return DOM notification serializer
     */
    public static DOMNotification create(final BindingNormalizedNodeSerializer codec, final Notification<?> data) {
        final SchemaPath type = SchemaPath.create(true, BindingReflections.findQName(data.getClass()));
        return new LazySerializedDOMNotification(codec, data, type);
    }

    @Nonnull
    @Override
    public SchemaPath getType() {
        return type;
    }

    @Nonnull
    @Override
    public ContainerNode getBody() {
        if (domBody == null) {
            domBody = codec.toNormalizedNodeNotification(data);
        }
        return domBody;
    }

    /**
     * Get binding notification data.
     *
     * @return binding notification data
     */
    public Notification<?> getBindingData() {
        return data;
    }
}
