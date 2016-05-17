/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.broker.test.DOMDataTreeListenerTest;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class UnknownDOMRpcRoutingTableEntryTest {

    @Test
    public void basicTest() throws Exception {
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> emptyImpls = new HashMap<>();
        UnknownDOMRpcRoutingTableEntry test = new UnknownDOMRpcRoutingTableEntry(SchemaPath.ROOT, emptyImpls);
        assertNotNull(test);
        assertNotNull(test.newInstance(emptyImpls));
        assertNotNull(test.invokeRpc(DOMDataTreeListenerTest.TEST_CONTAINER));
    }

}