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
 * Interface implemented by an individual operation implementation. This API allows for dispatch implementations, e.g.
 * an individual object handling a multitude of operations.
 */
@FunctionalInterface
@NonNullByDefault
public non-sealed interface DOMActionImplementation extends DOMOperationImplementation {
    /**
     * Initiate invocation of the action. Implementations of this method are expected to not block.
     *
     * @param type absolute schema node identifier of the action to be invoked. This path refers to an effective action
     *             instantiated on top of the conceptual {@link StoreTreeNode}.
     * @param path {@link DOMDataTreeIdentifier} of parent data node which action attached to.
     * @param input Input arguments
     * @return A {@link ListenableFuture} which completes with the result of invocation
     * @throws NullPointerException if any of the arguments is null
     */
    ListenableFuture<? extends DOMRpcResult> invokeAction(Absolute type, DOMDataTreeIdentifier path,
        ContainerNode input);
}
