/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Interface implemented by an individual RPC implementation. This API allows for dispatch
 * implementations, e.g. an individual object handling a multitude of RPCs.
 */
public interface DOMRpcImplementation {
    /**
     * Initiate invocation of the RPC. Implementations of this method are
     * expected to not block on external resources.
     *
     * @param rpc RPC identifier which was invoked
     * @param input Input arguments, null if the RPC does not take any.
     * @return A {@link ListenableFuture} which will return either a result structure,
     *         or report a subclass of {@link DOMRpcException} reporting a transport
     *         error.
     */
    // FIXME: 4.0.0: do not allow null input
    @NonNull ListenableFuture<DOMRpcResult> invokeRpc(@NonNull DOMRpcIdentifier rpc,
            @Nullable NormalizedNode<?, ?> input);

    /**
     * Return the relative invocation cost of this implementation. Default implementation return 0.
     *
     * @return Non-negative cost of invoking this implementation.
     */
    default long invocationCost() {
        return 0;
    }
}
