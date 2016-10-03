/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * Delegating implementation of a {@link WriteCursorStrategy}.
 */
@Beta
public abstract class DelegatingWriteCursorStrategy extends ForwardingObject implements WriteCursorStrategy {

    @Override
    protected abstract DOMDataTreeWriteCursor delegate();

    /**
     * Returns strategy to be used on child nodes. Default implementation is to reuse same instance.
     *
     * @return Strategy to be used for child nodes.
     */
    protected DelegatingWriteCursorStrategy childStrategy() {
        return this;
    }

    @Override
    public WriteCursorStrategy enter(final PathArgument arg) {
        delegate().enter(arg);
        return childStrategy();
    }

    @Override
    public void delete(final PathArgument arg) {
        delegate().delete(arg);
    }

    @Override
    public void merge(final PathArgument arg, final NormalizedNode<?, ?> data) {
        delegate().merge(arg, data);
    }

    @Override
    public void write(final PathArgument arg, final NormalizedNode<?, ?> data) {
        delegate().write(arg, data);
    }

    @Override
    public void mergeToCurrent(final NormalizedNodeContainer<?, ?, ?> data) {
        for (NormalizedNode<?, ?> child : data.getValue()) {
            delegate().merge(child.getIdentifier(), child);
        }
    }

    @Override
    public void writeToCurrent(final NormalizedNodeContainer<?, ?, ?> data) {
        for (NormalizedNode<?, ?> child : data.getValue()) {
            delegate().write(child.getIdentifier(), child);
        }
    }

    /**
     * Operation performed to exit current logical level, default implementation calls
     * {@link DOMDataTreeWriteCursor#exit()} on underlaying cursor.
     *
     *<p>
     * Subclasses may override this to customize exit strategy.
     *
     */
    @Override
    public void exit() {
        delegate().exit();
    }
}
