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
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ActionProviderServiceAdapterTest extends AbstractActionAdapterTest {
    private static final @NonNull Foo FOO = (path, input) -> RpcResultBuilder.success(BINDING_FOO_OUTPUT).buildFuture();

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
    public void testInstanceRegistration() {
        adapter.registerImplementation(ActionSpec.builder(Cont.class).build(Foo.class), FOO,
            LogicalDatastoreType.OPERATIONAL, Set.of(InstanceIdentifier.create(Cont.class)));

        verify(actionProvider).registerActionImplementation(any(), eq(DOMActionInstance.of(FOO_PATH,
            LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.create(new NodeIdentifier(Cont.QNAME)))));
    }

    @Test
    public void testWildcardRegistration() {
        adapter.registerImplementation(ActionSpec.builder(Cont.class).build(Foo.class), FOO);
        verify(actionProvider).registerActionImplementation(any(), eq(DOMActionInstance.of(FOO_PATH,
            LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.empty())));
    }
}
