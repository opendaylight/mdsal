/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A {@link DOMService} which allows clients to invoke Actions and RPCs. The conceptual model of this service is that
 * of a dynamic router, where the set of available Operation services can change dynamically. The service allows users
 * to add a listener to track the process of Actions and RPCs becoming available.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface DOMOperationService extends DOMExtensibleService<DOMOperationService, DOMOperationServiceExtension> {
    /**
     * Initiate invocation of an RPC. This method is guaranteed to not block on any external resources.
     *
     * @param type QName of the RPC to be invoked
     * @param input Input arguments
     * @param callback Callback to invoke with the invocation result
     * @param callbackExecutor Executor to use for executing the callback
     * @throws NullPointerException if any of the arguments is null
     */
    void invokeRpc(QName type, NormalizedNode<?, ?> input, DOMOperationCallback callback,
            Executor callbackExecutor);

    /**
     * Initiate invocation of an RPC. This method is guaranteed to not block on any external resources.
     *
     * @param type QName of the RPC to be invoked
     * @param input Input arguments
     * @return A FluentFuture which completes with the result of invocation
     * @throws NullPointerException if any of the arguments is null
     */
    default FluentFuture<DOMOperationResult> invokeRpc(final QName type, final NormalizedNode<?, ?> input) {
        final SettableFuture<DOMOperationResult> future = SettableFuture.create();
        invokeRpc(type, input, DOMOperationCallback.completingFuture(future), directExecutor());
        return future;
    }

    /**
     * Initiate invocation of an Action. This method is guaranteed to not block on any external resources.
     *
     * @param type SchemaPath of the action to be invoked. This path refers to an effective action instantiated on top
     *             of the conceptual {@link StoreTreeNode}.
     * @param path {@link DOMDataTreeIdentifier} of parent data node which action attached to.
     * @param input Input argument
     * @param callback Callback to invoke with the invocation result
     * @param callbackExecutor Executor to use for executing the callback
     * @throws NullPointerException if any of the arguments is null
     */
    void invokeAction(SchemaPath type, DOMDataTreeIdentifier path, ContainerNode input, DOMOperationCallback callback,
            Executor callbackExecutor);

    /**
     * Initiate invocation of an Action. This method is guaranteed to not block on any external resources.
     *
     * @param type SchemaPath of the action to be invoked. This path refers to an effective action instantiated on top
     *             of the conceptual {@link StoreTreeNode}.
     * @param path {@link DOMDataTreeIdentifier} of parent data node which action attached to.
     * @param input Input argument
     * @return A FluentFuture which completes with the result of invocation
     * @throws NullPointerException if any of the arguments is null
     */
    default FluentFuture<DOMOperationResult> invokeAction(final SchemaPath type, final DOMDataTreeIdentifier path,
            final ContainerNode input) {
        final SettableFuture<DOMOperationResult> future = SettableFuture.create();
        invokeAction(type, path, input, DOMOperationCallback.completingFuture(future), directExecutor());
        return future;
    }
}
