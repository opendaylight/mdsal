/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A prototype for a {@link ContainerLikeCodecContext}.
 */
class ContainerLikeCodecPrototype<T extends ContainerLikeRuntimeType<?, ?>> extends DataObjectCodecPrototype<T> {
    ContainerLikeCodecPrototype(final Class<?> cls, final T type, final CodecContextFactory factory) {
        super(cls, NodeIdentifier.create(type.statement().argument()), type, factory);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    ContainerLikeCodecContext createInstance() {
        return new ContainerLikeCodecContext(this);
    }
}
