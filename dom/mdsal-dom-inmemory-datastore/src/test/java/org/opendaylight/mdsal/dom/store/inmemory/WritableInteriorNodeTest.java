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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class WritableInteriorNodeTest {

    @Test
    public void basicTest() throws Exception {
        final NodeIdentifier nodeIdentifier = NodeIdentifier.create(QName.create("test"));
        final WriteableModificationNode writeableModificationNode = mock(WriteableModificationNode.class);
        doNothing().when(writeableModificationNode).markDeleted();

        final DOMDataTreeWriteCursor domDataTreeWriteCursor = mock(DOMDataTreeWriteCursor.class);
        doNothing().when(domDataTreeWriteCursor).exit();

        final Map<PathArgument, WriteableModificationNode> children = new HashMap<>();
        children.put(nodeIdentifier,writeableModificationNode);

        WritableInteriorNode writableInteriorNode = new WritableInteriorNode(nodeIdentifier, children);
        assertEquals(writableInteriorNode.getIdentifier(), nodeIdentifier);

        writableInteriorNode.createOperation(domDataTreeWriteCursor).exit();
        verify(domDataTreeWriteCursor).exit();
    }
}