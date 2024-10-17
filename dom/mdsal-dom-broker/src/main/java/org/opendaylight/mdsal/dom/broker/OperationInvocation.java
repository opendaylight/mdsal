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
import org.opendaylight.mdsal.dom.api.DOMActionResult;
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

    static ListenableFuture<? extends DOMActionResult> invoke(final DOMActionRoutingTableEntry entry,
            final Absolute type, final DOMDataTreeIdentifier path, final ContainerNode input) {
        var impls = entry.getImplementations(path);
        if (impls == null) {
            impls = entry.getImplementations(
                new DOMDataTreeIdentifier(path.getDatastoreType(), YangInstanceIdentifier.of()));
            if (impls == null) {
                return Futures.immediateFailedFuture(new DOMActionNotAvailableException(
                    "No implementation of Action %s available for %s", type, path));
            }
        }

        return invokeAction(impls.get(0), type, path, input);
    }

    static ListenableFuture<? extends DOMRpcResult> invoke(final AbstractDOMRpcRoutingTableEntry entry,
            final ContainerNode input) {
        if (entry instanceof UnknownDOMRpcRoutingTableEntry) {
            return Futures.immediateFailedFuture(new DOMRpcImplementationNotAvailableException(
                "%s is not resolved to an RPC", entry.getType()));
        } else if (entry instanceof RoutedDOMRpcRoutingTableEntry routed) {
            return invokeRoutedRpc(routed, input);
        } else if (entry instanceof GlobalDOMRpcRoutingTableEntry global) {
            return invokeGlobalRpc(global, input);
        }

        return Futures.immediateFailedFuture(new DOMRpcImplementationNotAvailableException("Unsupported RPC entry."));
    }

    private static ListenableFuture<? extends DOMRpcResult> invokeRoutedRpc(
            final RoutedDOMRpcRoutingTableEntry entry, final ContainerNode input) {
        final var maybeKey = NormalizedNodes.findNode(input, entry.getRpcId().getContextReference());

        // Routing key is present, attempt to deliver as a routed RPC
        if (maybeKey.isPresent()) {
            final var key = maybeKey.orElseThrow();
            final var value = key.body();
            if (value instanceof YangInstanceIdentifier iid) {
                // Find a DOMRpcImplementation for a specific iid
                final var specificImpls = entry.getImplementations(iid);
                if (specificImpls != null) {
                    return invokeRpc(specificImpls.get(0), DOMRpcIdentifier.create(entry.getType(), iid), input);
                }

                LOG.debug("No implementation for context {} found will now look for wildcard id", iid);

                // Find a DOMRpcImplementation for a wild card. Usually remote-rpc-connector would register an
                // implementation this way
                final var mayBeRemoteImpls = entry.getImplementations(YangInstanceIdentifier.of());
                if (mayBeRemoteImpls != null) {
                    return invokeRpc(mayBeRemoteImpls.get(0), DOMRpcIdentifier.create(entry.getType(), iid), input);
                }
            } else {
                LOG.warn("Ignoring wrong context value {}", value);
            }
        }

        final var impls = entry.getImplementations(null);
        if (impls != null) {
            return invokeRpc(impls.get(0), entry.getRpcId(), input);
        }

        return Futures.immediateFailedFuture(new DOMRpcImplementationNotAvailableException(
            "No implementation of RPC %s available", entry.getType()));
    }

    private static ListenableFuture<? extends DOMRpcResult> invokeGlobalRpc(
            final GlobalDOMRpcRoutingTableEntry entry, final ContainerNode input) {
        return invokeRpc(entry.getImplementations(YangInstanceIdentifier.of()).get(0), entry.getRpcId(), input);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static ListenableFuture<? extends DOMActionResult> invokeAction(final DOMActionImplementation impl,
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