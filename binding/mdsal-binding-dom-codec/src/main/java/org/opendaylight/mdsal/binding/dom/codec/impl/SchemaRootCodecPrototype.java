/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Prototype for the root of YANG modeled world.
 */
final class SchemaRootCodecPrototype extends DataObjectCodecPrototype<BindingRuntimeTypes> {
    private static final @NonNull NodeIdentifier ROOT_NODEID = NodeIdentifier.create(SchemaContext.NAME);

    SchemaRootCodecPrototype(final CodecContextFactory factory) {
        super(DataRoot.class, ROOT_NODEID, factory.getRuntimeContext().getTypes(), factory);
    }

    @Override
    DataContainerCodecContext<?, BindingRuntimeTypes> createInstance() {
        throw new UnsupportedOperationException("Should never be invoked");
    }
}
