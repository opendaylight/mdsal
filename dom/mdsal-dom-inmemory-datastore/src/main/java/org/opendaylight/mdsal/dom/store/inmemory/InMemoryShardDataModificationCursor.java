/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.dom.spi.shard.AbstractDataModificationCursor;
import org.opendaylight.mdsal.dom.spi.shard.WriteCursorStrategy;

class InMemoryShardDataModificationCursor extends AbstractDataModificationCursor<ShardDataModification> {

    private InmemoryDOMDataTreeShardWriteTransaction parent;

    InMemoryShardDataModificationCursor(final ShardDataModification root,
                                        final InmemoryDOMDataTreeShardWriteTransaction parent) {
        super(root);
        this.parent = requireNonNull(parent);
    }

    @Override
    protected WriteCursorStrategy getRootOperation(final ShardDataModification root) {
        return root.createOperation(null);
    }

    @Override
    public void close() {
        parent.cursorClosed();
    }

}