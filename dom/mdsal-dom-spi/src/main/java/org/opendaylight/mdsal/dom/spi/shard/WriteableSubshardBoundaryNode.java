/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Writable node that is located at a boundary to a subshard.
 */
@Beta
public final class WriteableSubshardBoundaryNode extends WriteableModificationNode {

    private final ForeignShardModificationContext boundary;

    private WriteableSubshardBoundaryNode(final ForeignShardModificationContext boundary) {
        this.boundary = Preconditions.checkNotNull(boundary);
    }

    public static WriteableSubshardBoundaryNode from(final ForeignShardModificationContext value) {
        return new WriteableSubshardBoundaryNode(value);
    }

    @Override
    public PathArgument getIdentifier() {
        return boundary.getIdentifier().getRootIdentifier().getLastPathArgument();
    }

    @Override
    public WriteCursorStrategy createOperation(final DOMDataTreeWriteCursor parentCursor) {
        return new DelegatingWriteCursorStrategy() {
            @Override
            public void exit() {
                parentCursor.exit();
            }

            @Override
            protected DelegatingWriteCursorStrategy childStrategy() {
                return new DelegatingWriteCursorStrategy() {
                    @Override
                    protected DOMDataTreeWriteCursor delegate() {
                        return boundary.getCursor();
                    }
                };
            }

            @Override
            protected DOMDataTreeWriteCursor delegate() {
                return boundary.getCursor();
            }
        };
    }

    @Override
    public WriteableModificationNode getChild(final PathArgument node) {
        // Another level of nesting should be taken care of by underlying cursor.
        return null;
    }

    @Override
    public void markDeleted() {
        // FIXME: Should we delete all data or disconnect?
    }

    @Override
    public Map<PathArgument, WriteableModificationNode> getChildrenWithSubshards() {
        return ImmutableMap.of();
    }
}
