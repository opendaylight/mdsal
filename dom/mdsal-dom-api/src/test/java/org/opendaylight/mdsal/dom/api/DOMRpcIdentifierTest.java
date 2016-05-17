/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DOMRpcIdentifierTest {
    private static final QNameModule TEST_MODULE =
            QNameModule.create(URI.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store"), null);
    private static final String LOCAL_IDENT = "local";

    private static final DOMRpcIdentifier GLOBAL =  DOMRpcIdentifier.create(SchemaPath.SAME, null);
    private static final DOMRpcIdentifier LOCAL =   DOMRpcIdentifier.create(SchemaPath.ROOT,
                            YangInstanceIdentifier.create(new NodeIdentifier(QName.create(TEST_MODULE, LOCAL_IDENT))));

    @Test
    public void createTest() throws Exception {
        assertTrue("Global instance", GLOBAL.getClass().getCanonicalName().contains("Global"));
        assertTrue("Local instance", LOCAL.getClass().getCanonicalName().contains("Local"));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertEquals("hashCode", GLOBAL.hashCode(), DOMRpcIdentifier.create(SchemaPath.ROOT).hashCode());

        assertNotEquals("hashCode", GLOBAL.hashCode(), LOCAL.hashCode());
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue("Equals same",
                GLOBAL.equals(DOMRpcIdentifier.create(SchemaPath.SAME)));

        assertTrue("Equals same instance", GLOBAL.equals(GLOBAL));

        assertFalse("Different object", GLOBAL.equals(new Object()));

        assertFalse("Different instance", GLOBAL.equals(LOCAL));
    }

    @Test
    public void toStringTest() throws Exception {
        assertTrue("ToString",  GLOBAL.toString().contains(GLOBAL.getContextReference().toString())
                            &&  GLOBAL.toString().contains(GLOBAL.getType().toString()));
        assertTrue("ToString",  LOCAL.toString().contains(LOCAL.getContextReference().toString())
                            &&  LOCAL.toString().contains(LOCAL.getType().toString()));
    }

}