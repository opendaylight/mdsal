/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

abstract class WriteableNodeWithSubshard extends WriteableModificationNode {

    private final Map<PathArgument, WriteableModificationNode> children;

    WriteableNodeWithSubshard(final Map<PathArgument, WriteableModificationNode> children) {
        this.children = ImmutableMap.copyOf(children);
    }

    @Override
    Map<PathArgument, WriteableModificationNode> getChildrenWithSubshards() {
        return children;
    }

    @Override
    WriteableModificationNode getChild(final PathArgument node) {
        return children.get(node);
    }

    @Override
    void markDeleted() {
        for (WriteableModificationNode child : children.values()) {
            child.markDeleted();
        }
    }
}