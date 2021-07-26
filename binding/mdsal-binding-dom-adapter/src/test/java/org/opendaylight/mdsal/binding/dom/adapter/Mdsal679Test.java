/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.Input;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.OutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class Mdsal679Test extends AbstractAdapterTest {
    @Mock
    private DOMActionProviderService actionProvider;

    private ActionProviderService adapter;

    @Override
    @Before
    public void before() {
        super.before();
        adapter = new ActionProviderServiceAdapter(codec, actionProvider);
    }

    @Test
    public void testSingleAction() {
        adapter.registerImplementation(Foo.class,
            (path, input) -> RpcResultBuilder.success(new OutputBuilder().build()).buildFuture(),
            LogicalDatastoreType.OPERATIONAL, Set.of(InstanceIdentifier.create(Cont.class)));

        verify(actionProvider).registerActionImplementation(any(), eq(Set.of(DOMActionInstance.of(
            Absolute.of(Cont.QNAME, Foo.QNAME),
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
                YangInstanceIdentifier.create(new NodeIdentifier(Cont.QNAME)))))));
    }

    @Test
    public void testWildcardAction() {
        adapter.registerImplementation(Foo.class,
            (path, input) -> RpcResultBuilder.success(new OutputBuilder().build()).buildFuture());
        verify(actionProvider).registerActionImplementation(any(), eq(Set.of()));
    }

    private static ContainerNode createInput() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(Input.QNAME))
            .withChild(ImmutableNodes.leafNode(QName.create(Input.QNAME, "xyzzy"), "value"))
            .build();
    }
}
