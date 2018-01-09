/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class UnknownDOMRpcRoutingTableEntry extends AbstractDOMRpcRoutingTableEntry {
    private final DOMRpcImplementationNotAvailableException unknownRpc;

    UnknownDOMRpcRoutingTableEntry(final SchemaPath schemaPath, final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        super(schemaPath, impls);
        unknownRpc =
            new DOMRpcImplementationNotAvailableException("SchemaPath %s is not resolved to an RPC", schemaPath);
    }

    @Override
    protected void invokeRpc(NormalizedNode<?, ?> input, BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        callback.accept(new DefaultDOMRpcResult(), unknownRpc);
    }

    @Override
    protected UnknownDOMRpcRoutingTableEntry newInstance(final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        return new UnknownDOMRpcRoutingTableEntry(getSchemaPath(), impls);
    }
}