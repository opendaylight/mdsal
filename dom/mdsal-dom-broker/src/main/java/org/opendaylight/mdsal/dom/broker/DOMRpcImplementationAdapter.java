/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class DOMRpcImplementationAdapter implements DOMRpcImplementation, DOMOperationImplementation {

    final DOMRpcImplementation impl;

    DOMRpcImplementationAdapter(final DOMRpcImplementation rpc) {
        this.impl = rpc;
    }

    @Override
    public void invokeOperation(@Nonnull DOMRpcIdentifier operation, @Nullable NormalizedNode<?, ?> input,
        @Nonnull BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        final CheckedFuture<DOMRpcResult, DOMRpcException> future = impl.invokeRpc(operation, input);
        future.addListener(() -> {
            try {
                DOMRpcResult result = future.checkedGet();
                callback.accept(result, null);
            } catch (DOMRpcException e) {
                callback.accept(null, e);
            }
        }, MoreExecutors.directExecutor());
    }

    @Nonnull
    @Override
    public CheckedFuture<DOMRpcResult, DOMRpcException> invokeRpc(@Nonnull DOMRpcIdentifier rpc,
            @Nullable NormalizedNode<?, ?> input) {
        return impl.invokeRpc(rpc, input);
    }
}