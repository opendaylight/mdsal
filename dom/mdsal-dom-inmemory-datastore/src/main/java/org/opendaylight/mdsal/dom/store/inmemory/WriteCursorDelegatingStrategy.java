/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

class WriteCursorDelegatingStrategy implements WriteCursorStrategy {

    private final DOMDataTreeWriteCursor delegate;

    WriteCursorDelegatingStrategy(DOMDataTreeWriteCursor cursor) {
        this.delegate = cursor;
    }

    protected final DOMDataTreeWriteCursor getDelegate() {
        return delegate;
    }

    @Override
    public WriteCursorStrategy enter(PathArgument arg) {
        delegate.enter(arg);
        return childStrategy();
    }

    @Override
    final public void delete(PathArgument arg) {
        delegate.delete(arg);
    }

    @Override
    final public void merge(PathArgument arg, NormalizedNode<?, ?> data) {
        delegate.merge(arg, data);
    }

    @Override
    final public void write(PathArgument arg, NormalizedNode<?, ?> data) {
        delegate.write(arg, data);
    }

    @Override
    final public void mergeToCurrent(NormalizedNodeContainer<?, ?, ?> data) {
        for (NormalizedNode<?, ?> child : data.getValue()) {
            delegate.merge(child.getIdentifier(), child);
        }
    }

    @Override
    final public void writeToCurrent(NormalizedNodeContainer<?, ?, ?> data) {
        for (NormalizedNode<?, ?> child : data.getValue()) {
            delegate.write(child.getIdentifier(), child);
        }
    }

    /**
     * Operation performed to exit current logical level, default implementation calls
     * {@link DOMDataTreeWriteCursor#exit()} on underlaying cursor.
     *
     * Subclasses may override this to customize exit strategy.
     *
     */
    @Override
    public void exit() {
        delegate.exit();
    }

    /**
     *
     * Returns strategy to be used on child nodes. Default implementation is to reuse same instance.
     *
     * @return Strategy to be used for child nodes.
     */
    protected WriteCursorDelegatingStrategy childStrategy() {
        return this;
    }

}