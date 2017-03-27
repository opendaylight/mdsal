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
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Abstract base class for container-like serialization. This class exists only as a type capture, with two separate
 * implementations. The reason for this is that for RPCs we need an unmapped version, which always returns null,
 * but we do not want to burden normal data transcoding paths with bimorphic execution -- those should be bound to
 * a final class.
 *
 * @author Robert Varga
 *
 * @param <D> Object type
 */
abstract class AbstractContainerNodeCodecContext<D extends DataObject>
        extends DataObjectCodecContext<D, ContainerSchemaNode> {

    AbstractContainerNodeCodecContext(final DataContainerCodecPrototype<ContainerSchemaNode> prototype) {
        super(prototype);
    }

    @Override
    protected final Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }
}
