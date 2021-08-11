/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.spi.SimpleDOMActionResult;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.Output;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ActionServiceAdapterTest extends AbstractActionAdapterTest {
    @Mock
    private DOMActionService delegate;

    private ActionService service;

    private SettableFuture<DOMActionResult> domResult;

    @Override
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        super.before();

        domResult = SettableFuture.create();
        doReturn(domResult).when(delegate).invokeAction(any(), any(), any());

        service = new ActionServiceAdapter(codec, delegate);
    }

    @Test
    public void testInvocation() throws ExecutionException {
        final Foo handle = service.getActionHandle(Foo.class, Set.of());
        final ListenableFuture<RpcResult<Output>> future = handle.invoke(InstanceIdentifier.create(Cont.class),
            BINDING_FOO_INPUT);
        assertNotNull(future);
        assertFalse(future.isDone());
        domResult.set(new SimpleDOMActionResult(DOM_FOO_OUTPUT, List.of()));
        final RpcResult<Output> bindingResult = Futures.getDone(future);

        assertEquals(List.of(), bindingResult.getErrors());
        assertEquals(BINDING_FOO_OUTPUT, bindingResult.getResult());
    }
}
