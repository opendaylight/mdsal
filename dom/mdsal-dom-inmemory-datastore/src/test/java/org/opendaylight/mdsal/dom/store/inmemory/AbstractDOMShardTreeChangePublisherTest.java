/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DATA_TREE;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.shard.ChildShardContext;
import org.opendaylight.mdsal.dom.spi.shard.ReadableWriteableDOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

public class AbstractDOMShardTreeChangePublisherTest extends AbstractDOMShardTreeChangePublisher {

    private static final YangInstanceIdentifier YANG_INSTANCE_IDENTIFIER =
            YangInstanceIdentifier.of(QName.create("", "test"));

    private static final DOMDataTreeIdentifier DOM_DATA_TREE_IDENTIFIER =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YANG_INSTANCE_IDENTIFIER);

    private static final ReadableWriteableDOMDataTreeShard READABLE_WRITEABLE_DOM_DATA_TREE_SHARD =
            mock(ReadableWriteableDOMDataTreeShard.class);

    private static final ChildShardContext CHILD_SHARD_CONTEXT =
            new ChildShardContext(DOM_DATA_TREE_IDENTIFIER, READABLE_WRITEABLE_DOM_DATA_TREE_SHARD);

    private static final Map<DOMDataTreeIdentifier, ChildShardContext> CHILD_SHARDS =
            ImmutableMap.of(DOM_DATA_TREE_IDENTIFIER, CHILD_SHARD_CONTEXT);

    @Captor
    private ArgumentCaptor<Collection<DataTreeCandidate>> captorForChanges;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void registerTreeChangeListenerTest() throws Exception {
        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);
        final ListenerRegistration<?> listenerRegistration = mock(ListenerRegistration.class);
        final DataTreeSnapshot initialSnapshot = mock(DataTreeSnapshot.class);
        final DataContainerNode<?> initialData =
                ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(QName.create("", "test")))
                        .build();
        doReturn(initialSnapshot).when(DATA_TREE).takeSnapshot();
        doReturn(Optional.of(initialData)).when(initialSnapshot).readNode(any());
        doNothing().when(domDataTreeChangeListener).onDataTreeChanged(any());

        doReturn(listenerRegistration)
                .when(READABLE_WRITEABLE_DOM_DATA_TREE_SHARD).registerTreeChangeListener(any(), any());

        assertNotNull(this.registerTreeChangeListener(YANG_INSTANCE_IDENTIFIER, domDataTreeChangeListener));
        verify(READABLE_WRITEABLE_DOM_DATA_TREE_SHARD).registerTreeChangeListener(any(), any());

        verify(domDataTreeChangeListener)
                .onDataTreeChanged(captorForChanges.capture());

        final Collection<DataTreeCandidate> initialChange = captorForChanges.getValue();

        assertTrue(initialChange.size() == 1);
        initialChange.forEach(dataTreeCandidate ->
                assertEquals(dataTreeCandidate.getRootPath(), YANG_INSTANCE_IDENTIFIER));
        initialChange.forEach(dataTreeCandidate ->
                assertEquals(dataTreeCandidate.getRootNode().getModificationType(), ModificationType.UNMODIFIED));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registerTreeChangeListenerTestWithException() throws Exception {
        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);
        final ListenerRegistration<?> listenerRegistration = mock(ListenerRegistration.class);
        doReturn(listenerRegistration)
                .when(READABLE_WRITEABLE_DOM_DATA_TREE_SHARD).registerTreeChangeListener(any(), any());
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.empty());
        final ChildShardContext childShardContext =
                new ChildShardContext(domDataTreeIdentifier, READABLE_WRITEABLE_DOM_DATA_TREE_SHARD);
        final Map<DOMDataTreeIdentifier, ChildShardContext> childShardContextMap =
                ImmutableMap.of(domDataTreeIdentifier, childShardContext);

        final AbstractDOMShardTreeChangePublisherTest abstractDOMShardTreeChangePublisherTest =
                new AbstractDOMShardTreeChangePublisherTest(childShardContextMap);
        abstractDOMShardTreeChangePublisherTest
                .registerTreeChangeListener(YANG_INSTANCE_IDENTIFIER, domDataTreeChangeListener);
    }

    public AbstractDOMShardTreeChangePublisherTest() {
        super(DATA_TREE, YANG_INSTANCE_IDENTIFIER, CHILD_SHARDS);
    }

    private AbstractDOMShardTreeChangePublisherTest(
            final Map<DOMDataTreeIdentifier, ChildShardContext> childShardContextMap) {
        super(DATA_TREE, YANG_INSTANCE_IDENTIFIER, childShardContextMap);
    }

    @Override
    protected void notifyListener(final AbstractDOMDataTreeChangeListenerRegistration<?> registration,
            final Collection<DataTreeCandidate> changes) {
        // NOOP
    }

    @Override
    protected void registrationRemoved(final AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        // NOOP
    }

    @After
    public void reset() {
        resetMocks();
    }
}
