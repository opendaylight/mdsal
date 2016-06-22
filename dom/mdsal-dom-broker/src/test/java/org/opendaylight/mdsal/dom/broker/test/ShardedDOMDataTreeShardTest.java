/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardingConflictException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardingService;
import org.opendaylight.mdsal.dom.broker.ShardedDOMDataTree;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class ShardedDOMDataTreeShardTest {


    private static final DOMDataTreeIdentifier ROOT_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST_PATH);

    @Mock(name = "rootShard")
    private DOMDataTreeShard rootShard;

    @Mock(name = "rootShard")
    private DOMDataTreeShard childShard;

    @Mock
    private DOMDataTreeProducer rootProducer;

    @Mock
    private DOMDataTreeProducer testProducer;

    private DOMDataTreeShardingService shardingService;
    private ListenerRegistration<DOMDataTreeShard> shardReg;

    @Before
    public void setUp() throws DOMDataTreeShardingConflictException {
        MockitoAnnotations.initMocks(this);
        doReturn(Collections.singleton(ROOT_ID)).when(rootProducer).getSubtrees();
        doReturn(Collections.singleton(TEST_ID)).when(testProducer).getSubtrees();
        final ShardedDOMDataTree impl = new ShardedDOMDataTree();
        shardingService = impl;
        shardReg = impl.registerDataTreeShard(ROOT_ID, rootShard, rootProducer);
        doReturn("rootShard").when(rootShard).toString();
        doReturn("childShard").when(childShard).toString();
    }

    @Test
    public void attachChildShard() throws DOMDataTreeShardingConflictException {
        doNothing().when(rootShard).onChildAttached(TEST_ID, childShard);
        shardingService.registerDataTreeShard(TEST_ID, childShard, testProducer);
        verify(rootShard, times(1)).onChildAttached(TEST_ID, childShard);
    }

    @Test
    public void attachAndRemoveShard() throws DOMDataTreeShardingConflictException {
        doNothing().when(rootShard).onChildAttached(TEST_ID, childShard);
        ListenerRegistration<DOMDataTreeShard> reg = shardingService.registerDataTreeShard(TEST_ID, childShard, testProducer);
        verify(rootShard, times(1)).onChildAttached(TEST_ID, childShard);

        doNothing().when(rootShard).onChildDetached(TEST_ID, childShard);
        doNothing().when(childShard).onChildDetached(TEST_ID, childShard);

        reg.close();
        verify(rootShard, times(1)).onChildDetached(TEST_ID, childShard);
    }

    @Test
    public void removeShard() {
        doNothing().when(rootShard).onChildDetached(ROOT_ID, rootShard);
        shardReg.close();
    }

}
