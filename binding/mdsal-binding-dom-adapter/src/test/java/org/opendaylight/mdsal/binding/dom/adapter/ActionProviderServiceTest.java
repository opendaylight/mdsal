/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
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

public class ActionProviderServiceTest {

    private static final QName FOO_XYZZY_QNAME = QName.create(Foo.QNAME, "xyzzy");
    private DOMRpcRouter rpcRouter;
    private ActionProviderService actionProviderServiceAdapter;


    @Before
    public void before() {
        this.rpcRouter = new DOMRpcRouter();
        BindingCodecContext bindingCodecContext = new BindingCodecContext(BindingRuntimeHelpers.createRuntimeContext());
        AdapterContext adapterContext = new ConstantAdapterContext(bindingCodecContext);
        this.actionProviderServiceAdapter
                = new ActionProviderServiceAdapter(adapterContext, rpcRouter.getActionProviderService());
    }

    @After
    public void tearDown() {
        this.rpcRouter.close();
    }

    @Test
    public void testActionRegistration() throws ExecutionException, InterruptedException {
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(Cont.QNAME).node(Foo.QNAME);
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier);
        final Set<DataTreeIdentifier<Cont>> dataTreeIdentifierSet = new HashSet<>();
        dataTreeIdentifierSet.add(
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,InstanceIdentifier.create(Cont.class)));
        // Register Foo Action
        this.actionProviderServiceAdapter.registerImplementation(Foo.class, new FooImpl(),
                LogicalDatastoreType.OPERATIONAL, dataTreeIdentifierSet);
        //Invoke Foo Action
        ContainerNode inputAction = getInputActionContainer(FOO_XYZZY_QNAME, "value");
        ListenableFuture<? extends DOMActionResult> action = rpcRouter.getActionService()
                .invokeAction(SchemaNodeIdentifier.Absolute.of(Cont.QNAME, Foo.QNAME),
                        domDataTreeIdentifier, inputAction);
        //Verify Foo Action result
        DOMActionResult domActionResult = action.get();
        Assert.assertNotNull(domActionResult.getOutput());
        Assert.assertTrue(domActionResult.getOutput().isPresent());
    }

    private ContainerNode getInputActionContainer(final QName qname, final String value) {
        final ImmutableLeafNodeBuilder<String> immutableLeafNodeBuilder = new ImmutableLeafNodeBuilder<>();
        final DataContainerChild leafNode = immutableLeafNodeBuilder
                .withNodeIdentifier(NodeIdentifier.create(qname))
                .withValue(value)
                .build();
        return ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(NodeIdentifier.create(QName.create(qname, "input")))
                .withChild(leafNode)
                .build();
    }

    private static class FooImpl implements Foo {
        @Override
        public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull Output>> invoke(
                @NonNull InstanceIdentifier<Cont> path, @NonNull Input input) {
            return Futures.immediateFuture(RpcResultBuilder.success(new OutputBuilder().build()).build());
        }
    }
}
