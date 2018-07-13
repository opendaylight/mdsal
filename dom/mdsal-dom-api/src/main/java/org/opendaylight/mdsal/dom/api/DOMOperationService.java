/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
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
     * @return A FluentFuture which completes with the result of invocation
     * @throws NullPointerException if any of the arguments is null
     */
    FluentFuture<DOMOperationResult> invokeRpc(QName type, ContainerNode input);

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
    FluentFuture<DOMOperationResult> invokeAction(SchemaPath type, DOMDataTreeIdentifier path, ContainerNode input);
}
