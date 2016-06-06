/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class WritableInteriorNodeTest implements TestUtils {

    @Test
    public void basicTest() throws Exception {
        doNothing().when(WRITEABLE_MODIFICATION_NODE).markDeleted();
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).exit();

        final Map<PathArgument, WriteableModificationNode> children = new HashMap<>();
        children.put(NODE_IDENTIFIER, WRITEABLE_MODIFICATION_NODE);

        final WritableInteriorNode writableInteriorNode = new WritableInteriorNode(NODE_IDENTIFIER, children);
        assertEquals(writableInteriorNode.getIdentifier(), NODE_IDENTIFIER);

        writableInteriorNode.createOperation(DOM_DATA_TREE_WRITE_CURSOR).exit();
        verify(DOM_DATA_TREE_WRITE_CURSOR).exit();
    }

    @After
    public void resetMocks() {
        TestUtils.resetMocks();
    }
}