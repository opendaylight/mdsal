/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

public class ClassBaseOperationServiceInvokerTest {

    @Test
    public void rpcTest() {
        final OperationServiceInvoker serviceInvoker = ClassBasedOperationServiceInvoker.instanceFor(DummyRpc.class);
        assertNotNull(serviceInvoker);
    }

    @Test
    public void actionTest() {
        final OperationServiceInvoker serviceInvoker = ClassBasedOperationServiceInvoker.instanceFor(DummyAction.class);
        assertNotNull(serviceInvoker);
    }

    @SuppressWarnings("rawtypes")
    private interface DummyRpc extends Rpc, TreeNode {
    }

    @SuppressWarnings("rawtypes")
    private interface DummyAction extends Action, TreeNode {
    }
}
