/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.HashSet;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;

final class TestUtils {

    static final DOMDataTreeIdentifier DOM_DATA_TREE_IDENTIFIER =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);

    static final PathArgument PATH_ARGUMENT = mock(PathArgument.class);

    static final NodeIdentifier NODE_IDENTIFIER = NodeIdentifier.create(QName.create("test"));

    static final NormalizedNode NORMALIZED_NODE = mock(NormalizedNode.class);

    static final NormalizedNodeContainer NORMALIZED_NODE_CONTAINER = mock(NormalizedNodeContainer.class);

    static final DOMStoreThreePhaseCommitCohort DOM_STORE_THREE_PHASE_COMMIT_COHORT =
            mock(DOMStoreThreePhaseCommitCohort.class);

    static final Collection<DOMStoreThreePhaseCommitCohort> COHORTS = new HashSet<>();

    static final ListenableFuture LISTENABLE_FUTURE = mock(ListenableFuture.class);

    static final WriteableModificationNode WRITEABLE_MODIFICATION_NODE = mock(WriteableModificationNode.class);

    static final DOMDataTreeWriteCursor DOM_DATA_TREE_WRITE_CURSOR = mock(DOMDataTreeWriteCursor.class);

    static final WriteCursorStrategy WRITE_CURSOR_STRATEGY = mock(WriteCursorStrategy.class);

    static final DOMDataTreeShardProducer DOM_DATA_TREE_SHARD_PRODUCER = mock(DOMDataTreeShardProducer.class);

    static final DOMDataTreeShardWriteTransaction DOM_DATA_TREE_SHARD_WRITE_TRANSACTION =
            mock(DOMDataTreeShardWriteTransaction.class);

    static final DataTree DATA_TREE = mock(DataTree.class);

    static void resetMocks() {
        reset(WRITE_CURSOR_STRATEGY, DOM_DATA_TREE_WRITE_CURSOR, WRITEABLE_MODIFICATION_NODE, LISTENABLE_FUTURE,
                DOM_STORE_THREE_PHASE_COMMIT_COHORT, NORMALIZED_NODE_CONTAINER, NORMALIZED_NODE, PATH_ARGUMENT,
                DOM_DATA_TREE_SHARD_PRODUCER, DOM_DATA_TREE_SHARD_WRITE_TRANSACTION, DATA_TREE);
    }

    private TestUtils() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}