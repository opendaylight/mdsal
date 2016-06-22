/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.store.inmemory.WriteableDOMDataTreeShard;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class ShardedDOMDataWriteTransactionTest {
    private static final Map<YangInstanceIdentifier, List<NormalizedNode<?, ?>>> TEST_MAP = new HashMap<>();

    private static final DOMDataTreeIdentifier ROOT_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);

    @Mock
    private WriteableDOMDataTreeShard rootShard;

    @Mock
    private DOMDataTreeShardProducer mockedProducer;

    @Test
    public void basicTests() throws Exception {

        initMocks(this);

        doReturn(new TestDOMShardWriteTransaction()).when(mockedProducer).createTransaction();
        doReturn(mockedProducer).when(rootShard).createProducer(any(Collection.class));

        final ShardedDOMDataTree shardedDOMDataTree =
                new ShardedDOMDataTree();
        final DOMDataTreeProducer shardRegProducer = shardedDOMDataTree.createProducer(Collections.singletonList(ROOT_ID));
        shardedDOMDataTree.registerDataTreeShard(ROOT_ID, rootShard, shardRegProducer);
        shardRegProducer.close();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(QName.create("test"));

        final DOMDataTreeProducer producer = shardedDOMDataTree.createProducer(Collections.singletonList(ROOT_ID));
        final DOMDataTreeCursorAwareTransaction transaction = producer.createTransaction(false);
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

        assertNotNull(cursor);
        assertFalse(TEST_MAP.containsKey(yangInstanceIdentifier));
        cursor.write(yangInstanceIdentifier.getLastPathArgument(), TestUtils.TEST_CONTAINER);
        assertTrue(TEST_MAP.containsKey(yangInstanceIdentifier));

        cursor.delete(yangInstanceIdentifier.getLastPathArgument());
        assertFalse(TEST_MAP.containsKey(yangInstanceIdentifier));

        cursor.merge(yangInstanceIdentifier.getLastPathArgument(), TestUtils.TEST_CONTAINER);
        assertTrue(TEST_MAP.get(yangInstanceIdentifier).contains(TestUtils.TEST_CONTAINER));

        try {
            producer.createTransaction(false);
            fail("Should have failed, there's still a tx open");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("open"));
        }

        cursor.close();
        try {
            transaction.createCursor(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY));
            fail("Should have failed, config ds not available to this tx");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not accessible"));
        }

        assertTrue(transaction.cancel());
        assertFalse(transaction.cancel());

        final DOMDataTreeCursorAwareTransaction newTx = producer.createTransaction(false);
        assertNotNull(newTx);
    }

    private final class TestDOMShardWriteTransaction implements DOMDataTreeShardWriteTransaction {

        @Nonnull
        @Override
        public DOMDataTreeWriteCursor createCursor(@Nonnull final DOMDataTreeIdentifier prefix) {
            return new TestCursor();
        }

        @Override
        public void ready() {

        }

        @Override
        public void close() {

        }

        @Override
        public ListenableFuture<Void> submit() {
            return null;
        }

        @Override
        public ListenableFuture<Boolean> validate() {
            return null;
        }

        @Override
        public ListenableFuture<Void> prepare() {
            return null;
        }

        @Override
        public ListenableFuture<Void> commit() {
            return null;
        }
    }

    private final class TestCursor implements DOMDataTreeWriteCursor {

        private final Deque<PathArgument> stack = new ArrayDeque<>();

        @Override
        public void delete(final PathArgument child) {
            final ArrayDeque<PathArgument> newPath = new ArrayDeque<>(stack);
            newPath.push(child);
            TEST_MAP.remove(YangInstanceIdentifier.create(newPath));
        }

        @Override
        public void merge(final PathArgument child, final NormalizedNode<?, ?> data) {
            final ArrayDeque<PathArgument> newPath = new ArrayDeque<>(stack);
            newPath.push(child);
            final List<NormalizedNode<?, ?>> dataList = TEST_MAP.get(YangInstanceIdentifier.create(newPath));
            if (dataList != null) {
                dataList.add(data);
            } else {
                TEST_MAP.put(YangInstanceIdentifier.create(newPath), Lists.newArrayList(data));
            }
        }

        @Override
        public void write(final PathArgument child, final NormalizedNode<?, ?> data) {
            final ArrayDeque<PathArgument> newPath = new ArrayDeque<>(stack);
            newPath.push(child);
            TEST_MAP.put(YangInstanceIdentifier.create(newPath), Lists.newArrayList(data));
        }

        @Override
        public void enter(@Nonnull final PathArgument child) {
            stack.push(child);
        }

        @Override
        public void enter(@Nonnull final PathArgument... path) {
            for (final PathArgument pathArgument : path) {
                stack.push(pathArgument);
            }
        }

        @Override
        public void enter(@Nonnull final Iterable<PathArgument> path) {
            path.forEach(stack::push);
        }

        @Override
        public void exit() {
            stack.pop();
        }

        @Override
        public void exit(final int depth) {
            stack.pop();
        }

        @Override
        public void close() {

        }
    }
}