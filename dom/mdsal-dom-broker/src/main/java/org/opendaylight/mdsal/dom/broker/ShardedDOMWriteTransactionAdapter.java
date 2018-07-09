/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardedDOMWriteTransactionAdapter implements DOMDataTreeWriteTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMWriteTransactionAdapter.class);

    private final Map<LogicalDatastoreType, DOMDataTreeProducer> producerMap;
    private final Map<LogicalDatastoreType, DOMDataTreeCursorAwareTransaction> transactionMap;
    private final Map<LogicalDatastoreType, DOMDataTreeWriteCursor> cursorMap;

    private final DOMDataTreeService treeService;
    private final Object txIdentifier;
    private boolean finished = false;
    private boolean initialized = false;

    ShardedDOMWriteTransactionAdapter(final Object identifier, final DOMDataTreeService transactionDelegator) {
        this.treeService = Preconditions.checkNotNull(transactionDelegator);
        this.txIdentifier = Preconditions.checkNotNull(identifier);
        this.producerMap = new EnumMap<>(LogicalDatastoreType.class);
        this.transactionMap = new EnumMap<>(LogicalDatastoreType.class);
        this.cursorMap = new EnumMap<>(LogicalDatastoreType.class);
    }

    @Override
    public boolean cancel() {
        LOG.debug("{}: Cancelling transaction");
        if (finished) {
            return false;
        }

        // We close cursor, cancel transactions and close producers and
        // mark transaction as finished
        cursorMap.values().forEach(DOMDataTreeWriteCursor::close);
        transactionMap.values().forEach(domDataTreeCursorAwareTransaction ->
                Preconditions.checkState(domDataTreeCursorAwareTransaction.cancel()));
        closeProducers();
        finished = true;
        return true;
    }

    @Override
    public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
        checkRunning();
        LOG.debug("{}: Submitting transaction", txIdentifier);
        if (!initialized) {
            // If underlying producers, transactions and cursors are
            // not even initialized just seal this transaction and
            // return immediate future
            finished = true;
            return CommitInfo.emptyFluentFuture();
        }
        // First we need to close cursors
        cursorMap.values().forEach(DOMDataTreeWriteCursor::close);
        final FluentFuture<List<Void>> aggregatedSubmit = FluentFuture.from(Futures.allAsList(
                transactionMap.get(LogicalDatastoreType.CONFIGURATION).submit(),
                transactionMap.get(LogicalDatastoreType.OPERATIONAL).submit()));

        // Now we can close producers and mark transaction as finished
        closeProducers();
        finished = true;

        return aggregatedSubmit.transform(unused -> CommitInfo.empty(), MoreExecutors.directExecutor());
    }

    @Override
    public Object getIdentifier() {
        return txIdentifier;
    }

    @Override
    public void put(final LogicalDatastoreType store, final YangInstanceIdentifier path,
                    final NormalizedNode<?, ?> data) {
        checkRunning();
        LOG.debug("{}: Invoking put operation at {}:{} with payload {}", txIdentifier, store, path);
        if (!initialized) {
            initializeDataTreeProducerLayer(path.getParent());
        }

        final DOMDataTreeWriteCursor cursor = cursorMap.get(store);
        cursor.write(path.getLastPathArgument(), data);
    }

    @Override
    public void merge(final LogicalDatastoreType store, final YangInstanceIdentifier path,
                      final NormalizedNode<?, ?> data) {
        checkRunning();
        LOG.debug("{}: Invoking merge operation at {}:{} with payload {}", txIdentifier, store, path);
        if (!initialized) {
            initializeDataTreeProducerLayer(path.getParent());
        }

        final DOMDataTreeWriteCursor cursor = cursorMap.get(store);
        cursor.merge(path.getLastPathArgument(), data);
    }

    @Override
    public void delete(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        checkRunning();
        LOG.debug("{}: Invoking delete operation at {}:{}", txIdentifier, store, path);
        if (!initialized) {
            initializeDataTreeProducerLayer(path.getParent());
        }

        final DOMDataTreeWriteCursor cursor = cursorMap.get(store);
        cursor.delete(path.getLastPathArgument());
    }

    // TODO initialize producer, transaction and cursor for only
    // for necessary data store at one time
    private void initializeDataTreeProducerLayer(final YangInstanceIdentifier path) {
        Preconditions.checkState(producerMap.isEmpty(), "Producers already initialized");
        Preconditions.checkState(transactionMap.isEmpty(), "Transactions already initialized");
        Preconditions.checkState(cursorMap.isEmpty(), "Cursors already initialized");

        LOG.debug("{}: Creating data tree producers on path {}", txIdentifier, path);
        producerMap.put(LogicalDatastoreType.CONFIGURATION,
                treeService.createProducer(
                        Collections.singleton(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, path))));
        producerMap.put(LogicalDatastoreType.OPERATIONAL,
                treeService.createProducer(
                        Collections.singleton(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path))));

        LOG.debug("{}: Creating DOMDataTreeCursorAwareTransactions delegates", txIdentifier, path);
        transactionMap.put(LogicalDatastoreType.CONFIGURATION,
                producerMap.get(LogicalDatastoreType.CONFIGURATION).createTransaction(true));
        transactionMap.put(LogicalDatastoreType.OPERATIONAL,
                producerMap.get(LogicalDatastoreType.OPERATIONAL).createTransaction(true));

        LOG.debug("{}: Creating DOMDataTreeWriteCursors delegates");
        cursorMap.put(LogicalDatastoreType.CONFIGURATION,
                transactionMap.get(LogicalDatastoreType.CONFIGURATION)
                        .createCursor(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, path)));
        cursorMap.put(LogicalDatastoreType.OPERATIONAL,
                transactionMap.get(LogicalDatastoreType.OPERATIONAL)
                        .createCursor(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path)));

        initialized = true;
    }

    private void checkRunning() {
        Preconditions.checkState(!finished, "{}: Transaction already finished");
    }

    private void closeProducers() {
        producerMap.values().forEach(domDataTreeProducer -> {
            try {
                domDataTreeProducer.close();
            } catch (final DOMDataTreeProducerException e) {
                throw new IllegalStateException("Trying to close DOMDataTreeProducer with open transaction", e);
            }
        });
    }
}
