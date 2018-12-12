/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

public class DOMDataTreeChangeListenerTest {

    private InMemoryDOMDataStore domStore;

    @Before
    public void setUp() {
        domStore = new InMemoryDOMDataStore("Mdsal217", MoreExecutors.newDirectExecutorService());
        domStore.onGlobalContextUpdated(TestModel.createTestContext());
    }

    @Test
    public void receiveOnDataInitialEventForEmptyRoot() throws InterruptedException, ExecutionException {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        doNothing().when(listener).onEmptyInitialData();

        domStore.registerTreeChangeListener(YangInstanceIdentifier.EMPTY, listener);
        verify(listener, timeout(1000)).onEmptyInitialData();
    }

    @Test
    public void receiveOnDataInitialEventForNonExistingData() throws InterruptedException, ExecutionException {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        final ArgumentCaptor<Collection> candidateCapture = ArgumentCaptor.forClass(Collection.class);
        doNothing().when(listener).onEmptyInitialData();
        doNothing().when(listener).onDataTreeChanged(any());

        domStore.registerTreeChangeListener(TestModel.TEST_PATH, listener);
        verify(listener, times(1)).onEmptyInitialData();

        final NormalizedNode<?, ?> testNode = ImmutableNodes.containerNode(TestModel.TEST_QNAME);
        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);
        writeTx.write(TestModel.TEST_PATH, testNode);
        DOMStoreThreePhaseCommitCohort cohort = writeTx.ready();
        cohort.preCommit().get();
        cohort.commit().get();

        verify(listener, timeout(1000)).onDataTreeChanged(candidateCapture.capture());
        final DataTreeCandidate candidate = (DataTreeCandidate) Iterables.getOnlyElement(candidateCapture.getValue());
        assertEquals(TestModel.TEST_PATH, candidate.getRootPath());
    }

    @Test
    public void receiveOnDataTreeChangedEventForPreExistingEmptyData() throws InterruptedException, ExecutionException {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        final ArgumentCaptor<Collection> candidateCapture = ArgumentCaptor.forClass(Collection.class);
        doNothing().when(listener).onDataTreeChanged(any());

        final NormalizedNode<?, ?> testNode = ImmutableNodes.containerNode(TestModel.TEST_QNAME);

        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);
        writeTx.write(TestModel.TEST_PATH, testNode);
        DOMStoreThreePhaseCommitCohort cohort = writeTx.ready();
        cohort.preCommit().get();
        cohort.commit().get();

        domStore.registerTreeChangeListener(TestModel.TEST_PATH, listener);

        verify(listener, timeout(1000)).onDataTreeChanged(candidateCapture.capture());
        final DataTreeCandidate candidate = (DataTreeCandidate) Iterables.getOnlyElement(candidateCapture.getValue());
        assertEquals(TestModel.TEST_PATH, candidate.getRootPath());
    }

    @Test
    public void receiveOnDataTreeChangeEventForPreExistingData() throws InterruptedException, ExecutionException {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        final ArgumentCaptor<Collection> candidateCapture = ArgumentCaptor.forClass(Collection.class);
        doNothing().when(listener).onDataTreeChanged(any());

        final ContainerNode testNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .addChild(ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                        .addChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME,
                                TestModel.ID_QNAME, 1)).build()).build();
        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);
        writeTx.write(TestModel.TEST_PATH, testNode);
        DOMStoreThreePhaseCommitCohort cohort = writeTx.ready();
        cohort.preCommit().get();
        cohort.commit().get();

        domStore.registerTreeChangeListener(TestModel.TEST_PATH, listener);

        verify(listener, timeout(1000)).onDataTreeChanged(candidateCapture.capture());
        final DataTreeCandidate firstItem = (DataTreeCandidate) candidateCapture.getValue().iterator().next();
        assertEquals(TestModel.TEST_PATH, firstItem.getRootPath());
    }
}
