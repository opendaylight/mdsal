/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.mdsal.dom.spi.RegistrationTreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class ShardedDOMDataTreeListenerRegistrationTree
        extends AbstractRegistrationTree<ShardedDOMDataTreeListenerRegistration<?>> {
    private final DOMDataTreeListenerRegistry localRegistry;
    private final DOMDataTreeIdentifier prefix;
    private SchemaContext schemaContext;

    ShardedDOMDataTreeListenerRegistrationTree(final DOMDataTreeIdentifier prefix,
            final DOMDataTreeListenerRegistry localRegistry) {
        this.prefix = requireNonNull(prefix);
        this.localRegistry = requireNonNull(localRegistry);
    }

    void addChild(final DOMDataTreeIdentifier childPrefix, final DOMDataTreeShard child) {
        final com.google.common.base.Optional<YangInstanceIdentifier> pathOpt = childPrefix
                .getRootIdentifier().relativeTo(prefix.getRootIdentifier());
        checkArgument(pathOpt.isPresent(), "Registered child {} prefix {} does not belong under {}", child,
            childPrefix, prefix);
        final Collection<PathArgument> path = pathOpt.get().getPathArguments();

        takeLock();
        try {
            updateListeners(rootNode(), path.iterator(), child);
        } finally {
            releaseLock();
        }
    }

    void updateSchemaContext(final SchemaContext schemaContext) {
        takeLock();
        try {
            this.schemaContext = requireNonNull(schemaContext);
            updateSchemaContext(rootNode(), schemaContext);
        } finally {
            releaseLock();
        }
    }

    <T extends DOMDataTreeListener> ShardedDOMDataTreeListenerRegistration<T> createListener(final T userListener,
            final Collection<DOMDataTreeIdentifier> localSubtrees, final boolean allowRxMerges) {
        final Collection<Collection<PathArgument>> paths = subtreesToPaths(localSubtrees);

        takeLock();
        try {
            // Instantiate and start the listener
            final ShardedDOMDataTreeListenerRegistration<T> ret = new ShardedDOMDataTreeListenerRegistration<>(
                    localRegistry, schemaContext, userListener, localSubtrees, allowRxMerges);

            // Register it with all the nodes it belongs to
            for (Collection<PathArgument> path : paths) {
                addRegistration(findNodeFor(path), ret);
            }

            return ret;
        } finally {
            releaseLock();
        }
    }

    private void updateListeners(final RegistrationTreeNode<ShardedDOMDataTreeListenerRegistration<?>> root,
            final Iterator<PathArgument> path, final DOMDataTreeShard shard) {
        // FIXME: walk all registered listeners, looking for

    }

    private RegistrationTreeNode<ShardedDOMDataTreeListenerRegistration<?>> rootNode() {
        return findNodeFor(ImmutableList.of());
    }

    private static void updateSchemaContext(final RegistrationTreeNode<ShardedDOMDataTreeListenerRegistration<?>> node,
            final SchemaContext schemaContext) {
        node.getRegistrations().forEach(reg -> reg.updateSchemaContext(schemaContext));
        node.forEachChild(child -> updateSchemaContext(child, schemaContext));
    }

    private Collection<Collection<PathArgument>> subtreesToPaths(final Collection<DOMDataTreeIdentifier> subtrees) {
        return subtrees.stream().map(treeId -> {
            final com.google.common.base.Optional<YangInstanceIdentifier> optPath = treeId.getRootIdentifier()
                    .relativeTo(prefix.getRootIdentifier());
            checkArgument(optPath.isPresent(), "Requested subtree %s is not a subtree of %s", treeId, prefix);
            return optPath.get().getPathArguments();
        }).collect(Collectors.toList());
    }
}