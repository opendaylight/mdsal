/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

final class GlobalDOMRpcRoutingTableEntry extends AbstractDOMRpcRoutingTableEntry {
    private final DOMRpcIdentifier rpcId;

    private GlobalDOMRpcRoutingTableEntry(final DOMRpcIdentifier rpcId, final Map<YangInstanceIdentifier,
            List<DOMRpcImplementation>> impls) {
        super(rpcId.getType(), impls);
        this.rpcId = Preconditions.checkNotNull(rpcId);
    }

    // We do not need the RpcDefinition, but this makes sure we do not
    // forward something we don't know to be an RPC.
    GlobalDOMRpcRoutingTableEntry(final RpcDefinition def, final Map<YangInstanceIdentifier,
            List<DOMRpcImplementation>> impls) {
        super(def.getPath(), impls);
        this.rpcId = DOMRpcIdentifier.create(def.getPath());
    }

    @Override
    protected FluentFuture<DOMRpcResult> invokeRpc(final NormalizedNode<?, ?> input) {
        return getImplementations(YangInstanceIdentifier.EMPTY).get(0).invokeRpc(rpcId, input);
    }

    @Override
    protected GlobalDOMRpcRoutingTableEntry newInstance(final Map<YangInstanceIdentifier,
            List<DOMRpcImplementation>> impls) {
        return new GlobalDOMRpcRoutingTableEntry(rpcId, impls);
    }
}
