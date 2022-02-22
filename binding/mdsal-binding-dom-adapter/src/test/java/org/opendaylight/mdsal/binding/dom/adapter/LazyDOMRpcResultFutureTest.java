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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class LazyDOMRpcResultFutureTest {
    @Mock
    private BindingNormalizedNodeSerializer codec;
    @Mock
    private ListenableFuture<RpcResult<?>> future;
    @Mock
    private RpcResult<?> domRpcResult;

    private LazyDOMRpcResultFuture lazyDOMRpcResultFuture;

    @Before
    public void setUp() throws Exception {
        lazyDOMRpcResultFuture = new LazyDOMRpcResultFuture(codec, QName.create("foo", "foo"), future);
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
        assertEquals(lazyDOMRpcResultFuture.get(), lazyDOMRpcResultFuture.get(1, TimeUnit.SECONDS));

        lazyDOMRpcResultFuture.result = null;
        assertEquals(lazyDOMRpcResultFuture.get(1, TimeUnit.SECONDS), lazyDOMRpcResultFuture.get());

        lazyDOMRpcResultFuture.result = null;
        doReturn(new Object()).when(domRpcResult).getResult();
        assertNotNull(lazyDOMRpcResultFuture.get());

        lazyDOMRpcResultFuture.result = null;
        doReturn(false).when(domRpcResult).isSuccessful();
        doReturn(List.of()).when(domRpcResult).getErrors();
        assertNotNull(lazyDOMRpcResultFuture.get());
    }

    @Test
    public void checkedGetWithException() throws Exception {
        doThrow(InterruptedException.class).when(future).get();
        assertThrows(InterruptedException.class, lazyDOMRpcResultFuture::get);
    }

    @Test
    public void checkedGetWithException2() throws Exception {
        doThrow(InterruptedException.class).when(future).get(1, TimeUnit.SECONDS);
        assertThrows(InterruptedException.class, () -> lazyDOMRpcResultFuture.get(1, TimeUnit.SECONDS));
    }
}
