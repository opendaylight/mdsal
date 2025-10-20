/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.common.RpcResult;

@ExtendWith(MockitoExtension.class)
class LazyDOMRpcResultFutureTest {
    @Mock
    private BindingNormalizedNodeSerializer codec;
    @Mock
    private ListenableFuture<RpcResult<?>> future;
    @Mock
    private RpcResult<?> domRpcResult;
    @Mock
    private RpcOutput output;

    private LazyDOMRpcResultFuture lazyDOMRpcResultFuture;

    @BeforeEach
    void beforeEach() throws Exception {
        lazyDOMRpcResultFuture = LazyDOMRpcResultFuture.create(codec, future);
        doReturn(true).when(future).cancel(anyBoolean());
        doNothing().when(future).addListener(any(), any());

        doReturn(true).when(domRpcResult).isSuccessful();
        doReturn(true).when(future).isCancelled();
        doReturn(true).when(future).isDone();

        doReturn(output).when(domRpcResult).getResult();
        doReturn(domRpcResult).when(future).get();
        doReturn(domRpcResult).when(future).get(1, TimeUnit.SECONDS);
    }

    @Test
    void basicTest() throws Exception {
        assertNotNull(lazyDOMRpcResultFuture);
        assertEquals(future, lazyDOMRpcResultFuture.getBindingFuture());

        lazyDOMRpcResultFuture.cancel(true);
        verify(future).cancel(anyBoolean());

        lazyDOMRpcResultFuture.addListener(any(), any());
        verify(future).addListener(any(), any());

        assertTrue(lazyDOMRpcResultFuture.isCancelled() && lazyDOMRpcResultFuture.isDone());
        assertEquals(lazyDOMRpcResultFuture.get(), lazyDOMRpcResultFuture.get(1, TimeUnit.SECONDS));
        final Field result = LazyDOMRpcResultFuture.class.getDeclaredField("result");
        result.setAccessible(true);
        result.set(lazyDOMRpcResultFuture, null);
        assertEquals(lazyDOMRpcResultFuture.get(1, TimeUnit.SECONDS), lazyDOMRpcResultFuture.get());

        result.set(lazyDOMRpcResultFuture, null);
        doReturn(new Object()).when(domRpcResult).getResult();
        assertNotNull(lazyDOMRpcResultFuture.get());

        result.set(lazyDOMRpcResultFuture, null);
        doReturn(false).when(domRpcResult).isSuccessful();
        doReturn(ImmutableList.of()).when(domRpcResult).getErrors();
        assertNotNull(lazyDOMRpcResultFuture.get());
    }
}
