/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.base;

/**
 *
 * The "action" statement is used to define an operation connected to a
 * specific container or list data node. The "action" statement defines
 * an action node in the schema tree. Under the action node, a schema node
 * with the name "input" and a schema node with the name "output" are also
 * defined. The nodes "input" and "output" are defined in the module’s namespace.
 *
 * The difference between an action and an rpc is that an action is tied
 * to a node in the datastore, whereas an rpc is not.
 *
 * Action replaces concept of routed RPC and comes up with implicit InstanceIdentifier
 * context, whereas routed RPC defines explicit leaf for this purpose.
 *
 */
@FunctionalInterface
public interface Action<P extends TreeNode, D extends InstanceIdentifier<P>, I extends Input<I> & Instantiable<I>,
    O extends Output<O> & Instantiable<O>> extends Operation {

    /**
     * @param input Action input schema node
     * @param ii implicit InstanceIdentifier connected to action, according to https://tools.ietf.org/html/rfc7950
     * @param callback on success/failure callback
     */
    void invoke(I input, D ii, RpcCallback<O> callback);
}
