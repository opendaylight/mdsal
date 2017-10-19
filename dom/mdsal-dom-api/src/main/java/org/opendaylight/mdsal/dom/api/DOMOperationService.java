/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A {@link DOMService} which allows clients to invoke Actions and RPCs. The conceptual model of this service is that
 * of a dynamic router, where the set of available Operation services can change dynamically. The service allows users
 * to add a listener to track the process of Actions and RPCs becoming available.
 */
@NonNullByDefault
public interface DOMOperationService extends DOMService {
    /**
     * Initiate invocation of an RPC. This method is guaranteed to not block on any external resources.
     *
     * @param type SchemaPath of the RPC to be invoked
     * @param input Input arguments
     * @param callback Callback to invoke with the invocation result
     * @param callbackExecutor Executor to use for executing the callback
     * @throws NullPointerException if any of the arguments is null
     */
    void invokeRpc(SchemaPath type, NormalizedNode<?, ?> input, DOMOperationCallback callback,
            Executor callbackExecutor);

    default FluentFuture<DOMRpcResult> invokeRpc(final SchemaPath type, final NormalizedNode<?, ?> input) {
        final SettableFuture<DOMRpcResult> future = SettableFuture.create();
        invokeRpc(type, input, DOMOperationCallback.completingFuture(future), MoreExecutors.directExecutor());
        return future;
    }

    /**
     * Initiate invocation of an Action. This method is guaranteed to not block on any external resources.
     *
     * @param type SchemaPath of the action to be invoked. This path refers to an effective action instantiated on top
     *             of the conceptual {@link StoreTreeNode}.
     * @param parent {@link YangInstanceIdentifier} of parent data node which action attached to. This identifier is
     *               evaluated against {@link LogicalDatastoreType#OPERATIONAL}.
     * @param input Input argument
     * @param callback Callback to invoke with the invocation result
     * @param callbackExecutor Executor to use for executing the callback
     * @throws NullPointerException if any of the arguments is null
     */
    void invokeAction(SchemaPath type, YangInstanceIdentifier parent,
            NormalizedNode<?, ?> input, DOMOperationCallback callback, Executor callbackExecutor);

    default FluentFuture<DOMRpcResult> invokeAction(final SchemaPath type, final YangInstanceIdentifier parent,
            final NormalizedNode<?, ?> input) {
        final SettableFuture<DOMRpcResult> future = SettableFuture.create();
        invokeAction(type, parent, input, DOMOperationCallback.completingFuture(future),
            MoreExecutors.directExecutor());
        return future;
    }

    /**
     * Register a {@link DOMOperationAvailabilityListener} with this service to receive notifications about Operation
     * implementations becoming (un)available. The listener will be invoked with the current implementations reported
     * and will be kept uptodate as implementations come and go.
     *
     * <p>
     * Users should note that using a listener does not necessarily mean that
     * {@link #invokeRpc(SchemaPath, NormalizedNode, DOMOperationCallback, Executor)} will not report a failure due to
     * {@link DOMRpcImplementationNotAvailableException} and need to be ready to handle it.
     *
     * <p>
     * Implementations are encouraged to take reasonable precautions to prevent this scenario from occurring.
     *
     * @param listener {@link DOMOperationAvailabilityListener} instance to register
     * @return A {@link ListenerRegistration} representing this registration. Performing a
     *         {@link ListenerRegistration#close()} will cancel it.
     */
    <T extends DOMOperationAvailabilityListener> ListenerRegistration<T> registerOperationListener(@Nonnull T listener);
}
