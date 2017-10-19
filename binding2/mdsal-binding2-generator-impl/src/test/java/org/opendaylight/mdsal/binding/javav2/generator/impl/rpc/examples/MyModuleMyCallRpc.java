/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl.rpc.examples;

import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;

/**
 * Example RPC interface.
 * It represents following YANG snippet:
 *
 * <p>
 * module my-module {
 *
 * <p>
 *  rpc my-call {
 *
 * <p>
 *     input {
 *       leaf v1 {
 *          type string;
 *       }
 *     }
 *
 * <p>
 *     output {
 *       leaf v2 {
 *          type string
 *       }
 *     }
 *  }
 * }
 */
public interface MyModuleMyCallRpc extends Rpc<MyCallInput, MyCallOutput> {

    @Override
    void invoke(MyCallInput input, RpcCallback<MyCallOutput> callback);
}
