/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DefaultDOMRpcException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class OperationInvocation {
    private static final Logger LOG = LoggerFactory.getLogger(OperationInvocation.class);

    private OperationInvocation() {
        // hidden on purpose
    }

    static ListenableFuture<? extends DOMRpcResult> invoke(final DOMActionRoutingTableEntry entry,
            final Absolute type, final DOMDataTreeIdentifier path, final ContainerNode input) {
        var impls = entry.getImplementations(path);
        if (impls == null) {
            impls = entry.getImplementations(DOMDataTreeIdentifier.of(path.datastore(), YangInstanceIdentifier.of()));
            if (impls == null) {
                return Futures.immediateFailedFuture(new DOMActionNotAvailableException(
                    "No implementation of Action %s available for %s", type, path));
            }
        }

        return invokeAction(impls.getFirst(), type, path, input);
    }

    static ListenableFuture<? extends DOMRpcResult> invoke(final DOMRpcRoutingTableEntry entry,
            final ContainerNode input) {
        return switch (entry) {
            case GlobalDOMRpcRoutingTableEntry global -> invokeGlobalRpc(global, input);
            case RoutedDOMRpcRoutingTableEntry routed -> invokeRoutedRpc(routed, input);
            case UnknownDOMRpcRoutingTableEntry unknown -> Futures.immediateFailedFuture(
                new DOMRpcImplementationNotAvailableException("%s is not resolved to an RPC", entry.getType()));
        };
    }

    private static ListenableFuture<? extends DOMRpcResult> invokeGlobalRpc(final GlobalDOMRpcRoutingTableEntry entry,
            final ContainerNode input) {
        return invokeRpc(entry.getImplementations(YangInstanceIdentifier.of()).getFirst(), entry.getRpcId(), input);
    }

    private static ListenableFuture<? extends DOMRpcResult> invokeRoutedRpc(final RoutedDOMRpcRoutingTableEntry entry,
            final ContainerNode input) {
        final var maybeKey = NormalizedNodes.findNode(input, entry.getRpcId().getContextReference());

        // Routing key is present, attempt to deliver as a routed RPC
        if (maybeKey.isPresent()) {
            final var key = maybeKey.orElseThrow();
            final var value = key.body();
            if (value instanceof YangInstanceIdentifier iid) {
                // Find a DOMRpcImplementation for a specific iid
                final var specificImpls = entry.getImplementations(iid);
                if (specificImpls != null) {
                    return invokeRpc(specificImpls.getFirst(), DOMRpcIdentifier.create(entry.getType(), iid), input);
                }

                LOG.debug("No implementation for context {} found will now look for wildcard id", iid);

                // Find a DOMRpcImplementation for a wild card. Usually remote-rpc-connector would register an
                // implementation this way
                final var mayBeRemoteImpls = entry.getImplementations(YangInstanceIdentifier.of());
                if (mayBeRemoteImpls != null) {
                    return invokeRpc(mayBeRemoteImpls.getFirst(), DOMRpcIdentifier.create(entry.getType(), iid), input);
                }
            } else {
                LOG.warn("Ignoring wrong context value {}", value);
            }
        }

        final var impls = entry.getImplementations(null);
        if (impls != null) {
            return invokeRpc(impls.getFirst(), entry.getRpcId(), input);
        }

        return Futures.immediateFailedFuture(new DOMRpcImplementationNotAvailableException(
            "No implementation of RPC %s available", entry.getType()));
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static ListenableFuture<? extends DOMRpcResult> invokeAction(final DOMActionImplementation impl,
            final Absolute type, final DOMDataTreeIdentifier path, final ContainerNode input) {
        try {
            return impl.invokeAction(type, path, input);
        } catch (Exception e) {
            LOG.debug("{} failed on {} with {}", impl, path, input.prettyTree(), e);
            return Futures.immediateFailedFuture(new DefaultDOMRpcException("Action implementation failed: " + e, e));
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static ListenableFuture<? extends DOMRpcResult> invokeRpc(final DOMRpcImplementation impl,
            final DOMRpcIdentifier rpc, final ContainerNode input) {
        try {
            return impl.invokeRpc(rpc, input);
        } catch (Exception e) {
            LOG.debug("{} failed on {} with {}", impl, rpc, input.prettyTree(), e);
            return Futures.immediateFailedFuture(new DefaultDOMRpcException("RPC implementation failed: " + e, e));
        }
    }
}