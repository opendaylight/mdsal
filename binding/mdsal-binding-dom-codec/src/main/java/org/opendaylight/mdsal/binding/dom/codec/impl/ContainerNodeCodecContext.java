/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

final class ContainerNodeCodecContext<D extends DataObject>
        extends DataObjectCodecContext<D, ContainerLikeRuntimeType<?, ?>>
        implements RpcInputCodec<D> {
    private static final VarHandle EMPTY_OBJECT;

    static {
        try {
            EMPTY_OBJECT = MethodHandles.lookup().findVarHandle(ContainerNodeCodecContext.class, "emptyObject",
                DataObject.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private volatile D emptyObject;

    ContainerNodeCodecContext(final DataContainerCodecPrototype<ContainerLikeRuntimeType<?, ?>> prototype) {
        super(prototype);
    }

    @Override
    public D deserialize(final NormalizedNode data) {
        if (data instanceof ContainerNode container) {
            return createBindingProxy(container);
        }
        throw new IllegalStateException("Unexpected data " + data);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    D emptyObject() {
        final D local;
        return (local = emptyObject) != null ? local : loadEmptyObject();
    }

    private @NonNull D loadEmptyObject() {
        final var domArg = getDomPathArgument();
        if (!(domArg instanceof NodeIdentifier nodeId)) {
            throw new IllegalStateException("Unexpected identifier " + domArg);
        }
        final var local = createBindingProxy(Builders.containerBuilder().withNodeIdentifier(nodeId).build());
        final var witness = (D) EMPTY_OBJECT.compareAndExchange(this, null, local);
        return witness != null ? witness : local;
    }
}
