/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

@Beta
public abstract class AbstractShardModificationFactoryBuilder<T> extends ModificationContextNodeBuilder
        implements Builder<T> {
    protected final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> childShards = new HashMap<>();
    protected final DOMDataTreeIdentifier root;

    public AbstractShardModificationFactoryBuilder(final DOMDataTreeIdentifier root) {
        this.root = requireNonNull(root);
    }

    public void addSubshard(final ForeignShardModificationContext value) {
        WriteableSubshardBoundaryNode leafNode = WriteableSubshardBoundaryNode.from(value);
        putNode(value.getIdentifier().getRootIdentifier(), leafNode);
    }

    public void addSubshard(final DOMDataTreeIdentifier prefix, final ForeignShardModificationContext value) {
        childShards.put(prefix, value);
    }

    @Override
    public abstract T build();

    private void putNode(final YangInstanceIdentifier key, final WriteableSubshardBoundaryNode subshardNode) {
        final Iterator<PathArgument> toBoundary = toRelative(key).getPathArguments().iterator();
        if (toBoundary.hasNext()) {
            ModificationContextNodeBuilder current = this;
            while (true) {
                final PathArgument nextArg = toBoundary.next();
                if (!toBoundary.hasNext()) {
                    current.addBoundary(nextArg, subshardNode);
                    break;
                }

                current = current.getInterior(nextArg);
            }
        }
    }

    private YangInstanceIdentifier toRelative(final YangInstanceIdentifier key) {
        return key.relativeTo(root.getRootIdentifier()).get();
    }
}
