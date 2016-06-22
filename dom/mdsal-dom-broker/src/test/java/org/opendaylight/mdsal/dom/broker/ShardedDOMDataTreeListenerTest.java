/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.broker.MockingUtilities.captorFor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardingConflictException;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class ShardedDOMDataTreeListenerTest {


    private static final DOMDataTreeIdentifier ROOT_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST_PATH);

    private static final Collection<DOMDataTreeIdentifier> SUBTREES_ROOT = Collections.singleton(ROOT_ID);
    private static final Collection<DOMDataTreeIdentifier> SUBTREES_TEST = Collections.singleton(TEST_ID);
    private static final ContainerNode TEST_CONTAINER = ImmutableNodes.containerNode(TestModel.TEST_QNAME);

    private interface ListenableShard extends DOMDataTreeShard, DOMStoreTreeChangePublisher {


    }

    @Mock(name = "rootShard")
    private ListenableShard rootShard;

    @Mock(name = "childShard")
    private ListenableShard childShard;

    @Mock(name = "listener")
    private DOMDataTreeListener listener;

    @Mock
    private ShardedDOMDataTreeProducer producer;

    @Mock
    private ListenerRegistration<?> storeListenerReg;

    private DOMDataTreeService treeService;
    private ListenerRegistration<ListenableShard> shardReg;

    @Before
    public void setUp() throws DOMDataTreeShardingConflictException {
        MockitoAnnotations.initMocks(this);
        final ShardedDOMDataTree impl = new ShardedDOMDataTree();
        doReturn(Collections.singleton(ROOT_ID)).when(producer).getSubtrees();
        doNothing().when(producer).subshardAdded(anyMap());
        treeService = impl;
        shardReg = impl.registerDataTreeShard(ROOT_ID, rootShard, producer);
        doReturn("rootShard").when(rootShard).toString();
        doReturn("childShard").when(childShard).toString();

        doReturn(storeListenerReg).when(rootShard).registerTreeChangeListener(any(YangInstanceIdentifier.class),
                any(DOMDataTreeChangeListener.class));
        doNothing().when(storeListenerReg).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerListenerWithEmptySubtrees() throws DOMDataTreeLoopException {
        treeService.registerListener(listener, Collections.<DOMDataTreeIdentifier>emptyList(), true,
                Collections.<DOMDataTreeProducer>emptyList());
    }

    @Test
    public void registerRootListener() throws DOMDataTreeLoopException {
        treeService.registerListener(listener, SUBTREES_ROOT, true, Collections.<DOMDataTreeProducer>emptyList());
        verify(rootShard, times(1)).registerTreeChangeListener(eq(ROOT_ID.getRootIdentifier()),
                any(DOMDataTreeChangeListener.class));
    }

    @Test
    public void registerTreeListener() throws DOMDataTreeLoopException {
        treeService.registerListener(listener, SUBTREES_TEST, true, Collections.<DOMDataTreeProducer>emptyList());
        verify(rootShard, times(1)).registerTreeChangeListener(eq(TEST_ID.getRootIdentifier()),
                any(DOMDataTreeChangeListener.class));
    }

    @Test
    public void registerAndCloseListener() throws DOMDataTreeLoopException {
        final ListenerRegistration<DOMDataTreeListener> reg =
                treeService.registerListener(listener, SUBTREES_TEST, true,
                        Collections.<DOMDataTreeProducer>emptyList());
        reg.close();
        verify(storeListenerReg, times(1)).close();
    }

    @Test
    public void receiveChangeEvent() throws DOMDataTreeLoopException {
        final ArgumentCaptor<DOMDataTreeChangeListener> storeListener =
                ArgumentCaptor.forClass(DOMDataTreeChangeListener.class);
        treeService.registerListener(listener, SUBTREES_TEST, true, Collections.<DOMDataTreeProducer>emptyList());
        verify(rootShard, times(1)).registerTreeChangeListener(eq(TEST_ID.getRootIdentifier()),
                storeListener.capture());

        final DataTreeCandidate sentStoreCandidate =
                DataTreeCandidates.fromNormalizedNode(TEST_ID.getRootIdentifier(), TEST_CONTAINER);
        final Collection<DataTreeCandidate> changes = Collections.singleton(sentStoreCandidate);

        doNothing().when(listener).onDataTreeChanged(Mockito.<Collection<DataTreeCandidate>>any(), Mockito.anyMap());
        storeListener.getValue().onDataTreeChanged(changes);

        final ArgumentCaptor<Collection<DataTreeCandidate>> candidateCapture = captorFor(Collection.class);
        final ArgumentCaptor<Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>>> mapCapture = captorFor(Map.class);
        verify(listener, times(1)).onDataTreeChanged(candidateCapture.capture(), mapCapture.capture());

        final Collection<DataTreeCandidate> receivedCandidate = candidateCapture.getValue();
        final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> receivedMap = mapCapture.getValue();

        assertNotNull("receivedCandidate", receivedCandidate);
        assertNotNull("receivedMap", receivedMap);
        assertFalse("candidate collection must not be empty", receivedCandidate.isEmpty());
        assertEquals(1, receivedCandidate.size());
        final DataTreeCandidate firstItem = receivedCandidate.iterator().next();
        assertEquals(TEST_ID.getRootIdentifier(), firstItem.getRootPath());
        assertEquals(TEST_CONTAINER, receivedMap.get(TEST_ID));
    }

}
