/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Singleton codec for translating RPCs with implicit input statements, which are not mapped by binding spec v1. Since
 * there is no equivalent, we always return null.
 *
 * @author Robert Varga
 *
 * @param <D> Data object type
 */
final class UnmappedRpcContainerNodeCodecContext<D extends DataObject> extends AbstractContainerNodeCodecContext<D> {
    private static final UnmappedRpcContainerNodeCodecContext<?> INSTANCE = new UnmappedRpcContainerNodeCodecContext<>();

    private UnmappedRpcContainerNodeCodecContext() {
        super(null);
    }

    @SuppressWarnings("unchecked")
    static <D extends DataObject> UnmappedRpcContainerNodeCodecContext<D> getInstance() {
        return (UnmappedRpcContainerNodeCodecContext<D>) INSTANCE;
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        return null;
    }
}
