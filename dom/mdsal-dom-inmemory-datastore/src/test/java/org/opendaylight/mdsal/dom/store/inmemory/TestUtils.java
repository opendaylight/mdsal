/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.mockito.Mockito.mock;

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

interface TestUtils {

    DOMDataTreeIdentifier DOM_DATA_TREE_IDENTIFIER =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);

    PathArgument PATH_ARGUMENT = mock(PathArgument.class);

    NodeIdentifier NODE_IDENTIFIER = NodeIdentifier.create(QName.create("test"));

    NormalizedNode NORMALIZED_NODE = mock(NormalizedNode.class);

    NormalizedNodeContainer NORMALIZED_NODE_CONTAINER = mock(NormalizedNodeContainer.class);

    DOMStoreThreePhaseCommitCohort DOM_STORE_THREE_PHASE_COMMIT_COHORT = mock(DOMStoreThreePhaseCommitCohort.class);

    Collection<DOMStoreThreePhaseCommitCohort> COHORTS = new HashSet<>();

    ListenableFuture LISTENABLE_FUTURE = mock(ListenableFuture.class);

    WriteableModificationNode WRITEABLE_MODIFICATION_NODE = mock(WriteableModificationNode.class);

    DOMDataTreeWriteCursor DOM_DATA_TREE_WRITE_CURSOR = mock(DOMDataTreeWriteCursor.class);

    WriteCursorStrategy WRITE_CURSOR_STRATEGY = mock(WriteCursorStrategy.class);
}
