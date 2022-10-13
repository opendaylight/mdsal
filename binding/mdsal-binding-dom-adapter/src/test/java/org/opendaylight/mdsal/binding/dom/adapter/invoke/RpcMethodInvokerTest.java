/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;

@Deprecated(since = "11.0.0", forRemoval = true)
public class RpcMethodInvokerTest {
    private static final TestImplClassWithInput TEST_IMPL_CLASS = new TestImplClassWithInput();

    @Test
    public void invokeOnTest() throws Exception {
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(
                TestImplClassWithInput.class.getDeclaredMethod("testMethod", RpcService.class, DataObject.class));
        final RpcMethodInvoker rpcMethodInvokerWithInput = new RpcMethodInvoker(methodHandle);
        assertNotNull(rpcMethodInvokerWithInput.invokeOn(TEST_IMPL_CLASS, null));
    }

    @Test
    public void invokeOnWithException() throws Exception {
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(TestImplClassWithInput.class
                .getDeclaredMethod("testMethodWithException", RpcService.class, DataObject.class));
        final RpcMethodInvoker rpcMethodInvokerWithInput = new RpcMethodInvoker(methodHandle);

        assertThrows(InternalError.class, () -> rpcMethodInvokerWithInput.invokeOn(TEST_IMPL_CLASS, null));
    }

    static final class TestImplClassWithInput implements RpcService {

        static ListenableFuture<?> testMethod(final RpcService testArg, final DataObject data) {
            return Futures.immediateFuture(null);
        }

        static ListenableFuture<?> testMethodWithException(final RpcService testArg, final DataObject data) {
            throw new InternalError();
        }
    }
}