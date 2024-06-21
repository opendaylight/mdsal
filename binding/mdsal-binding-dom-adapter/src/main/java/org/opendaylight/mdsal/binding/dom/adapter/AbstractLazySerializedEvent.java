/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.EventInstantAware;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Lazy serialized implementation of {@link DOMNotification} and {@link DOMEvent}.
 *
 * <p>
 * This implementation performs serialization of data, only if receiver of notification actually accessed data from
 * notification.
 */
abstract class AbstractLazySerializedEvent<T extends BaseNotification> implements DOMNotification, DOMEvent {
    private final @NonNull BindingNormalizedNodeSerializer codec;
    private final @NonNull T data;
    private final @NonNull Absolute type;
    private final @NonNull Instant eventInstant;

    private volatile ContainerNode domBody;

    AbstractLazySerializedEvent(final BindingNormalizedNodeSerializer codec, final T data, final Absolute type) {
        this.codec = requireNonNull(codec);
        this.data = requireNonNull(data);
        this.type = requireNonNull(type);
        eventInstant = data instanceof EventInstantAware aware ? aware.eventInstant() : Instant.now();
    }

    @Override
    public final Absolute getType() {
        return type;
    }

    @Override
    public final ContainerNode getBody() {
        var local = domBody;
        if (local == null) {
            domBody = local = verifyNotNull(loadBody(codec));
        }
        return local;
    }

    abstract @NonNull ContainerNode loadBody(@NonNull BindingNormalizedNodeSerializer codec);

    @Override
    public final Instant getEventInstant() {
        return eventInstant;
    }

    final @NonNull T getBindingData() {
        return data;
    }
}
