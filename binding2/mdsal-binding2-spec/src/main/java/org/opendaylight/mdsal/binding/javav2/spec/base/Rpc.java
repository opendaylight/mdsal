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
 * The "rpc" statement is used to define an RPC operation. The "rpc" statement
 * defines an RPC node in the schema tree. Under the RPC node, a schema node
 * with the name "input" and a schema node with the name "output" are also defined.
 * The nodes "input" and "output" are defined in the module’s namespace.
 *
 */
@FunctionalInterface
public interface Rpc<I extends Input<I> & Instantiable<I>, O extends Output<O> & Instantiable<O>> extends Operation {

    /**
     * @param input Rpc input schema node
     * @param callback on success/failure callback
     */
    void invoke(I input, RpcCallback<O> callback);
}
