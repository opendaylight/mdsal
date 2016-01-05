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
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

class DataTreeModificationCursorAdaptor implements DOMDataTreeWriteCursor {

    private final DataTreeModificationCursor delegate;

    private DataTreeModificationCursorAdaptor(DataTreeModificationCursor delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    public static DataTreeModificationCursorAdaptor of(DataTreeModificationCursor dataTreeCursor) {
        return new DataTreeModificationCursorAdaptor(dataTreeCursor);
    }

    @Override
    public void delete(PathArgument child) {
        delegate.delete(child);
    }

    @Override
    public void enter(PathArgument child) {
        delegate.enter(child);
    }

    @Override
    public void merge(PathArgument child, NormalizedNode<?, ?> data) {
        delegate.merge(child, data);
    }

    @Override
    public void write(PathArgument child, NormalizedNode<?, ?> data) {
        delegate.write(child, data);
    }

    @Override
    public void enter(Iterable<PathArgument> path) {
        delegate.enter(path);
    }

    @Override
    public void exit() {
        delegate.exit();
    }

    @Override
    public void exit(int depth) {
        delegate.exit(depth);
    }

    public Optional<NormalizedNode<?, ?>> readNode(PathArgument child) {
        return delegate.readNode(child);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void enter(PathArgument... path) {
        delegate.enter(path);
    }
}
