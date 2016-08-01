/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class ShardedDOMDataWriteTransactionTest {

    @Mock
    private DOMDataTreeService dataTreeService;

    @Mock
    private DOMDataTreeProducer dataProducer;

    @Mock
    private DOMDataTreeCursorAwareTransaction cursorWriteTx;

    @Mock
    private DOMDataTreeWriteCursor writeCursor;

    @Mock
    private NormalizedNode<?, ?> data;

    private ShardedDOMWriteTransactionAdapter writeTx;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(dataProducer).when(dataTreeService).createProducer(any());
        doReturn(cursorWriteTx).when(dataProducer).createTransaction(true);
        doReturn(writeCursor).when(cursorWriteTx).createCursor(any());

        writeTx = new ShardedDOMWriteTransactionAdapter("TEST-TX", dataTreeService);
    }

    @Test
    public void testGetIdentifier() {
        assertEquals(writeTx.getIdentifier(), "TEST-TX");
    }

    @Test
    public void testOperations() throws Exception {
        doNothing().when(writeCursor).write(any(), any());
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TestModel.TEST_PATH, data);

        verify(writeCursor).write(eq(TestModel.TEST_PATH.getLastPathArgument()), eq(data));

        doNothing().when(writeCursor).delete(any());
        writeTx.delete(LogicalDatastoreType.OPERATIONAL, TestModel.TEST_PATH);
        verify(writeCursor).delete(eq(TestModel.TEST_PATH.getLastPathArgument()));
        // verify no new producers have been opened
        verify(dataProducer, times(2)).createTransaction(true);
        verify(cursorWriteTx, times(2)).createCursor(any());

        doNothing().when(writeCursor).merge(any(), any());
        writeTx.merge(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, data);
        verify(writeCursor).merge(eq(TestModel.TEST_PATH.getLastPathArgument()), eq(data));

        doNothing().when(writeCursor).close();
        doNothing().when(dataProducer).close();
        doReturn(Futures.immediateCheckedFuture(null)).when(cursorWriteTx).submit();
        writeTx.submit();
        verify(cursorWriteTx, times(2)).submit();
        verify(writeCursor, times(2)).close();
        verify(dataProducer, times(2)).close();
    }
}