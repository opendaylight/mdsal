/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationOutputQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.Input;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.InputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.Output;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.OutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class ActionServiceAdapterTest extends AbstractAdapterTest {
    private static final NodeIdentifier FOO_INPUT = NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier FOO_OUTPUT = NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier FOO_XYZZY = NodeIdentifier.create(QName.create(Foo.QNAME, "xyzzy"));
    private static final ContainerNode DOM_FOO_INPUT = containerBuilder().withNodeIdentifier(FOO_INPUT)
            .withChild(leafBuilder().withNodeIdentifier(FOO_XYZZY).withValue("xyzzy").build())
            .build();
    private static final ContainerNode DOM_FOO_OUTPUT = containerBuilder().withNodeIdentifier(FOO_OUTPUT).build();
    private static final Input BINDING_FOO_INPUT = new InputBuilder().setXyzzy("xyzzy").build();
    private static final RpcOutput BINDING_FOO_OUTPUT = new OutputBuilder().build();

    @Mock
    private DOMOperationService delegate;

    private ActionService service;

    private SettableFuture<DOMOperationResult> domResult;

    @Override
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        super.before();

        doReturn(domResult).when(delegate).invokeAction(any(), any(), any());

        service = new ActionServiceAdapter(codec, delegate);
    }

    @Test
    public void testInvocation() {
        final Foo handle = service.getActionHandle(Foo.class, ImmutableSet.of());
        final FluentFuture<RpcResult<Output>> future = handle.invoke(InstanceIdentifier.create(Cont.class),
            BINDING_FOO_INPUT);
        assertNotNull(future);
    }

}
