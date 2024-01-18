/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactions;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ExecutorServiceUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory DOM Data Store. Implementation of {@link DOMStore} which uses {@link DataTree} and other classes such as
 * {@link SnapshotBackedWriteTransaction}.
 * {@link org.opendaylight.mdsal.dom.spi.store.SnapshotBackedReadTransaction} to implement {@link DOMStore} contract.
 */
public class InMemoryDOMDataStore extends TransactionReadyPrototype<String> implements DOMStore,
        Identifiable<String>, AutoCloseable, DOMStoreTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataStore.class);

    private final AtomicLong txCounter = new AtomicLong(0);
    private final DataTree dataTree;

    private final InMemoryDOMStoreTreeChangePublisher changePublisher;
    private final ExecutorService dataChangeListenerExecutor;
    private final boolean debugTransactions;
    private final @NonNull String name;

    private volatile AutoCloseable closeable;

    public InMemoryDOMDataStore(final String name, final ExecutorService dataChangeListenerExecutor) {
        this(name, dataChangeListenerExecutor,
            InMemoryDOMDataStoreConfigProperties.DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE, false);
    }

    public InMemoryDOMDataStore(final String name, final ExecutorService dataChangeListenerExecutor,
            final int maxDataChangeListenerQueueSize, final boolean debugTransactions) {
        this(name, LogicalDatastoreType.OPERATIONAL, dataChangeListenerExecutor, maxDataChangeListenerQueueSize,
            debugTransactions);
    }

    public InMemoryDOMDataStore(final String name, final LogicalDatastoreType type,
            final ExecutorService dataChangeListenerExecutor, final int maxDataChangeListenerQueueSize,
            final boolean debugTransactions) {
        this(name, defaultConfig(type), dataChangeListenerExecutor, maxDataChangeListenerQueueSize, debugTransactions);
    }

    public InMemoryDOMDataStore(final String name, final DataTreeConfiguration config,
            final ExecutorService dataChangeListenerExecutor, final int maxDataChangeListenerQueueSize,
            final boolean debugTransactions) {
        this.name = requireNonNull(name);
        this.dataChangeListenerExecutor = requireNonNull(dataChangeListenerExecutor);
        this.debugTransactions = debugTransactions;
        dataTree = new InMemoryDataTreeFactory().create(config);
        changePublisher = new InMemoryDOMStoreTreeChangePublisher("name", this.dataChangeListenerExecutor,
                maxDataChangeListenerQueueSize);
    }

    public void setCloseable(final AutoCloseable closeable) {
        this.closeable = closeable;
    }

    @Override
    public final String getIdentifier() {
        return name;
    }

    public final synchronized void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        dataTree.setEffectiveModelContext(newModelContext);
    }

    @Override
    public DOMStoreReadTransaction newReadOnlyTransaction() {
        return SnapshotBackedTransactions.newReadTransaction(nextIdentifier(), debugTransactions,
            dataTree.takeSnapshot());
    }

    @Override
    public DOMStoreReadWriteTransaction newReadWriteTransaction() {
        return SnapshotBackedTransactions.newReadWriteTransaction(nextIdentifier(), debugTransactions,
            dataTree.takeSnapshot(), this);
    }

    @Override
    public DOMStoreWriteTransaction newWriteOnlyTransaction() {
        return SnapshotBackedTransactions.newWriteTransaction(nextIdentifier(), debugTransactions,
            dataTree.takeSnapshot(), this);
    }

    @Override
    public DOMStoreTransactionChain createTransactionChain() {
        return new DOMStoreTransactionChainImpl(this);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void close() {
        ExecutorServiceUtil.tryGracefulShutdown(dataChangeListenerExecutor, 30, TimeUnit.SECONDS);

        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.debug("Error closing instance", e);
            }
        }
    }

    public final boolean getDebugTransactions() {
        return debugTransactions;
    }

    final DataTreeSnapshot takeSnapshot() {
        return dataTree.takeSnapshot();
    }

    @Override
    public synchronized Registration registerTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener) {
        // Make sure commit is not occurring right now. Listener has to be registered and its state capture enqueued at
        // a consistent point.
        return changePublisher.registerTreeChangeListener(treeId, listener, dataTree.takeSnapshot());
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public Registration registerLegacyTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener) {
        return registerTreeChangeListener(treeId, listener);
    }

    @Override
    protected void transactionAborted(final SnapshotBackedWriteTransaction<String> tx) {
        LOG.debug("Tx: {} is closed.", tx.getIdentifier());
    }

    @Override
    protected DOMStoreThreePhaseCommitCohort transactionReady(final SnapshotBackedWriteTransaction<String> tx,
            final DataTreeModification modification, final Exception readyError) {
        LOG.debug("Tx: {} is submitted. Modifications: {}", tx.getIdentifier(), modification);
        return new InMemoryDOMStoreThreePhaseCommitCohort(this, tx, modification, readyError);
    }

    String nextIdentifier() {
        return name + "-" + txCounter.getAndIncrement();
    }

    void validate(final DataTreeModification modification) throws DataValidationFailedException {
        dataTree.validate(modification);
    }

    DataTreeCandidate prepare(final DataTreeModification modification) throws DataValidationFailedException {
        return dataTree.prepare(modification);
    }

    synchronized void commit(final DataTreeCandidate candidate) {
        dataTree.commit(candidate);
        changePublisher.publishChange(candidate);
    }

    private static DataTreeConfiguration defaultConfig(final LogicalDatastoreType type) {
        return switch (type) {
            case CONFIGURATION -> DataTreeConfiguration.DEFAULT_CONFIGURATION;
            case OPERATIONAL -> DataTreeConfiguration.DEFAULT_OPERATIONAL;
        };
    }
}
