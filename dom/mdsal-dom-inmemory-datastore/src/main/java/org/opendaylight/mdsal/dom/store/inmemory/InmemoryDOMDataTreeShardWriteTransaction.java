/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.Iterator;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class InmemoryDOMDataTreeShardWriteTransaction implements DOMDataTreeShardWriteTransaction {

    private DOMDataTreeWriteCursor cursor;
    private ShardDataModification modification;


    InmemoryDOMDataTreeShardWriteTransaction(ShardDataModification root) {
        this.modification = root;
    }

    private DOMDataTreeWriteCursor getCursor() {
        if (cursor == null) {
            cursor = new ShardDataModificationCursor(modification);
        }
        return cursor;
    }



    public void delete(YangInstanceIdentifier path) {
        YangInstanceIdentifier relativePath = toRelative(path);
        Preconditions.checkArgument(!YangInstanceIdentifier.EMPTY.equals(relativePath),
                "Deletion of shard root is not allowed");
        SimpleCursorOperation.DELETE.apply(getCursor(), relativePath , null);
    }


    public void merge(YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        SimpleCursorOperation.MERGE.apply(getCursor(), toRelative(path), data);
    }


    public void write(YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        SimpleCursorOperation.DELETE.apply(getCursor(), toRelative(path), data);
    }

    private YangInstanceIdentifier toRelative(YangInstanceIdentifier path) {
        Optional<YangInstanceIdentifier> relative =
                path.relativeTo(modification.getPrefix().getRootIdentifier());
        Preconditions.checkArgument(relative.isPresent());
        return relative.get();
    }


    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(
            YangInstanceIdentifier path) {
        // FIXME: Implement this
        return null;
    }


    public CheckedFuture<Boolean, ReadFailedException> exists(YangInstanceIdentifier path) {
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

        modification.seal();


        return;
    }

    private enum SimpleCursorOperation {
        MERGE {
            @Override
            void applyOnLeaf(DOMDataTreeWriteCursor cursor, PathArgument child,
                    NormalizedNode<?, ?> data) {
                cursor.merge(child, data);
            }
        },
        DELETE {
            @Override
            void applyOnLeaf(DOMDataTreeWriteCursor cursor, PathArgument child,
                    NormalizedNode<?, ?> data) {
                cursor.delete(child);
            }
        },
        WRITE {
            @Override
            void applyOnLeaf(DOMDataTreeWriteCursor cursor, PathArgument child,
                    NormalizedNode<?, ?> data) {
                cursor.write(child, data);
            }
    
        };
    
        abstract void applyOnLeaf(DOMDataTreeWriteCursor cursor, PathArgument child,
                NormalizedNode<?, ?> data);
    
        void apply(DOMDataTreeWriteCursor cursor, YangInstanceIdentifier path,
                NormalizedNode<?, ?> data) {
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

    public void followUp() {

    }

    @Override
    public DOMDataTreeWriteCursor createCursor(DOMDataTreeIdentifier prefix) {
        DOMDataTreeWriteCursor ret = getCursor();
        YangInstanceIdentifier relativePath = toRelative(prefix.getRootIdentifier());
        ret.enter(relativePath.getPathArguments());
        return ret;
    }

}
