/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * A {@link DOMService} which allows clients to invoke Actions. The conceptual model of this service is that
 * of a dynamic router, where the set of available Action services can change dynamically.
 */
@NonNullByDefault
public interface DOMActionService extends DOMService<DOMActionService, DOMActionService.Extension> {
    /**
     * Marker interface for extensions of {@link DOMActionService}.
     */
    interface Extension extends DOMService.Extension<DOMActionService, Extension> {
        // Marker interface
    }

    /**
     * Initiate invocation of an Action. This method is guaranteed to not block on any external resources.
     *
     * @param type Absolute schema node identifier of the action to be invoked. This path refers to an effective action
     *             instantiated on top of the conceptual {@link StoreTreeNode}.
     * @param path {@link DOMDataTreeIdentifier} of parent data node which action attached to.
     * @param input Input argument
     * @return A {@link ListenableFuture} which completes with the result of invocation
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if {@code path} is empty
     */
    ListenableFuture<? extends DOMRpcResult> invokeAction(Absolute type, DOMDataTreeIdentifier path,
            ContainerNode input);
}
