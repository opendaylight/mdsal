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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

final class GlobalDOMRpcRoutingTableEntry extends AbstractDOMRpcRoutingTableEntry {
    private GlobalDOMRpcRoutingTableEntry(final DOMRpcIdentifier rpcId,
            final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> impls) {
        super(rpcId, impls);
    }

    // We do not need the RpcDefinition, but this makes sure we do not
    // forward something we don't know to be an RPC.
    GlobalDOMRpcRoutingTableEntry(final @NonNull QName rpcName,
            final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> impls) {
        super(DOMRpcIdentifier.create(rpcName), impls);
    }

    @Override
    protected GlobalDOMRpcRoutingTableEntry newInstance(
            final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> impls) {
        return new GlobalDOMRpcRoutingTableEntry(getRpcId(), impls);
    }
}
