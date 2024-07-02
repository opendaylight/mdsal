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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

public class DOMDataTreeChangeListenerTest extends AbstractDatastoreTest {

    private InMemoryDOMDataStore domStore;

    @Before
    public void setUp() {
        domStore = new InMemoryDOMDataStore("Mdsal217", MoreExecutors.newDirectExecutorService());
        domStore.onModelContextUpdated(SCHEMA_CONTEXT);
    }

    @Test
    public void receiveOnDataInitialEventForEmptyRoot() {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        doNothing().when(listener).onInitialData();

        domStore.registerTreeChangeListener(YangInstanceIdentifier.of(), listener);
        verify(listener, timeout(1000)).onInitialData();
    }

    @Test
    public void receiveOnDataInitialEventForNonExistingData() throws Exception {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        final var candidateCapture = ArgumentCaptor.forClass(List.class);
        doNothing().when(listener).onInitialData();
        doNothing().when(listener).onDataTreeChanged(any());

        domStore.registerTreeChangeListener(TestModel.TEST_PATH, listener);
        verify(listener, times(1)).onInitialData();

        final var testNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();
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
    public void receiveOnDataTreeChangedEventForPreExistingEmptyData() throws Exception {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        final ArgumentCaptor<List> candidateCapture = ArgumentCaptor.forClass(List.class);
        doNothing().when(listener).onDataTreeChanged(any());

        final var testNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();

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
    public void receiveOnDataTreeChangeEventForPreExistingData() throws Exception {
        final DOMDataTreeChangeListener listener = mock(DOMDataTreeChangeListener.class);
        final ArgumentCaptor<List> candidateCapture = ArgumentCaptor.forClass(List.class);
        doNothing().when(listener).onDataTreeChanged(any());

        final ContainerNode testNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .addChild(TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
                .build())
            .build();
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
