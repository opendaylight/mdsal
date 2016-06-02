/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingObject;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

final class DataTreeModificationCursorAdaptor extends ForwardingObject implements DOMDataTreeWriteCursor {

    private final DataTreeModificationCursor delegate;
    private boolean closed = false;

    private DataTreeModificationCursorAdaptor(final DataTreeModificationCursor delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    static DataTreeModificationCursorAdaptor of(final DataTreeModificationCursor dataTreeCursor) {
        return new DataTreeModificationCursorAdaptor(dataTreeCursor);
    }

    protected final DataTreeModificationCursor delegate() {
        return delegate;
    }

    @Override
    public void delete(final PathArgument child) {
        delegate.delete(child);
    }

    @Override
    public void enter(final PathArgument child) {
        delegate.enter(child);
    }

    @Override
    public void merge(final PathArgument child, final NormalizedNode<?, ?> data) {
        delegate.merge(child, data);
    }

    @Override
    public void write(final PathArgument child, final NormalizedNode<?, ?> data) {
        delegate.write(child, data);
    }

    @Override
    public void enter(final Iterable<PathArgument> path) {
        delegate.enter(path);
    }

    @Override
    public void exit() {
        delegate.exit();
    }

    @Override
    public void exit(final int depth) {
        delegate.exit(depth);
    }

    public Optional<NormalizedNode<?, ?>> readNode(final PathArgument child) {
        return delegate.readNode(child);
    }

    @Override
    public void close() {
        delegate.close();
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void enter(final PathArgument... path) {
        delegate.enter(path);
    }
}
