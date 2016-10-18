/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class AbstractMappedRpcInvokerTest implements RpcService {

    @Test
    public void invokeRpcTest() throws Exception {
        final Method method = this.getClass().getDeclaredMethod("testMethod", RpcService.class);
        final Method method2 = this.getClass().getDeclaredMethod("testMethod2", RpcService.class, DataObject.class);
        method.setAccessible(true);
        final TestRpcInvokerImpl testRpcInvoker = new TestRpcInvokerImpl(ImmutableMap.of("tst", method));
        assertEquals("test", testRpcInvoker.invokeRpc(this, QName.create("tst"), null).get());
        final TestRpcInvokerImpl testRpcInvoker2 = new TestRpcInvokerImpl(ImmutableMap.of("tst2", method2));
        assertEquals("test2", testRpcInvoker2.invokeRpc(this, QName.create("tst2"), null).get());
    }

    public static Future testMethod(RpcService testArgument) {
        return Futures.immediateFuture("test");
    }

    public static Future testMethod2(RpcService testArgument, DataObject testArgument2) {
        return Futures.immediateFuture("test2");
    }

    private class TestRpcInvokerImpl extends AbstractMappedRpcInvoker {

        TestRpcInvokerImpl(Map map) {
            super(map);
        }

        @Override
        protected Object qnameToKey(QName qname) {
            return qname.toString();
        }
    }
}