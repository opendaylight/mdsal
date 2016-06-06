/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;

public class DelegatingReadableCursorOperationTest extends DelegatingReadableCursorOperation implements TestUtils {

    private static final DataTreeSnapshotCursor MOCK_CURSOR = mock(DataTreeSnapshotCursor.class);

    @Test
    public void basicText() throws Exception {
        final Optional<NormalizedNode<?, ?>> nodeOptional = Optional.absent();
        doReturn(nodeOptional).when(MOCK_CURSOR).readNode(PATH_ARGUMENT);
        doNothing().when(MOCK_CURSOR).exit();
        doNothing().when(MOCK_CURSOR).enter(PATH_ARGUMENT);
        doReturn("test").when(PATH_ARGUMENT).toString();

        assertFalse(readNode(PATH_ARGUMENT).isPresent());
        verify(MOCK_CURSOR).readNode(PATH_ARGUMENT);

        exit();
        verify(MOCK_CURSOR).exit();

        assertEquals(this, enter(PATH_ARGUMENT));
        verify(MOCK_CURSOR).enter(PATH_ARGUMENT);
    }

    @Override
    protected DataTreeSnapshotCursor delegate() {
        return MOCK_CURSOR;
    }
}