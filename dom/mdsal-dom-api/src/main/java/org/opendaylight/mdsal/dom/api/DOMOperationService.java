/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A {@link DOMService} which allows clients to invoke Actions and RPCs. The conceptual model of this
 * service is that of a dynamic router, where the set of available RPC services can change
 * dynamically. The service allows users to add a listener to track the process of
 * Actions and RPCs becoming available.
 */
public interface DOMOperationService extends DOMRpcService {

    /**
     * Initiate invocation of an action. This method is guaranteed to not block on any external
     * resources.
     *
     * @param type SchemaPath of the RPC to be invoked
     * @param parent Yang instance identifier of parent data node which action attached to.
     * @param input Input arguments, null if the RPC does not take any.
     * @return A {@link CheckedFuture} which will return either a result structure,
     *         or report a subclass of {@link DOMRpcException} reporting a transport
     *         error.
     */
    @Nonnull
    CheckedFuture<DOMRpcResult, DOMRpcException>
        invokeAction(@Nonnull SchemaPath type, @Nonnull YangInstanceIdentifier parent,
            @Nullable NormalizedNode<?, ?> input);

}
