/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class ShardedDOMDataWriteTransactionTest {
    private static final Map<YangInstanceIdentifier, List<NormalizedNode<?, ?>>> TEST_MAP = new HashMap<>();

    @Test
    public void basicTests() throws Exception {
        final ShardedDOMDataTree shardedDOMDataTree =
                new ShardedDOMDataTree();
        final ShardedDOMDataTreeProducer shardedDOMDataTreeProducer =
                new ShardedDOMDataTreeProducer(shardedDOMDataTree, new HashMap<>(), new HashSet<>());
        final TestDOMStoreWriteTransaction testDOMStoreWriteTransaction =
                new TestDOMStoreWriteTransaction();
        final Map<DOMDataTreeIdentifier, DOMStoreWriteTransaction> idToTransaction =
                new HashMap<>();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(QName.create("test"));
        idToTransaction.put(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier),
                            testDOMStoreWriteTransaction);

        final ShardedDOMDataWriteTransaction shardedDOMDataWriteTransaction =
                new ShardedDOMDataWriteTransaction(shardedDOMDataTreeProducer, idToTransaction);
        final ShardedDOMDataWriteTransaction otherShardedDOMDataWriteTransaction =
                new ShardedDOMDataWriteTransaction(shardedDOMDataTreeProducer, idToTransaction);

        assertFalse(TEST_MAP.containsKey(yangInstanceIdentifier));
        shardedDOMDataWriteTransaction.put(
                LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier, TestUtils.TEST_CONTAINER);
        assertTrue(TEST_MAP.containsKey(yangInstanceIdentifier));
        shardedDOMDataWriteTransaction.delete(
                LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier);
        assertFalse(TEST_MAP.containsKey(yangInstanceIdentifier));
        shardedDOMDataWriteTransaction.merge(
                LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier, TestUtils.TEST_CONTAINER);
        assertTrue(TEST_MAP.get(yangInstanceIdentifier).contains(TestUtils.TEST_CONTAINER));

        try {
            shardedDOMDataWriteTransaction.put(
                    LogicalDatastoreType.CONFIGURATION, yangInstanceIdentifier, TestUtils.TEST_CONTAINER);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(LogicalDatastoreType.CONFIGURATION.toString()));
        }

        shardedDOMDataTreeProducer.createTransaction(true);
        assertTrue(shardedDOMDataWriteTransaction.cancel());
        assertFalse(shardedDOMDataWriteTransaction.cancel());

        assertTrue(shardedDOMDataWriteTransaction.getIdentifier().contains("0"));
        assertTrue(otherShardedDOMDataWriteTransaction.getIdentifier().contains("1"));
    }

    private final class TestDOMStoreWriteTransaction implements DOMStoreWriteTransaction {

        @Override
        public void write(YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
            List<NormalizedNode<?, ?>> dataList = TEST_MAP.get(path);
            if(dataList == null) dataList = new ArrayList<>();
            dataList.add(data);
            TEST_MAP.put(path, dataList);
        }

        @Override
        public void merge(YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
            List<NormalizedNode<?, ?>> dataList = TEST_MAP.get(path);
            if(dataList == null) dataList = new ArrayList<>();
            dataList.add(data);
            TEST_MAP.put(path, dataList);
        }

        @Override
        public void delete(YangInstanceIdentifier path) {
            TEST_MAP.remove(path);
        }

        @Override
        public DOMStoreThreePhaseCommitCohort ready() {
            return null;
        }

        @Override
        public Object getIdentifier() {
            return null;
        }

        @Override
        public void close() {
            // NOOP
        }
    }
}