/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

@Deprecated(since = "11.0.0", forRemoval = true)
public class AbstractMappedRpcInvokerTest {
    @Test
    public void invokeRpcTest() throws Exception {
        final Method methodWithInput =
                TestRpcService.class.getDeclaredMethod("methodWithInput", RpcService.class, DataObject.class);

        methodWithInput.setAccessible(true);

        final RpcService rpcService = new TestRpcService();

        final TestRpcInvokerImpl testRpcInvoker =
                new TestRpcInvokerImpl(ImmutableMap.of(
                        "(test)tstWithInput", methodWithInput));

        assertTrue(testRpcInvoker.map.get("(test)tstWithInput") instanceof RpcMethodInvoker);

        final DataObject dataObject = mock(DataObject.class);
        final Crate crateWithInput =
                (Crate) testRpcInvoker.invokeRpc(rpcService, QName.create("test", "tstWithInput"), dataObject).get();
        assertEquals(TestRpcService.methodWithInput(rpcService, dataObject).get().getRpcService(),
                crateWithInput.getRpcService());
        assertTrue(crateWithInput.getDataObject().isPresent());
        assertEquals(dataObject, crateWithInput.getDataObject().get());
    }

    private static class TestRpcInvokerImpl extends AbstractMappedRpcInvoker<String> {
        TestRpcInvokerImpl(final Map<String, Method> map) {
            super(map);
        }

        @Override
        protected String qnameToKey(final QName qname) {
            return qname.toString();
        }
    }

    static class Crate {
        private final RpcService rpcService;
        private final ThreadLocal<Optional<DataObject>> dataObject;

        Crate(final @NonNull RpcService rpcService, final @Nullable DataObject dataObject) {
            this.rpcService = rpcService;
            this.dataObject =
                ThreadLocal.withInitial(() -> dataObject == null ? Optional.empty() : Optional.of(dataObject));
        }

        RpcService getRpcService() {
            return rpcService;
        }

        Optional<DataObject> getDataObject() {
            return dataObject.get();
        }
    }

    static class TestRpcService implements RpcService {
        static ListenableFuture<Crate> methodWithInput(final RpcService testArgument, final DataObject testArgument2) {
            return Futures.immediateFuture(new Crate(testArgument, testArgument2));
        }
    }
}

