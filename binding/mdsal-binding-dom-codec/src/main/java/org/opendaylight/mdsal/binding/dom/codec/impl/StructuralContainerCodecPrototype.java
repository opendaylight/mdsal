/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;

/**
 * A prototype for a {@link StructuralContainerCodecContext}.
 */
final class StructuralContainerCodecPrototype extends ContainerLikeCodecPrototype<ContainerRuntimeType> {
    StructuralContainerCodecPrototype(final Class<?> cls, final ContainerRuntimeType type,
            final CodecContextFactory factory) {
        super(cls, type, factory);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    StructuralContainerCodecContext createInstance() {
        return new StructuralContainerCodecContext(this);
    }
}
