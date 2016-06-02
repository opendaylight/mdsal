/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class ShardDataModificationCursor implements DOMDataTreeWriteCursor {

    private final Deque<WriteCursorStrategy> stack = new ArrayDeque<>();
    private final InmemoryDOMDataTreeShardWriteTransaction parent;

    ShardDataModificationCursor(final ShardDataModification root, final InmemoryDOMDataTreeShardWriteTransaction parent) {
        stack.push(root.createOperation(null));
        this.parent = Preconditions.checkNotNull(parent);
    }

    @Override
    public void enter(final PathArgument child) {
        WriteCursorStrategy nextOp = getCurrent().enter(child);
        stack.push(nextOp);
    }

    private WriteCursorStrategy getCurrent() {
        return stack.peek();
    }

    @Override
    public void enter(final PathArgument... path) {
        for (PathArgument pathArgument : path) {
            enter(pathArgument);
        }
    }

    @Override
    public void enter(final Iterable<PathArgument> path) {
        for (PathArgument pathArgument : path) {
            enter(pathArgument);
        }
    }

    @Override
    public void exit() {
        WriteCursorStrategy op = stack.pop();
        op.exit();
    }

    @Override
    public void exit(final int depth) {
        for (int i = 0; i < depth; i++) {
            exit();
        }
    }

    @Override
    public void close() {
        parent.cursorClosed();
    }

    @Override
    public void delete(final PathArgument child) {
        getCurrent().delete(child);
    }

    @Override
    public void merge(final PathArgument child, final NormalizedNode<?, ?> data) {
        getCurrent().merge(child, data);
    }

    @Override
    public void write(final PathArgument child, final NormalizedNode<?, ?> data) {
        getCurrent().write(child, data);
    }

}