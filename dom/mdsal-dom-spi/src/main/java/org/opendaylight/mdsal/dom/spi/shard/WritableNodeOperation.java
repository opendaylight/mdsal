/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

@Beta
public abstract class WritableNodeOperation implements WriteCursorStrategy {
    private final WriteableModificationNode node;
    private final DOMDataTreeWriteCursor cursor;

    protected WritableNodeOperation(final WriteableModificationNode node, final DOMDataTreeWriteCursor cursor) {
        this.node = Preconditions.checkNotNull(node);
        this.cursor = Preconditions.checkNotNull(cursor);
    }

    protected final DOMDataTreeWriteCursor getCursor() {
        return cursor;
    }

    private void delete(final PathArgument arg, final WriteableModificationNode potentialChild) {
        cursor.delete(arg);
        if (potentialChild != null) {
            potentialChild.markDeleted();
        }
    }

    @Override
    public final void delete(final PathArgument arg) {
        delete(arg, node.getChild(arg));
    }

    @Override
    public final void merge(final PathArgument arg, final NormalizedNode<?, ?> data) {
        WriteableModificationNode potentialChild = node.getChild(arg);
        if (potentialChild == null) {
            cursor.merge(arg, data);
        } else {
            potentialChild.createOperation(cursor).mergeToCurrent((NormalizedNodeContainer<?, ?, ?>) data);
        }
    }

    @Override
    public final void write(final PathArgument arg, final NormalizedNode<?, ?> data) {
        WriteableModificationNode potentialChild = node.getChild(arg);
        if (potentialChild == null) {
            cursor.write(arg, data);
        } else {
            potentialChild.createOperation(cursor).writeToCurrent((NormalizedNodeContainer<?, ?, ?>) data);
        }
    }

    @Override
    public final WriteCursorStrategy enter(final PathArgument arg) {
        cursor.enter(arg);
        WriteableModificationNode child = node.getChild(arg);
        if (child != null) {
            return child.createOperation(cursor);
        }
        return new DelegatingWriteCursorStrategy() {
            @Override
            protected DOMDataTreeWriteCursor delegate() {
                return cursor;
            }
        };
    }

    @Override
    public final void mergeToCurrent(final NormalizedNodeContainer<?, ?, ?> data) {
        for (NormalizedNode<?, ?> child : data.getValue()) {
            PathArgument childId = child.getIdentifier();
            WriteableModificationNode shardChild = node.getChild(childId);
            if (shardChild != null) {
                // FIXME: Decompose somehow
                throw new UnsupportedOperationException("Not implemented yet");
            }

            cursor.merge(childId, child);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final void writeToCurrent(final NormalizedNodeContainer<?, ?, ?> data) {
        // write the entire thing into the cursor
        write(data.getIdentifier(), data);
        // write the children with subshard check and subshard write if we are going into subshard
        cursor.enter(data.getIdentifier());
        for (NormalizedNode<?, ?> writtenChild : data.getValue()) {
            write(writtenChild.getIdentifier(), writtenChild);
        }
        // Delete step - remove subshard data that was written into current shard
        // delete from current
        node.getChildrenWithSubshards().entrySet()
                .stream().filter(entry -> entry.getValue() instanceof WriteableSubshardBoundaryNode).forEach(entry -> {
                    @SuppressWarnings("unchecked")
                    Optional<NormalizedNode<?, ?>> writtenValue =
                            ((NormalizedNodeContainer) data).getChild(entry.getKey());
                    if (writtenValue.isPresent()) {
                        // delete from current
                        cursor.delete(entry.getKey());
                    }
                });

        cursor.exit();
    }
}
