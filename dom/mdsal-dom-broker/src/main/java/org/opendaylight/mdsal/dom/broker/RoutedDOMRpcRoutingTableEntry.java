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
import java.util.Optional;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RoutedDOMRpcRoutingTableEntry extends AbstractDOMRpcRoutingTableEntry {
    private static final Logger LOG = LoggerFactory.getLogger(RoutedDOMRpcRoutingTableEntry.class);
    private final DOMRpcIdentifier globalRpcId;
    private final YangInstanceIdentifier keyId;

    private RoutedDOMRpcRoutingTableEntry(final DOMRpcIdentifier globalRpcId,
            final YangInstanceIdentifier keyId,
            final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> impls) {
        super(globalRpcId.getType(), impls);
        this.keyId = Preconditions.checkNotNull(keyId);
        this.globalRpcId = Preconditions.checkNotNull(globalRpcId);
    }

    RoutedDOMRpcRoutingTableEntry(final RpcDefinition def, final YangInstanceIdentifier keyId,
            final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> impls) {
        super(def.getPath(), impls);
        this.keyId = Preconditions.checkNotNull(keyId);
        this.globalRpcId = DOMRpcIdentifier.create(def.getPath());
    }

    @Override
    protected void invokeRpc(NormalizedNode<?, ?> input, BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        final Optional<NormalizedNode<?, ?>> maybeKey = NormalizedNodes.findNode(input, keyId);

        // Routing key is present, attempt to deliver as a routed RPC
        if (maybeKey.isPresent()) {
            final NormalizedNode<?, ?> key = maybeKey.get();
            final Object value = key.getValue();
            if (value instanceof YangInstanceIdentifier) {
                final YangInstanceIdentifier iid = (YangInstanceIdentifier) value;

                // Find a DOMOperationImplementation for a specific iid
                final List<DOMOperationImplementation> specificImpls = getImplementations(iid);
                if (specificImpls != null) {
                    specificImpls.get(0).invokeOperation(DOMRpcIdentifier.create(getSchemaPath(), iid), input,
                        callback);
                }

                LOG.debug("No implementation for context {} found will now look for wildcard id", iid);

                // Find a DOMOperationImplementation for a wild card. Usually remote-rpc-connector would register an
                // implementation this way
                final List<DOMOperationImplementation> mayBeRemoteImpls =
                    getImplementations(YangInstanceIdentifier.EMPTY);

                if (mayBeRemoteImpls != null) {
                    mayBeRemoteImpls.get(0).invokeOperation(DOMRpcIdentifier.create(getSchemaPath(), iid), input,
                        callback);
                }

            } else {
                LOG.warn("Ignoring wrong context value {}", value);
            }
        }

        final List<DOMOperationImplementation> impls = getImplementations(null);
        if (impls != null) {
            impls.get(0).invokeOperation(globalRpcId, input, callback);
        }

        callback.accept(new DefaultDOMRpcResult(),
            new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available", getSchemaPath()));
    }

    @Override
    protected RoutedDOMRpcRoutingTableEntry newInstance(final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        return new RoutedDOMRpcRoutingTableEntry(globalRpcId, keyId, impls);
    }
}
