/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A {@link DOMService} which allows clients to invoke Actions and RPCs. The conceptual model of this
 * service is that of a dynamic router, where the set of available RPC services can change
 * dynamically. The service allows users to add a listener to track the process of
 * Actions and RPCs becoming available.
 */
public interface DOMOperationService extends DOMService {

    /**
     * Initiate invocation of an RPC. This method is guaranteed to not block on any external
     * resources.
     *
     * @param type SchemaPath of the RPC to be invoked
     * @param input Input arguments, null if the RPC does not take any.
     * @param callback result callback
     */
    void invokeRpc(@Nonnull SchemaPath type, @Nullable NormalizedNode<?, ?> input,
            @Nonnull BiConsumer<DOMRpcResult, DOMRpcException> callback);

    /**
     * Initiate invocation of an action. This method is guaranteed to not block on any external
     * resources.
     *
     * @param type SchemaPath of the action to be invoked
     * @param parent Yang instance identifier of parent data node which action attached to.
     * @param input Input arguments, null if the RPC does not take any.
     * @param callback result callback
     */
    void invokeAction(@Nonnull SchemaPath type, @Nonnull YangInstanceIdentifier parent,
            @Nullable NormalizedNode<?, ?> input,  @Nonnull BiConsumer<DOMRpcResult, DOMRpcException> callback);

    /**
     * Register a {@link DOMRpcAvailabilityListener} with this service to receive notifications
     * about RPC implementations becoming (un)available. The listener will be invoked with the
     * current implementations reported and will be kept uptodate as implementations come and go.
     * Users should note that using a listener does not necessarily mean that
     * {@link #invokeRpc(SchemaPath, NormalizedNode, BiConsumer)} will not report a failure due to
     * {@link DOMRpcImplementationNotAvailableException} and need to be ready to handle it.
     * Implementations are encouraged to take reasonable precautions to prevent this scenario from
     * occurring.
     *
     * @param listener {@link DOMRpcAvailabilityListener} instance to register
     * @return A {@link ListenerRegistration} representing this registration. Performing a
     *         {@link ListenerRegistration#close()} will cancel it. Returned object is guaranteed to
     *         be non-null.
     */
    @Nonnull <T extends DOMOperationAvailabilityListener> ListenerRegistration<T>
    registerOperationListener(@Nonnull T listener);
}
