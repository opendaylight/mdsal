/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangDataName;

public class NamingTest {

    @Test
    public void testGetClassName() {
        assertEquals("TestClass", Naming.getClassName("testClass"));
        assertEquals("", Naming.getClassName(""));
        assertEquals("SomeTestingClassName", Naming.getClassName("  some-testing_class name   "));
        assertEquals("_0SomeTestingClassName", Naming.getClassName("  0 some-testing_class name   "));
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("test", Naming.getPropertyName("Test"));
        assertEquals("test", Naming.getPropertyName("test"));
        assertEquals("xmlClass", Naming.getPropertyName("Class"));
        assertEquals("_5", Naming.getPropertyName("5"));
        assertEquals("", Naming.getPropertyName(""));
        assertEquals("someTestingParameterName", Naming.getPropertyName("  some-testing_parameter   name   "));
        assertEquals("_0someTestingParameterName", Naming.getPropertyName("  0some-testing_parameter   name   "));
    }

    @Test
    public void getRpcMethodName() {
        assertEquals("testRpcCommand", Naming.getRpcMethodName(QName.create("test", "test-rpc-command")));
        assertEquals("testRpcCommand", Naming.getRpcMethodName(QName.create("test", "test.rpc.command")));
        assertEquals("interface$", Naming.getRpcMethodName(QName.create("test", "interface")));
        assertEquals("someTestingMethodName",
                Naming.getRpcMethodName(QName.create("test", "some-testing_methodName")));
        assertEquals("_0SomeTestingMethodName",
                Naming.getRpcMethodName(QName.create("test", "_0-some-testing_methodName")));
        // unsafely setting QNames to test the handling of non-identifier-part characters
        final Map<String, String> expectToValMap = Map.of(
                "someTestingMethodName", "  some-testing_method   name   ",
                "_0someTestingMethodName", "  0some-testing_method   name   ",
                "payDollar$Rpc", "pay_dollar$_rpc",
                "$startDollarRpc", "$start dollar rpc");
        final QNameModule module = QNameModule.create(XMLNamespace.of("aaa"), Revision.of("2023-02-17"));
        expectToValMap.keySet()
                .forEach(k -> assertEquals(k, Naming.getRpcMethodName(QName.unsafeOf(module, expectToValMap.get(k)))));
    }

    @Test
    public void yangDataMapping() {
        final var ns = QNameModule.create(XMLNamespace.of("unused"));

        // single ascii compliant non-conflicting word - remain as is
        assertEquals("single", Naming.mapYangDataName(new YangDataName(ns, "single")));
        // ascii compliant - non-compliany chars only encoded
        assertEquals("$abc$20$cde", Naming.mapYangDataName(new YangDataName(ns, "abc cde")));
        // latin1 compliant -> latin chars normalized, non-compliant chars are encoded
        assertEquals("$ľaľaho$20$papľuhu", Naming.mapYangDataName(new YangDataName(ns, "ľaľaho papľuhu")));
        // latin1 non-compliant - all non-compliant characters encoded
        assertEquals("$привет$20$papľuhu", Naming.mapYangDataName(new YangDataName(ns, "привет papľuhu")));
    }
}