/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class DOMRpcIdentifierTest {
    private static final QNameModule TEST_MODULE =
        QNameModule.of("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store");
    private static final QName LOCAL_QNAME = QName.create(TEST_MODULE, "local");
    private static final DOMRpcIdentifier GLOBAL = DOMRpcIdentifier.create(LOCAL_QNAME, null);
    private static final DOMRpcIdentifier LOCAL = DOMRpcIdentifier.create(LOCAL_QNAME,
        YangInstanceIdentifier.of(LOCAL_QNAME));

    @Test
    void createTest() {
        assertInstanceOf(DOMRpcIdentifier.Global.class, GLOBAL);
        assertInstanceOf(DOMRpcIdentifier.Local.class, LOCAL);
    }

    @Test
    void hashCodeTest() {
        assertEquals(GLOBAL.hashCode(), DOMRpcIdentifier.create(LOCAL_QNAME).hashCode());
        assertNotEquals(GLOBAL.hashCode(), LOCAL.hashCode());
    }

    @Test
    void equalsTest() {
        assertEquals(GLOBAL, DOMRpcIdentifier.create(LOCAL_QNAME));
        assertEquals(GLOBAL, GLOBAL);
        assertNotEquals(GLOBAL, new Object());
        assertNotEquals(GLOBAL, LOCAL);
    }

    @Test
    void toStringTest() {
        assertEquals("Global{type=(urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store)local, "
            + "contextReference=/}", GLOBAL.toString());
        assertEquals("Local{type=(urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store)local, "
            + "contextReference=/(urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store)local}",
            LOCAL.toString());
    }
}