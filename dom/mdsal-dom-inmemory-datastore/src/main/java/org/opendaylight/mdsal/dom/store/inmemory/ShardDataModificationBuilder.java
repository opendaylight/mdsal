/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

class ShardDataModificationBuilder extends ModificationContextNodeBuilder<ShardDataModification> {

    private final ShardRootModificationContext root;
    private final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> childShards =
            new HashMap<>();

    public ShardDataModificationBuilder(final ShardRootModificationContext root) {
        this.root = root;
    }

    public void addSubshard(final ForeignShardModificationContext value) {
        WriteableSubshardBoundaryNode leafNode = WriteableSubshardBoundaryNode.from(value);
        putNode(value.getIdentifier().getRootIdentifier(), leafNode);
    }

    public void addSubshard(final DOMDataTreeIdentifier prefix, final ForeignShardModificationContext value) {
        childShards.put(prefix, value);
    }

    private void putNode(final YangInstanceIdentifier key, final WriteableSubshardBoundaryNode subshardNode) {
        ModificationContextNodeBuilder<?> current = this;
        Iterator<PathArgument> toBoundary = toRelative(key).getPathArguments().iterator();
        while (toBoundary.hasNext()) {
            PathArgument nextArg = toBoundary.next();
            if (toBoundary.hasNext()) {
                current = getInterior(nextArg);
            } else {
                current.addBoundary(nextArg, subshardNode);
            }
        }
    }


    @Override
    ShardDataModification build(final Map<PathArgument, WriteableModificationNode> children) {
        return new ShardDataModification(root, children, childShards);
    }

    private YangInstanceIdentifier toRelative(final YangInstanceIdentifier key) {
        return key.relativeTo(root.getIdentifier().getRootIdentifier()).get();
    }
}