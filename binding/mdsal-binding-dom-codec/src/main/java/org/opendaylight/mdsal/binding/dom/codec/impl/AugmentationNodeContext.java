/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

final class AugmentationNodeContext<D extends DataObject & Augmentation<?>>
        extends AbstractDataObjectCodecContext<D, AugmentRuntimeType> implements BindingAugmentationCodecTreeNode<D> {
    AugmentationNodeContext(final DataContainerCodecPrototype<AugmentRuntimeType> prototype) {
        super(prototype);
    }

    @Override
    public PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        return checkNull(arg);
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final PathArgument arg) {
        return checkNull(arg);
    }

    private static <T> @Nullable T checkNull(final Object arg) {
        if (arg != null) {
            throw new IllegalArgumentException("Unexpected argument " + arg);
        }
        return null;
    }

    @Override
    public D filterFrom(final DataContainerNode parentData) {
        for (var childArg : prototype.getChildArgs()) {
            if (parentData.childByArg(childArg) != null) {
                return createProxy(parentData);
            }
        }
        return null;
    }

    private @NonNull D createProxy(final @NonNull DataContainerNode parentData) {
        // FIXME: return createBindingProxy(parentData);
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamTo(final NormalizedNodeStreamWriter writer, final D data) throws IOException {
        eventStreamSerializer().serialize(requireNonNull(data), new BindingToNormalizedStreamWriter(this, writer));
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return filterFrom(checkDataArgument(DataContainerNode.class, normalizedNode));
    }
}
