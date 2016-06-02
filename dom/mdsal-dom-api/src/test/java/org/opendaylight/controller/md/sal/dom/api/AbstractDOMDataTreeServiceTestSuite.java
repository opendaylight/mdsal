/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.md.sal.dom.api;

import static org.junit.Assert.assertNotNull;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

import org.opendaylight.mdsal.dom.api.DOMDataTreeCursor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import com.google.common.util.concurrent.CheckedFuture;
import java.net.URI;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

/**
 * Abstract test suite demonstrating various access patterns on how a {@link DOMDataTreeService}
 * can be used.
 */
public abstract class AbstractDOMDataTreeServiceTestSuite {
    protected static final QNameModule TEST_MODULE = QNameModule.create(URI.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store"), null);

    protected static final YangInstanceIdentifier UNORDERED_CONTAINER_IID = YangInstanceIdentifier.create(
        new NodeIdentifier(QName.create(TEST_MODULE, "lists")),
        new NodeIdentifier(QName.create(TEST_MODULE, "unordered-container")));
    protected static final DOMDataTreeIdentifier UNORDERED_CONTAINER_TREE = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, UNORDERED_CONTAINER_IID);

    /**
     * Return a reference to the service used in this test. The instance
     * needs to be reused within the same test and must be isolated between
     * tests.
     *
     * @return {@link DOMDataTreeService} instance.
     */
    protected abstract @Nonnull DOMDataTreeService service();

    /**
     * A simple unbound producer. It write some basic things into the data store based on the
     * test model.
     * @throws DOMDataTreeProducerException
     * @throws TransactionCommitFailedException
     */

    @Test
    public final void testBasicProducer() throws DOMDataTreeProducerException, TransactionCommitFailedException {
        // Create a producer. It is an AutoCloseable resource, hence the try-with pattern
        try (final DOMDataTreeProducer prod = service().createProducer(Collections.singleton(UNORDERED_CONTAINER_TREE))) {
            assertNotNull(prod);

            final DOMDataTreeCursorAwareTransaction tx = prod.createTransaction(true);
            assertNotNull(tx);

            final DOMDataTreeWriteCursor cursor = tx.createCursor(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, UNORDERED_CONTAINER_IID));
            assertNotNull(cursor);
            cursor.write(UNORDERED_CONTAINER_IID.getLastPathArgument(), ImmutableContainerNodeBuilder.create().build());
            cursor.close();

            final CheckedFuture<Void, TransactionCommitFailedException> f = tx.submit();
            assertNotNull(f);

            f.checkedGet();
        }
    }

    // TODO: simple listener
}
