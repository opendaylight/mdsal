/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

final class GlobalDOMRpcRoutingTableEntry extends AbstractDOMRpcRoutingTableEntry {
    private static final YangInstanceIdentifier ROOT = YangInstanceIdentifier.builder().build();
    private final DOMRpcIdentifier rpcId;

    private GlobalDOMRpcRoutingTableEntry(final DOMRpcIdentifier rpcId, final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        super(rpcId.getType(), impls);
        this.rpcId = Preconditions.checkNotNull(rpcId);
    }

    // We do not need the RpcDefinition, but this makes sure we do not
    // forward something we don't know to be an RPC.
    GlobalDOMRpcRoutingTableEntry(final RpcDefinition def, final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        super(def.getPath(), impls);
        this.rpcId = DOMRpcIdentifier.create(def.getPath());
    }

    @Override
    protected void invokeRpc(final NormalizedNode<?, ?> input,
            final BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        getImplementations(ROOT).get(0).invokeOperation(rpcId, input, callback);
    }

    @Override
    protected GlobalDOMRpcRoutingTableEntry newInstance(final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        return new GlobalDOMRpcRoutingTableEntry(rpcId, impls);
    }
}