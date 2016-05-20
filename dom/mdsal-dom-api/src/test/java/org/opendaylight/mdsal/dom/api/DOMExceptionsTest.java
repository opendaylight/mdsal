/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class DOMExceptionsTest {
    private static final String TEST_MESSAGE = "TestMessage";
    private static final String TEST_LISTS = "test-lists";
    private static final QNameModule TEST_MODULE = QNameModule.create(URI.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store"), null);
    private static final YangInstanceIdentifier TEST_YI_ID = YangInstanceIdentifier.create(
            new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, TEST_LISTS)));
    private static final DOMDataTreeIdentifier TEST_TREE = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TEST_YI_ID);

    @Test(expected = DOMDataTreeInaccessibleException.class)
    public void DOMDataTreeInaccessibleExceptionTest() throws Exception {
        final DOMDataTreeInaccessibleException testExc = new DOMDataTreeInaccessibleException(TEST_TREE, TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));
        assertNotNull(testExc.getTreeIdentifier());
        assertEquals(TEST_TREE, testExc.getTreeIdentifier());

        throw new DOMDataTreeInaccessibleException(TEST_TREE, TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMDataTreeListeningException.class)
    public void DOMDataTreeListeningExceptionTest() throws Exception {
        final DOMDataTreeListeningException testExc = new DOMDataTreeListeningException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMDataTreeListeningException(TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMDataTreeLoopException.class)
    public void DOMDataTreeLoopExceptionTest() throws Exception {
        final DOMDataTreeLoopException testExc = new DOMDataTreeLoopException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMDataTreeLoopException(TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMDataTreeProducerBusyException.class)
    public void DOMDataTreeProducerBusyExceptionTest() throws Exception {
        final DOMDataTreeProducerBusyException testExc = new DOMDataTreeProducerBusyException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMDataTreeProducerBusyException(TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMDataTreeProducerException.class)
    public void DOMDataTreeProducerExceptionTest() throws Exception {
        final DOMDataTreeProducerException testExc = new DOMDataTreeProducerException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMDataTreeProducerException(TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMDataTreeShardingConflictException.class)
    public void DOMDataTreeShardingConflictExceptionTest() throws Exception {
        final DOMDataTreeShardingConflictException testExc = new DOMDataTreeShardingConflictException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMDataTreeShardingConflictException(TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMNotificationRejectedException.class)
    public void DOMNotificationRejectedExceptionTest() throws Exception {
        final DOMNotificationRejectedException testExc = new DOMNotificationRejectedException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMNotificationRejectedException(TEST_MESSAGE, new Throwable());
    }

    @Test(expected = DOMRpcImplementationNotAvailableException.class)
    public void DOMRpcImplementationNotAvailableExceptionTest() throws Exception {
        final DOMRpcImplementationNotAvailableException testExc = new DOMRpcImplementationNotAvailableException(TEST_MESSAGE);
        assertTrue(testExc.getMessage().contains(TEST_MESSAGE));

        throw new DOMRpcImplementationNotAvailableException(new Throwable(), TEST_MESSAGE, new Object());
    }
}