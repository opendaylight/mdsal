/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactions;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMStore;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory DOM Data Store. Implementation of {@link DOMStore} which uses {@link DataTree} and other classes such as
 * {@link SnapshotBackedWriteTransaction}.
 * {@link org.opendaylight.mdsal.dom.spi.store.SnapshotBackedReadTransaction} to implement {@link DOMStore} contract.
 */
public final class InMemoryDOMStoreImpl extends TransactionReadyPrototype<@NonNull String> implements InMemoryDOMStore {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMStoreImpl.class);

    private final InMemoryTree tree;

    public InMemoryDOMStoreImpl(final InMemoryTree tree) {
        this.tree = requireNonNull(tree);
    }

    public void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        synchronized (tree) {
            tree.dataTree.setEffectiveModelContext(newModelContext);
        }
    }

    @Override
    public String getIdentifier() {
        return tree.name;
    }

    @NonNullByDefault
    private DataTree dataTree() {
        return tree.dataTree;
    }

    private boolean debug() {
        return tree.debug();
    }

    @NonNullByDefault
    private String nextId() {
        return tree.nextId();
    }

    @NonNullByDefault
    DataTreeSnapshot takeSnapshot() {
        return dataTree().takeSnapshot();
    }

    @Override
    public DOMStoreReadTransaction newReadOnlyTransaction() {
        return SnapshotBackedTransactions.newReadTransaction(nextId(), debug(), takeSnapshot());
    }

    @Override
    public DOMStoreReadWriteTransaction newReadWriteTransaction() {
        return SnapshotBackedTransactions.newReadWriteTransaction(nextId(), debug(), takeSnapshot(), this);
    }

    @Override
    public DOMStoreWriteTransaction newWriteOnlyTransaction() {
        return SnapshotBackedTransactions.newWriteTransaction(nextId(), debug(), takeSnapshot(), this);
    }

    @Override
    public DOMStoreTransactionChain createTransactionChain() {
        return new DOMStoreTransactionChainImpl(tree);
    }

    @Override
    public Registration registerTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener) {
        synchronized (tree) {
            return tree.publisher.registerTreeChangeListener(treeId, listener, takeSnapshot());
        }
    }

    @Override
    protected void transactionAborted(final SnapshotBackedWriteTransaction<@NonNull String> tx) {
        LOG.debug("Tx: {} is closed.", tx.getIdentifier());
    }

    @Override
    protected DOMStoreThreePhaseCommitCohort transactionReady(final SnapshotBackedWriteTransaction<@NonNull String> tx,
            final DataTreeModification modification, final Exception readyError) {
        LOG.debug("Tx: {} is submitted. Modifications: {}", tx.getIdentifier(), modification);
        return new InMemoryDOMStoreThreePhaseCommitCohort(this, tx, modification, readyError);
    }

    @Override
    public void close() {
        synchronized (tree) {
            tree.close();
        }
    }
}
