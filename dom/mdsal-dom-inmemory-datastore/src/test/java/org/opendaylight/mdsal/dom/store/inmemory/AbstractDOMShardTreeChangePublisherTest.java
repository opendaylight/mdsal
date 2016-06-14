/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DATA_TREE;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

public class AbstractDOMShardTreeChangePublisherTest extends AbstractDOMShardTreeChangePublisher {

    private static final YangInstanceIdentifier YANG_INSTANCE_IDENTIFIER =
            YangInstanceIdentifier.of(QName.create("test"));

    private static final DOMDataTreeIdentifier DOM_DATA_TREE_IDENTIFIER =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YANG_INSTANCE_IDENTIFIER);

    private static final ReadableWriteableDOMDataTreeShard READABLE_WRITEABLE_DOM_DATA_TREE_SHARD =
            mock(ReadableWriteableDOMDataTreeShard.class);

    private static final ChildShardContext CHILD_SHARD_CONTEXT =
            new ChildShardContext(DOM_DATA_TREE_IDENTIFIER, READABLE_WRITEABLE_DOM_DATA_TREE_SHARD);

    private static final Map<DOMDataTreeIdentifier, ChildShardContext> CHILD_SHARDS =
            ImmutableMap.of(DOM_DATA_TREE_IDENTIFIER, CHILD_SHARD_CONTEXT);

    @Test
    public void registerTreeChangeListenerTest() throws Exception {
        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);
        final ListenerRegistration listenerRegistration = mock(ListenerRegistration.class);
        doReturn(listenerRegistration)
                .when(READABLE_WRITEABLE_DOM_DATA_TREE_SHARD).registerTreeChangeListener(any(), any());

        assertNotNull(this.registerTreeChangeListener(YANG_INSTANCE_IDENTIFIER, domDataTreeChangeListener));
        verify(READABLE_WRITEABLE_DOM_DATA_TREE_SHARD).registerTreeChangeListener(any(), any());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registerTreeChangeListenerTestWithException() throws Exception {
        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);
        final ListenerRegistration listenerRegistration = mock(ListenerRegistration.class);
        doReturn(listenerRegistration)
                .when(READABLE_WRITEABLE_DOM_DATA_TREE_SHARD).registerTreeChangeListener(any(), any());
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);
        final ChildShardContext childShardContext =
                new ChildShardContext(domDataTreeIdentifier, READABLE_WRITEABLE_DOM_DATA_TREE_SHARD);
        final Map<DOMDataTreeIdentifier, ChildShardContext> childShardContextMap =
                ImmutableMap.of(domDataTreeIdentifier, childShardContext);

        AbstractDOMShardTreeChangePublisherTest abstractDOMShardTreeChangePublisherTest =
                new AbstractDOMShardTreeChangePublisherTest(childShardContextMap);
        abstractDOMShardTreeChangePublisherTest
                .registerTreeChangeListener(YANG_INSTANCE_IDENTIFIER, domDataTreeChangeListener);
    }

    public AbstractDOMShardTreeChangePublisherTest() {
        super(DATA_TREE, YANG_INSTANCE_IDENTIFIER, CHILD_SHARDS);
    }

    private AbstractDOMShardTreeChangePublisherTest(Map<DOMDataTreeIdentifier, ChildShardContext> childShardContextMap) {
        super(DATA_TREE, YANG_INSTANCE_IDENTIFIER, childShardContextMap);
    }

    @Override
    protected void notifyListeners(@Nonnull Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations,
                                   @Nonnull YangInstanceIdentifier path, @Nonnull DataTreeCandidateNode node) {
        // NOOP
    }

    @Override
    protected void registrationRemoved(@Nonnull AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        // NOOP
    }

    @After
    public void reset(){
        resetMocks();
    }
}