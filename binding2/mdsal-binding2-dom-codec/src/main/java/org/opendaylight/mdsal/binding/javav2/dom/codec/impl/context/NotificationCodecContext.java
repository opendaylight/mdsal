/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecPrototype;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

/**
 * Context for prototype of notification.
 *
 * @param <D>
 *            - type of tree node
 */
@SuppressWarnings("rawtypes")
@Beta
public final class NotificationCodecContext<D extends TreeNode & Notification>
        extends TreeNodeCodecContext<D, NotificationDefinition> {

    /**
     * Prepare context for notification from prototype.
     *
     * @param key
     *            - binding class
     * @param schema
     *            - schema of notification
     * @param factory
     *            - codec context factory
     */
    public NotificationCodecContext(final Class<?> key, final NotificationDefinition schema,
            final CodecContextFactory factory) {
        super(DataContainerCodecPrototype.from(key, schema, factory));
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> data) {
        Preconditions.checkState(data instanceof ContainerNode);
        return createBindingProxy((NormalizedNodeContainer<?, ?, ?>) data);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }
}
