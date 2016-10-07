/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class AbstractMappedRpcInvokerTest implements RpcService {

    @Test
    public void invokeRpcTest() throws Exception {
        final Method methodWithoutInput =
                this.getClass().getDeclaredMethod("methodWithoutInput", RpcService.class);
        final Method methodWithInput =
                this.getClass().getDeclaredMethod("methodWithInput", RpcService.class, DataObject.class);

        final RpcServiceInvoker testRpcInvokerWithoutInput =
                new TestRpcInvokerImpl(ImmutableMap.of("tstWithoutInput", methodWithoutInput));
        final Crate crateWithoutInput =
                (Crate) testRpcInvokerWithoutInput.invokeRpc(this, QName.create("tstWithoutInput"), null).get();
        assertEquals(methodWithoutInput(this).get().getRpcService(), crateWithoutInput.getRpcService());
        assertFalse(crateWithoutInput.getDataObject().isPresent());

        final RpcServiceInvoker testRpcInvokerWithInput =
                new TestRpcInvokerImpl(ImmutableMap.of("tstWithInput", methodWithInput));
        final DataObject dataObject = mock(DataObject.class);
        final Crate crateWithInput =
                (Crate) testRpcInvokerWithInput.invokeRpc(this, QName.create("tstWithInput"), dataObject).get();
        assertEquals(methodWithInput(this, dataObject).get().getRpcService(), crateWithInput.getRpcService());
        assertTrue(crateWithInput.getDataObject().isPresent());
        assertEquals(dataObject, crateWithInput.getDataObject().get());
    }

    public static Future<Crate> methodWithoutInput(RpcService testArgument) {
        return Futures.immediateFuture(new Crate(testArgument, null));
    }

    public static Future<Crate> methodWithInput(RpcService testArgument, DataObject testArgument2) {
        return Futures.immediateFuture(new Crate(testArgument, testArgument2));
    }

    private class TestRpcInvokerImpl extends AbstractMappedRpcInvoker<String> {

        TestRpcInvokerImpl(Map<String, Method> map) {
            super(map);
        }

        @Override
        protected String qnameToKey(QName qname) {
            return qname.toString();
        }
    }

    private static class Crate {
        private final RpcService rpcService;
        private final Optional<DataObject> dataObject;

        Crate(@NotNull RpcService rpcService, @Nullable DataObject dataObject) {
            this.rpcService = rpcService;
            this.dataObject = dataObject == null ? Optional.empty() : Optional.of(dataObject);
        }

        RpcService getRpcService() {
            return this.rpcService;
        }

        Optional<DataObject> getDataObject() {
            return this.dataObject;
        }
    }
}