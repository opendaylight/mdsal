/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.ListNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.BindingCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Codec for serializer/deserialize DOM and binding identifiers.
 *
 */
@Beta
public final class InstanceIdentifierCodec implements Codec<YangInstanceIdentifier, InstanceIdentifier<?>> {

    private final BindingCodecContext context;

    /**
     * Prepare codec context.
     *
     * @param context
     *            - binding codec context
     */
    public InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public YangInstanceIdentifier serialize(final InstanceIdentifier<?> input) {
        final List<PathArgument> domArgs = new ArrayList<>();
        context.getCodecContextNode(input, domArgs);
        return YangInstanceIdentifier.create(domArgs);
    }

    @Override
    public InstanceIdentifier<?> deserialize(final YangInstanceIdentifier input) {
        final List<TreeArgument<?>> builder = new ArrayList<>();
        final NodeCodecContext<?> codec = context.getCodecContextNode(input, builder);

        if (codec == null) {
            return null;
        }

        if (codec instanceof ListNodeCodecContext && Iterables.getLast(builder) instanceof Item<?>) {
            // We ended up in list, but without key, which means it represent list as a whole,
            // which is not binding representable.
            return null;
        }

        return InstanceIdentifier.create(builder);
    }
}
