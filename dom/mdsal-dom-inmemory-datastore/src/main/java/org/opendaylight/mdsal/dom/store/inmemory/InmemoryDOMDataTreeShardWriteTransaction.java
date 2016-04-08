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
import java.util.Iterator;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;

class InmemoryDOMDataTreeShardWriteTransaction implements DOMDataTreeShardWriteTransaction {

    private InMemoryDOMDataTreeShardThreePhaseCommitCohort commitCohort;

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

    InmemoryDOMDataTreeShardWriteTransaction(final ShardDataModification root) {
        this.modification = Preconditions.checkNotNull(root);
    }

    InmemoryDOMDataTreeShardWriteTransaction(final ShardDataModification root, final DataTree rootShardDataTree) {
        this.modification = Preconditions.checkNotNull(root);
        this.rootShardDataTree = Preconditions.checkNotNull(rootShardDataTree);
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

        rootModification = modification.seal();

        commitCohort = new InMemoryDOMDataTreeShardThreePhaseCommitCohort(rootShardDataTree, rootModification, modification.getChildShards());
    }

    @Override
    public ListenableFuture<Boolean> validate() {
        return commitCohort.canCommit();
    }

    @Override
    public ListenableFuture<Void> prepare() {
        return commitCohort.preCommit();
    }

    @Override
    public ListenableFuture<Void> commit() {
        return commitCohort.commit();
    }

    public void submit() {
        Preconditions.checkNotNull(commitCohort);

        commitCohort.canCommit();
        commitCohort.preCommit();
        commitCohort.commit();
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
