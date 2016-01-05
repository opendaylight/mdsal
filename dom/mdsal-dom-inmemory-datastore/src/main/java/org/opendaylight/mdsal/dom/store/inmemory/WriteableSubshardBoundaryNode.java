/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class WriteableSubshardBoundaryNode extends WriteableModificationNode {

    private final ForeignShardModificationContext boundary;

    private WriteableSubshardBoundaryNode(ForeignShardModificationContext boundary) {
        this.boundary = Preconditions.checkNotNull(boundary);
    }

    public static WriteableSubshardBoundaryNode from(ForeignShardModificationContext value) {
        return new WriteableSubshardBoundaryNode(value);
    }

    @Override
    public PathArgument getIdentifier() {
        return boundary.getIdentifier().getRootIdentifier().getLastPathArgument();
    }

    @Override
    WriteCursorStrategy createOperation(final DOMDataTreeWriteCursor parentCursor) {
        return new WriteCursorDelegatingStrategy(boundary.getCursor()) {

            @Override
            public void exit() {
                parentCursor.exit();
            }

            @Override
            protected WriteCursorDelegatingStrategy childStrategy() {
                return new WriteCursorDelegatingStrategy(getDelegate());
            }

        };
    }

    @Override
    WriteableModificationNode getChild(PathArgument node) {
        // Another level of nesting should be taken care of by underlaying
        // cursor.
        return null;
    }

    @Override
    void markDeleted() {
        // FIXME: Should we delete all data or disconnect?
    }

    @Override
    Map<PathArgument, WriteableModificationNode> getChildrenWithSubshards() {
        return Collections.emptyMap();
    }

}