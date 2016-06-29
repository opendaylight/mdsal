/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InmemoryDOMDataTreeShardWriteTransaction implements DOMDataTreeShardWriteTransaction {

    private static final Logger LOG = LoggerFactory.getLogger(InmemoryDOMDataTreeShardWriteTransaction.class);

    private enum SimpleCursorOperation {
        MERGE {
            @Override
            void applyOnLeaf(final DOMDataTreeWriteCursor cursor, final PathArgument child,
                    final NormalizedNode<?, ?> data) {
                cursor.merge(child, data);
            }
        },
        DELETE {
            @Override
            void applyOnLeaf(final DOMDataTreeWriteCursor cursor, final PathArgument child,
                    final NormalizedNode<?, ?> data) {
                cursor.delete(child);
            }
        },
        WRITE {
            @Override
            void applyOnLeaf(final DOMDataTreeWriteCursor cursor, final PathArgument child,
                    final NormalizedNode<?, ?> data) {
                cursor.write(child, data);
            }
        };

        abstract void applyOnLeaf(DOMDataTreeWriteCursor cursor, PathArgument child, NormalizedNode<?, ?> data);

        void apply(final DOMDataTreeWriteCursor cursor, final YangInstanceIdentifier path,
                final NormalizedNode<?, ?> data) {
            int enterCount = 0;
            final Iterator<PathArgument> it = path.getPathArguments().iterator();
            while (it.hasNext()) {
                final PathArgument currentArg = it.next();
                if (it.hasNext()) {
                    // We need to enter one level deeper, we are not at leaf (modified) node
                    cursor.enter(currentArg);
                    enterCount++;
                } else {
                    applyOnLeaf(cursor, currentArg, data);
                }
            }
            cursor.exit(enterCount);
        }
    }

    private final ShardDataModification modification;
    private DOMDataTreeWriteCursor cursor;
    private final DataTree rootShardDataTree;
    private DataTreeModification rootModification = null;

    private final ArrayList<DOMStoreThreePhaseCommitCohort> cohorts = new ArrayList<>();
    private final InMemoryDOMDataTreeShardChangePublisher changePublisher;
    private boolean finished = false;

    private final ListeningExecutorService executor;

    InmemoryDOMDataTreeShardWriteTransaction(final ShardDataModification root,
                                             final DataTree rootShardDataTree,
                                             final InMemoryDOMDataTreeShardChangePublisher changePublisher, final ListeningExecutorService executor) {
        this.modification = Preconditions.checkNotNull(root);
        this.rootShardDataTree = Preconditions.checkNotNull(rootShardDataTree);
        this.changePublisher = Preconditions.checkNotNull(changePublisher);
        this.executor = executor;
    }

    private DOMDataTreeWriteCursor getCursor() {
        if (cursor == null) {
            cursor = new ShardDataModificationCursor(modification, this);
        }
        return cursor;
    }

    void delete(final YangInstanceIdentifier path) {
        final YangInstanceIdentifier relativePath = toRelative(path);
        Preconditions.checkArgument(!YangInstanceIdentifier.EMPTY.equals(relativePath),
                "Deletion of shard root is not allowed");
        SimpleCursorOperation.DELETE.apply(getCursor(), relativePath , null);
    }

    void merge(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        SimpleCursorOperation.MERGE.apply(getCursor(), toRelative(path), data);
    }

    void write(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        SimpleCursorOperation.DELETE.apply(getCursor(), toRelative(path), data);
    }

    private YangInstanceIdentifier toRelative(final YangInstanceIdentifier path) {
        final Optional<YangInstanceIdentifier> relative =
                path.relativeTo(modification.getPrefix().getRootIdentifier());
        Preconditions.checkArgument(relative.isPresent());
        return relative.get();
    }

    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(final YangInstanceIdentifier path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public CheckedFuture<Boolean, ReadFailedException> exists(final YangInstanceIdentifier path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void close() {
        Preconditions.checkState(!finished, "Attempting to close an already finished transaction.");
        modification.closeTransactions();
        if (cursor != null) {
            cursor.close();
        }
        finished = true;
    }

    void cursorClosed() {
        Preconditions.checkNotNull(cursor);
        cursor = null;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void ready() {
        Preconditions.checkState(!finished, "Attempting to ready an already finished transaction.");
        Preconditions.checkState(cursor == null, "Attempting to ready a transaction that has an open cursor.");
        Preconditions.checkNotNull(modification, "Attempting to ready an empty transaction.");

        LOG.debug("Readying open transaction on shard {}", modification.getPrefix());
        rootModification = modification.seal();

        cohorts.add(new InMemoryDOMDataTreeShardThreePhaseCommitCohort(rootShardDataTree, rootModification, changePublisher));
        for (final Entry<DOMDataTreeIdentifier, ForeignShardModificationContext> entry : modification.getChildShards().entrySet()) {
            cohorts.add(new ForeignShardThreePhaseCommitCohort(entry.getKey(), entry.getValue()));
        }
        finished = true;
    }

    @Override
    public ListenableFuture<Void> submit() {
        LOG.debug("Submitting open transaction on shard {}", modification.getPrefix());

        Preconditions.checkNotNull(cohorts);
        Preconditions.checkState(!cohorts.isEmpty(), "Transaction was not readied yet.");

        final ListenableFuture<Void> submit = executor.submit(new ShardSubmitCoordinationTask(modification.getPrefix(), cohorts));

        return submit;
    }

    @Override
    public ListenableFuture<Boolean> validate() {
        LOG.debug("CanCommit on open transaction on shard {}", modification.getPrefix());

        final ListenableFuture<Boolean> submit = executor.submit(new ShardCanCommitCoordinationTask(modification.getPrefix(), cohorts));
        return submit;
    }

    @Override
    public ListenableFuture<Void> prepare() {
        LOG.debug("PreCommit on open transaction on shard {}", modification.getPrefix());

        final ListenableFuture<Void> submit = executor.submit(new ShardPreCommitCoordinationTask(modification.getPrefix(), cohorts));
        return submit;
    }

    @Override
    public ListenableFuture<Void> commit() {
        LOG.debug("Commit open transaction on shard {}", modification.getPrefix());

        final ListenableFuture<Void> submit = executor.submit(new ShardCommitCoordinationTask(modification.getPrefix(), cohorts));
        return submit;
    }

    public void followUp() {

    }

    @Override
    public DOMDataTreeWriteCursor createCursor(final DOMDataTreeIdentifier prefix) {
        Preconditions.checkState(!finished, "Transaction is finished/closed already.");
        Preconditions.checkState(cursor == null, "Previous cursor wasn't closed");
        final DOMDataTreeWriteCursor ret = getCursor();
        final YangInstanceIdentifier relativePath = toRelative(prefix.getRootIdentifier());
        ret.enter(relativePath.getPathArguments());
        return ret;
    }
}
