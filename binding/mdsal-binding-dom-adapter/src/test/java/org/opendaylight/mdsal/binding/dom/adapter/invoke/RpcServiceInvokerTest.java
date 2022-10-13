/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

@Deprecated(since = "11.0.0", forRemoval = true)
public class RpcServiceInvokerTest {
    @Test
    public void fromTest() throws Exception {
        final Method method = this.getClass().getDeclaredMethod("testMethod");
        method.setAccessible(true);
        assertNotNull(RpcServiceInvoker.from(Map.of(
            QName.create(QNameModule.create(XMLNamespace.of("testURI"), Revision.of("2017-10-26")),"test"), method,
            QName.create(QNameModule.create(XMLNamespace.of("testURI2"), Revision.of("2017-10-26")),"test"), method)));
        assertNotNull(RpcServiceInvoker.from(Map.of(
            QName.create(QNameModule.create(XMLNamespace.of("testURI"), Revision.of("2017-10-26")), "test"), method)));
    }

    private void testMethod() {
        // NOOP
    }
}
