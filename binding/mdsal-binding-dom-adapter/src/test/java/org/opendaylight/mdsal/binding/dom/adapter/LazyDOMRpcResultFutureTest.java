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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class LazyDOMRpcResultFutureTest {

    private LazyDOMRpcResultFuture lazyDOMRpcResultFuture;
    private final BindingNormalizedNodeCodecRegistry codec = mock(BindingNormalizedNodeCodecRegistry.class);
    private final ListenableFuture future = mock(ListenableFuture.class);
    private final RpcResult domRpcResult = mock(RpcResult.class);

    @Before
    public void setUp() throws Exception {
        lazyDOMRpcResultFuture = (LazyDOMRpcResultFuture) LazyDOMRpcResultFuture.create(codec, future);
        reset(future);
        doReturn(true).when(future).cancel(anyBoolean());
        doNothing().when(future).addListener(any(), any());


        doReturn(true).when(domRpcResult).isSuccessful();
        doReturn(true).when(future).isCancelled();
        doReturn(true).when(future).isDone();

        doReturn(mock(DataContainer.class)).when(domRpcResult).getResult();
        doReturn(domRpcResult).when(future).get();
        doReturn(domRpcResult).when(future).get(1, TimeUnit.SECONDS);
    }

    @Test
    public void basicTest() throws Exception {
        assertNotNull(lazyDOMRpcResultFuture);
        assertEquals(future, lazyDOMRpcResultFuture.getBindingFuture());

        lazyDOMRpcResultFuture.cancel(true);
        verify(future).cancel(anyBoolean());

        lazyDOMRpcResultFuture.addListener(any(), any());
        verify(future).addListener(any(), any());

        assertTrue(lazyDOMRpcResultFuture.isCancelled() && lazyDOMRpcResultFuture.isDone());
        assertEquals(lazyDOMRpcResultFuture.checkedGet(), lazyDOMRpcResultFuture.get(1, TimeUnit.SECONDS));
        final Field result = LazyDOMRpcResultFuture.class.getDeclaredField("result");
        result.setAccessible(true);
        result.set(lazyDOMRpcResultFuture, null);
        assertEquals(lazyDOMRpcResultFuture.checkedGet(1, TimeUnit.SECONDS), lazyDOMRpcResultFuture.get());

        result.set(lazyDOMRpcResultFuture, null);
        doReturn(new Object()).when(domRpcResult).getResult();
        assertNotNull(lazyDOMRpcResultFuture.get());

        result.set(lazyDOMRpcResultFuture, null);
        doReturn(false).when(domRpcResult).isSuccessful();
        doReturn(ImmutableList.of()).when(domRpcResult).getErrors();
        assertNotNull(lazyDOMRpcResultFuture.get());
    }

    @Test(expected = InterruptedException.class)
    public void checkedGetWithException() throws Throwable {
        doThrow(InterruptedException.class).when(future).get();
        try {
            lazyDOMRpcResultFuture.checkedGet();
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }

    @Test(expected = InterruptedException.class)
    public void checkedGetWithException2() throws Throwable {
        doThrow(InterruptedException.class).when(future).get(1, TimeUnit.SECONDS);
        try {
            lazyDOMRpcResultFuture.checkedGet(1, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }
}