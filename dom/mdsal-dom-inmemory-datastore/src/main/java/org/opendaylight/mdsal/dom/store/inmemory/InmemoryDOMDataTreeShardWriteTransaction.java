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
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardWriteTransaction;
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
            Iterator<PathArgument> it = path.getPathArguments().iterator();
            while (it.hasNext()) {
                PathArgument currentArg = it.next();
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
    private DataTree rootShardDataTree;
    private DataTreeModification rootModification = null;

    private ArrayList<DOMStoreThreePhaseCommitCohort> cohorts = new ArrayList<>();
    private InMemoryDOMDataTreeShardChangePublisher changePublisher;

    // FIXME inject into shard?
    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    InmemoryDOMDataTreeShardWriteTransaction(final ShardDataModification root,
                                             final DataTree rootShardDataTree,
                                             final InMemoryDOMDataTreeShardChangePublisher changePublisher) {
        this.modification = Preconditions.checkNotNull(root);
        this.rootShardDataTree = Preconditions.checkNotNull(rootShardDataTree);
        this.changePublisher = Preconditions.checkNotNull(changePublisher);
    }

    private DOMDataTreeWriteCursor getCursor() {
        if (cursor == null) {
            cursor = new ShardDataModificationCursor(modification);
        }
        return cursor;
    }

    void delete(final YangInstanceIdentifier path) {
        YangInstanceIdentifier relativePath = toRelative(path);
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
        Optional<YangInstanceIdentifier> relative =
                path.relativeTo(modification.getPrefix().getRootIdentifier());
        Preconditions.checkArgument(relative.isPresent());
        return relative.get();
    }

    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(final YangInstanceIdentifier path) {
        // FIXME: Implement this
        return null;
    }

    public CheckedFuture<Boolean, ReadFailedException> exists(final YangInstanceIdentifier path) {
        // TODO Auto-generated method stub
        return null;
    }


    public Object getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public void ready() {

        LOG.debug("Readying open transaction on shard {}", modification.getPrefix());
        rootModification = modification.seal();

        cohorts.add(new InMemoryDOMDataTreeShardThreePhaseCommitCohort(rootShardDataTree, rootModification, changePublisher));
        for (Entry<DOMDataTreeIdentifier, ForeignShardModificationContext> entry : modification.getChildShards().entrySet()) {
            cohorts.add(new ForeignShardThreePhaseCommitCohort(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public ListenableFuture<Void> submit() {
        LOG.debug("Submitting open transaction on shard {}", modification.getPrefix());

        Preconditions.checkNotNull(cohorts);
        Preconditions.checkState(!cohorts.isEmpty(), "Submitting an empty transaction");

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
        DOMDataTreeWriteCursor ret = getCursor();
        YangInstanceIdentifier relativePath = toRelative(prefix.getRootIdentifier());
        ret.enter(relativePath.getPathArguments());
        return ret;
    }
}
