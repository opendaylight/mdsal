/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.spi.shard.ForeignShardModificationContext;
import org.opendaylight.mdsal.dom.spi.shard.WritableNodeOperation;
import org.opendaylight.mdsal.dom.spi.shard.WriteCursorStrategy;
import org.opendaylight.mdsal.dom.spi.shard.WriteableModificationNode;
import org.opendaylight.mdsal.dom.spi.shard.WriteableNodeWithSubshard;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;

public final class ShardDataModification extends WriteableNodeWithSubshard {

    private final ShardRootModificationContext rootContext;
    private final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> childShards;

    ShardDataModification(final ShardRootModificationContext boundary,
                          final Map<PathArgument, WriteableModificationNode> subshards,
                          final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> childShards) {
        super(subshards);
        this.rootContext = requireNonNull(boundary);
        this.childShards = ImmutableMap.copyOf(childShards);
    }

    @Override
    public WriteCursorStrategy createOperation(final DOMDataTreeWriteCursor parentCursor) {
        return new WritableNodeOperation(this, rootContext.cursor()) {
            @Override
            public void exit() {
                throw new IllegalStateException("Can not exit data tree root");
            }
        };
    }

    @Override
    public PathArgument getIdentifier() {
        return rootContext.getIdentifier().getRootIdentifier().getLastPathArgument();
    }

    DOMDataTreeIdentifier getPrefix() {
        return rootContext.getIdentifier();
    }

    Map<DOMDataTreeIdentifier, ForeignShardModificationContext> getChildShards() {
        return childShards;
    }

    DataTreeModification seal() {
        final DataTreeModification rootModification = rootContext.ready();
        for (ForeignShardModificationContext childShard : childShards.values()) {
            childShard.ready();
        }

        return rootModification;
    }

    void closeTransactions() {
        for (final ForeignShardModificationContext childShard : childShards.values()) {
            childShard.closeForeignTransaction();
        }
    }

    void closeCursor() {
        rootContext.closeCursor();
    }
}