/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Lazy serialized implementation of DOM Notification.
 *
 * <p>
 * This implementation performs serialization of data, only if receiver of notification actually accessed data from
 * notification.
 */
public final class LazySerializedDOMNotification implements DOMNotification, DOMEvent {
    private final @NonNull BindingNormalizedNodeSerializer codec;
    private final @NonNull Notification data;
    private final @NonNull SchemaPath type;
    private final @NonNull Instant eventInstant;

    private volatile ContainerNode domBody;

    LazySerializedDOMNotification(final BindingNormalizedNodeSerializer codec, final Notification data,
            final SchemaPath type, final Instant eventInstant) {
        this.codec = requireNonNull(codec);
        this.data = requireNonNull(data);
        this.type = requireNonNull(type);
        this.eventInstant = requireNonNull(eventInstant);
    }

    static @NonNull DOMNotification create(final BindingNormalizedNodeSerializer codec, final Notification data,
            final Instant eventInstant) {
        // TODO: for nested (YANG 1.1) notifications we will need the SchemaPath where the notification is being invoked
        //       and use that instead of ROOT. How Binding users will refer to it is TBD (but probably
        //       InstanceIdentifier, which means we will need to do some lifting to find the SchemaPath)
        final SchemaPath type = SchemaPath.ROOT.createChild(BindingReflections.findQName(data.implementedInterface()));
        return new LazySerializedDOMNotification(codec, data, type, eventInstant);
    }

    @Override
    public SchemaPath getType() {
        return type;
    }

    @Override
    public ContainerNode getBody() {
        ContainerNode local = domBody;
        if (local == null) {
            domBody = local = codec.toNormalizedNodeNotification(data);
        }
        return local;
    }

    @Override
    public Instant getEventInstant() {
        return eventInstant;
    }

    public @NonNull Notification getBindingData() {
        return data;
    }
}
