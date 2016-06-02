/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;

final class ShardDataModification extends WriteableNodeWithSubshard {

    private final ShardRootModificationContext rootContext;
    private final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> childShards;

    ShardDataModification(ShardRootModificationContext boundary,
            Map<PathArgument, WriteableModificationNode> subshards,
            Map<DOMDataTreeIdentifier, ForeignShardModificationContext> childShards) {
        super(subshards);
        this.rootContext = Preconditions.checkNotNull(boundary);
        this.childShards = ImmutableMap.copyOf(childShards);
    }

    @Override
    WriteCursorStrategy createOperation(DOMDataTreeWriteCursor parentCursor) {
        return new WriteableNodeOperation(this, rootContext.cursor()) {
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

    static ShardDataModification from(ShardRootModificationContext root,
            Map<YangInstanceIdentifier, ForeignShardModificationContext> shards) {

        ShardDataModificationBuilder builder = new ShardDataModificationBuilder(root);
        for (Entry<YangInstanceIdentifier, ForeignShardModificationContext> subshard : shards.entrySet()) {
            builder.addSubshard(subshard.getValue());
        }
        return builder.build();
    }

    public DOMDataTreeIdentifier getPrefix() {
        return rootContext.getIdentifier();
    }

    public Map<DOMDataTreeIdentifier, ForeignShardModificationContext> getChildShards() {
        return childShards;
    }

    public DataTreeModification seal() {
        final DataTreeModification rootModification = rootContext.ready();
        for (ForeignShardModificationContext childShard : childShards.values()) {
            childShard.ready();
        }

        return rootModification;
    }

    public void closeTransactions() {
        for (final ForeignShardModificationContext childShard : childShards.values()) {
            childShard.closeForeignTransaction();
        }
    }

}