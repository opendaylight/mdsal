/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.dom.spi.store.AbstractSnapshotBackedTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

final class DOMStoreTransactionChainImpl extends AbstractSnapshotBackedTransactionChain<String> {
    private final InMemoryTree tree;

    DOMStoreTransactionChainImpl(final InMemoryTree tree) {
        this.tree = requireNonNull(tree);
    }

    @Override
    protected DOMStoreThreePhaseCommitCohort createCohort(final SnapshotBackedWriteTransaction<String> tx,
            final DataTreeModification modification, final Exception operationError) {
        return new ChainedTransactionCommitImpl(tree, tx, modification, this, operationError);
    }

    @Override
    protected DataTreeSnapshot takeSnapshot() {
        return tree.dataTree.takeSnapshot();
    }

    @Override
    protected String nextTransactionIdentifier() {
        return tree.nextId();
    }

    @Override
    protected boolean getDebugTransactions() {
        return tree.debug();
    }

    void transactionCommited(final SnapshotBackedWriteTransaction<String> transaction) {
        super.onTransactionCommited(transaction);
    }
}
