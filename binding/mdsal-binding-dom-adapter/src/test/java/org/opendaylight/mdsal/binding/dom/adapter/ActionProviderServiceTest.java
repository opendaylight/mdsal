/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.Input;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.Output;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.OutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

public class ActionProviderServiceTest extends AbstractAdapterTest {
    private static final NodeIdentifier INPUT_ID = NodeIdentifier.create(Input.QNAME);
    private static final QName FOO_XYZZY_QNAME = QName.create(Input.QNAME, "xyzzy").intern();
    private static final NodeIdentifier FOO_XYZZY_ID = NodeIdentifier.create(FOO_XYZZY_QNAME);

    private DOMRpcRouter rpcRouter;
    private ActionProviderService actionProviderServiceAdapter;

    @Override
    @Before
    public void before() {
        super.before();
        this.rpcRouter = new DOMRpcRouter();
        this.rpcRouter.onModelContextUpdated(codec.currentSerializer().getRuntimeContext().getEffectiveModelContext());
        this.actionProviderServiceAdapter = new ActionProviderServiceAdapter(codec,
                rpcRouter.getActionProviderService());
    }

    @After
    public void tearDown() {
        this.rpcRouter.close();
    }

    @Test
    public void testActionRegistration() throws ExecutionException, InterruptedException {
        // register action Foo
        this.actionProviderServiceAdapter.registerImplementation(Foo.class, new FooImpl());
        // invoke action Foo
        final YangInstanceIdentifier actionYII = YangInstanceIdentifier.of(Cont.QNAME).node(Foo.QNAME);
        final DOMDataTreeIdentifier treeId = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
                actionYII.getParent());
        final ListenableFuture<? extends DOMActionResult> action = rpcRouter.getActionService()
                .invokeAction(SchemaNodeIdentifier.Absolute.of(Cont.QNAME, Foo.QNAME), treeId, createInput());
        // verify action Foo result
        final DOMActionResult domActionResult = action.get();
        assertNotNull(domActionResult.getOutput());
        assertTrue(domActionResult.getOutput().isPresent());
        assertTrue(domActionResult.getErrors().isEmpty());
    }

    private static ContainerNode createInput() {
        final DataContainerChild xyzzyNode = new ImmutableLeafNodeBuilder<>()
                .withNodeIdentifier(FOO_XYZZY_ID)
                .withValue("value")
                .build();
        return ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(INPUT_ID)
                .withChild(xyzzyNode)
                .build();
    }

    private static class FooImpl implements Foo {
        @Override
        public ListenableFuture<RpcResult<Output>> invoke(final InstanceIdentifier<Cont> path, final Input input) {
            return Futures.immediateFuture(RpcResultBuilder.success(new OutputBuilder().build()).build());
        }
    }
}
