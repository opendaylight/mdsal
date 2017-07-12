/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;

public class ShardedDOMReadTransactionAdapterTest {

    private ShardedDOMReadTransactionAdapter readTx;

    @Before
    public void setUp() {
        readTx = new ShardedDOMReadTransactionAdapter("TEST-TX", new TestTreeService());
    }

    @Test
    public void testGetIdentifier() {
        assertEquals("TEST-TX", readTx.getIdentifier());
    }

    @Test
    public void testRead() throws Exception {
        final ListenableFuture<Optional<NormalizedNode<?, ?>>> readResult =
                readTx.read(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH);
        assertTrue(readTx.exists(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH).get());
        assertEquals(readResult.get().get(), TestUtils.TEST_CONTAINER);
    }

    @After
    public void close() throws Exception {
        readTx.close();
    }

    private static class TestTreeService implements DOMDataTreeService {

        @Nonnull
        @Override
        public <T extends DOMDataTreeListener> ListenerRegistration<T>
            registerListener(@Nonnull final T listener, @Nonnull final Collection<DOMDataTreeIdentifier> subtrees,
                         final boolean allowRxMerges,
                         @Nonnull final Collection<DOMDataTreeProducer> producers) throws DOMDataTreeLoopException {
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtree = Maps.newHashMap();
            subtree.put(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH),
                    TestUtils.TEST_CONTAINER);

            listener.onDataTreeChanged(Collections.singleton(
                    DataTreeCandidates.fromNormalizedNode(TestModel.TEST_PATH, TestUtils.TEST_CONTAINER)), subtree);

            return new ListenerRegistration<T>() {
                @Override
                public void close() {
                    // NOOP
                }

                @Override
                public T getInstance() {
                    return listener;
                }
            };
        }

        @Nonnull
        @Override
        public DOMDataTreeProducer createProducer(@Nonnull final Collection<DOMDataTreeIdentifier> subtrees) {
            return null;
        }
    }
}