/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.spi.shard.ForeignShardModificationContext;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InmemoryDOMDataTreeShardWriteTransaction implements DOMDataTreeShardWriteTransaction, Identifiable<String> {

    private static final Logger LOG = LoggerFactory.getLogger(InmemoryDOMDataTreeShardWriteTransaction.class);

    private enum SimpleCursorOperation {
        MERGE {
            @Override
            void applyOnLeaf(final DOMDataTreeWriteCursor cur, final PathArgument child,
                             final NormalizedNode<?, ?> data) {
                cur.merge(child, data);
            }
        },
        DELETE {
            @Override
            void applyOnLeaf(final DOMDataTreeWriteCursor cur, final PathArgument child,
                             final NormalizedNode<?, ?> data) {
                cur.delete(child);
            }
        },
        WRITE {
            @Override
            void applyOnLeaf(final DOMDataTreeWriteCursor cur, final PathArgument child,
                             final NormalizedNode<?, ?> data) {
                cur.write(child, data);
            }
        };

        abstract void applyOnLeaf(DOMDataTreeWriteCursor cur, PathArgument child, NormalizedNode<?, ?> data);

        void apply(final DOMDataTreeWriteCursor cur, final YangInstanceIdentifier path,
                   final NormalizedNode<?, ?> data) {
            int enterCount = 0;
            final Iterator<PathArgument> it = path.getPathArguments().iterator();
            if (it.hasNext()) {
                while (true) {
                    final PathArgument currentArg = it.next();
                    if (!it.hasNext()) {
                        applyOnLeaf(cur, currentArg, data);
                        break;
                    }

                    // We need to enter one level deeper, we are not at leaf (modified) node
                    cur.enter(currentArg);
                    enterCount++;
                }
            }

            cur.exit(enterCount);
        }
    }

    private static final AtomicLong COUNTER = new AtomicLong();

    private final ArrayList<DOMStoreThreePhaseCommitCohort> cohorts = new ArrayList<>();
    private final InMemoryDOMDataTreeShardChangePublisher changePublisher;
    private final InMemoryDOMDataTreeShardProducer producer;
    private final ShardDataModification modification;
    private final ListeningExecutorService executor;
    private final DataTree rootShardDataTree;
    private final String identifier;

    private DataTreeModification rootModification = null;
    private DOMDataTreeWriteCursor cursor;
    private boolean finished = false;

    InmemoryDOMDataTreeShardWriteTransaction(final InMemoryDOMDataTreeShardProducer producer,
                                             final ShardDataModification root,
                                             final DataTree rootShardDataTree,
                                             final InMemoryDOMDataTreeShardChangePublisher changePublisher,
                                             final ListeningExecutorService executor) {
        this.producer = producer;
        this.modification = requireNonNull(root);
        this.rootShardDataTree = requireNonNull(rootShardDataTree);
        this.changePublisher = requireNonNull(changePublisher);
        this.identifier = "INMEMORY-SHARD-TX-" + COUNTER.getAndIncrement();
        LOG.debug("Shard transaction{} created", identifier);
        this.executor = executor;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    private DOMDataTreeWriteCursor getCursor() {
        if (cursor == null) {
            cursor = new InMemoryShardDataModificationCursor(modification, this);
        }
        return cursor;
    }

    void delete(final YangInstanceIdentifier path) {
        final YangInstanceIdentifier relativePath = toRelative(path);
        Preconditions.checkArgument(!YangInstanceIdentifier.EMPTY.equals(relativePath),
                "Deletion of shard root is not allowed");
        SimpleCursorOperation.DELETE.apply(getCursor(), relativePath, null);
    }

    void merge(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        SimpleCursorOperation.MERGE.apply(getCursor(), toRelative(path), data);
    }

    void write(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        SimpleCursorOperation.WRITE.apply(getCursor(), toRelative(path), data);
    }

    private YangInstanceIdentifier toRelative(final YangInstanceIdentifier path) {
        final Optional<YangInstanceIdentifier> relative =
                path.relativeTo(modification.getPrefix().getRootIdentifier());
        Preconditions.checkArgument(relative.isPresent());
        return relative.get();
    }

    @Override
    public void close() {
        Preconditions.checkState(!finished, "Attempting to close an already finished transaction.");
        modification.closeTransactions();
        if (cursor != null) {
            cursor.close();
        }
        producer.transactionAborted(this);
        finished = true;
    }

    void cursorClosed() {
        requireNonNull(cursor);
        modification.closeCursor();
        cursor = null;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void ready() {
        Preconditions.checkState(!finished, "Attempting to ready an already finished transaction.");
        Preconditions.checkState(cursor == null, "Attempting to ready a transaction that has an open cursor.");
        requireNonNull(modification, "Attempting to ready an empty transaction.");

        LOG.debug("Readying open transaction on shard {}", modification.getPrefix());
        rootModification = modification.seal();

        producer.transactionReady(this, rootModification);
        cohorts.add(new InMemoryDOMDataTreeShardThreePhaseCommitCohort(
                rootShardDataTree, rootModification, changePublisher));
        for (final Entry<DOMDataTreeIdentifier, ForeignShardModificationContext> entry :
                modification.getChildShards().entrySet()) {
            cohorts.add(new ForeignShardThreePhaseCommitCohort(entry.getKey(), entry.getValue()));
        }
        finished = true;
    }

    @Override
    public ListenableFuture<Void> submit() {
        LOG.debug("Submitting open transaction on shard {}", modification.getPrefix());

        requireNonNull(cohorts);
        Preconditions.checkState(!cohorts.isEmpty(), "Transaction was not readied yet.");

        return executor.submit(new ShardSubmitCoordinationTask(modification.getPrefix(), cohorts, this));
    }

    @Override
    public ListenableFuture<Boolean> validate() {
        LOG.debug("CanCommit on open transaction on shard {}", modification.getPrefix());
        return executor.submit(new ShardCanCommitCoordinationTask(modification.getPrefix(), cohorts));
    }

    @Override
    public ListenableFuture<Void> prepare() {
        LOG.debug("PreCommit on open transaction on shard {}", modification.getPrefix());
        return executor.submit(new ShardPreCommitCoordinationTask(modification.getPrefix(), cohorts));
    }

    @Override
    public ListenableFuture<Void> commit() {
        LOG.debug("Commit open transaction on shard {}", modification.getPrefix());
        return executor.submit(new ShardCommitCoordinationTask(modification.getPrefix(), cohorts, this));
    }

    DataTreeModification getRootModification() {
        requireNonNull(rootModification, "Transaction wasn't sealed yet");
        return rootModification;
    }

    void transactionCommited(final InmemoryDOMDataTreeShardWriteTransaction tx) {
        producer.onTransactionCommited(tx);
    }

    @Override
    public DOMDataTreeWriteCursor createCursor(final DOMDataTreeIdentifier prefix) {
        Preconditions.checkState(!finished, "Transaction is finished/closed already.");
        Preconditions.checkState(cursor == null, "Previous cursor wasn't closed");
        final YangInstanceIdentifier relativePath = toRelative(prefix.getRootIdentifier());
        final DOMDataTreeWriteCursor ret = getCursor();
        ret.enter(relativePath.getPathArguments());
        return ret;
    }
}
