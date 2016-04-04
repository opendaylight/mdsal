/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardingConflictException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.broker.ShardedDOMDataTree;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class ShardedDOMDataTreeProducerSingleShardTest {


    private static final DOMDataTreeIdentifier ROOT_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST_PATH);

    private static final Collection<DOMDataTreeIdentifier> SUBTREES_ROOT = Collections.singleton(ROOT_ID);
    private static final Collection<DOMDataTreeIdentifier> SUBTREES_TEST = Collections.singleton(TEST_ID);

    interface MockTestShard extends DOMDataTreeShard, DOMStore {

    }

    @Mock(name = "rootShard")
    private MockTestShard rootShard;


    @Mock(name = "storeWriteTx")
    private DOMStoreWriteTransaction writeTxMock;

    @Mock(name = "storeTxChain")
    private DOMStoreTransactionChain txChainMock;

    @Mock
    private DOMDataTreeProducer rootProducer;

    private DOMDataTreeService treeService;
    private ListenerRegistration<MockTestShard> shardReg;
    private DOMDataTreeProducer producer;

    @Before
    public void setUp() throws DOMDataTreeShardingConflictException {
        MockitoAnnotations.initMocks(this);
        doReturn(Collections.singleton(ROOT_ID)).when(rootProducer).getSubtrees();
        final ShardedDOMDataTree impl = new ShardedDOMDataTree();
        treeService = impl;
        shardReg = impl.registerDataTreeShard(ROOT_ID, rootShard, rootProducer);

        doReturn("rootShard").when(rootShard).toString();
        doReturn(txChainMock).when(rootShard).createTransactionChain();
        doReturn(writeTxMock).when(txChainMock).newWriteOnlyTransaction();
        doReturn(TestCommitCohort.ALLWAYS_SUCCESS).when(writeTxMock).ready();

        producer = treeService.createProducer(SUBTREES_ROOT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createProducerWithEmptyList() {
        treeService.createProducer(Collections.<DOMDataTreeIdentifier>emptySet());
    }

    @Test(expected = DOMDataTreeProducerBusyException.class)
    public void closeWithTxOpened() throws DOMDataTreeProducerException {
        producer.createTransaction(false);
        producer.close();
    }

    @Test
    public void closeWithTxSubmitted() throws DOMDataTreeProducerException {
        DOMDataTreeWriteTransaction tx = producer.createTransaction(false);
        tx.submit();
        producer.close();
    }

    @Test(expected = IllegalStateException.class)
    public void allocateTxWithTxOpen() {
        producer.createTransaction(false);
        producer.createTransaction(false);
    }


    @Test(expected = IllegalStateException.class)
    public void allocateChildProducerWithTxOpen() {
        producer.createTransaction(false);
        producer.createProducer(SUBTREES_TEST);
    }

    @Test
    public void allocateChildProducerWithTxSubmmited() {
        producer.createTransaction(false).submit();
        DOMDataTreeProducer childProducer = producer.createProducer(SUBTREES_TEST);
        assertNotNull(childProducer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeChildProducerDataToParentTx() {
        DOMDataTreeProducer childProducer = producer.createProducer(SUBTREES_TEST);
        assertNotNull(childProducer);
        DOMDataTreeWriteTransaction parentTx = producer.createTransaction(true);
        parentTx.put(TEST_ID.getDatastoreType(), TEST_ID.getRootIdentifier(),
                ImmutableNodes.containerNode(TestModel.TEST_QNAME));
    }

    @Test
    public void allocateTxWithTxSubmitted() {
        producer.createTransaction(false).submit();
        producer.createTransaction(false);
    }

}
