/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Writable node that can have subshard children somewhere on lower level.
 */
public abstract class WriteableModificationNode implements Identifiable<PathArgument> {

    /**
     * Gets child which is on path towards subshard.
     *
     * @return null if requested child is not subshard or enclosing node of any subshard.
     */
    public abstract @Nullable WriteableModificationNode getChild(@NonNull PathArgument node);

    public abstract Map<PathArgument, WriteableModificationNode> getChildrenWithSubshards();

    /**
     * Creates operation used to modify this node and its children.
     *
     * @param parentCursor Cursor associated with parent shard
     * @return WriteableOperation for this node.
     */
    public abstract WriteCursorStrategy createOperation(DOMDataTreeWriteCursor parentCursor);

    public abstract void markDeleted();

}
