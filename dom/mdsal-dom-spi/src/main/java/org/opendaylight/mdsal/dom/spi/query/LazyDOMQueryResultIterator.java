/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

@NonNullByDefault
final class LazyDOMQueryResultIterator extends AbstractIterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
    // Absolute path from root of current data item
    private final ArrayDeque<PathArgument> currentPath;
    // Steps remaining in the select part of the query
    private final ArrayDeque<PathArgument> selectSteps;

    // FIXME: This needs to be a stack of sorts
    private final NormalizedNode<?, ?> queryRoot;

    LazyDOMQueryResultIterator(final DOMQuery query, final NormalizedNode<?, ?> queryRoot) {
        currentPath = new ArrayDeque<>(query.getRoot().getPathArguments());
        selectSteps = new ArrayDeque<>(query.getSelect().getPathArguments());

        this.queryRoot = requireNonNull(queryRoot);
    }

    @Override
    protected Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> computeNext() {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> nextEntry = findNextEntry();
        return nextEntry != null ? nextEntry : endOfData();
    }

    private @Nullable Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> findNextEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    // FIXME: this is a recursive algo, filling provided list. Turn it around into a state mutator.

    private static void evalPath(final List<Entry<YangInstanceIdentifier, NormalizedNode<?,?>>> result,
            final Deque<PathArgument> path, final ArrayDeque<PathArgument> remaining,
            final NormalizedNode<?, ?> data, final DOMQuery query) {
        final PathArgument next = remaining.poll();
        if (next == null) {
            if (matches(data, query)) {
                result.add(new SimpleImmutableEntry<>(YangInstanceIdentifier.create(path), data));
            }
            return;
        }

        if (data instanceof MapNode && next instanceof NodeIdentifier) {
            checkArgument(data.getIdentifier().equals(next), "Unexpected step %s", next);
            for (MapEntryNode child : ((MapNode) data).getValue()) {
                evalChild(result, path, remaining, query, child);
            }
        } else {
            NormalizedNodes.getDirectChild(data, next).ifPresent(
                child -> evalChild(result, path, remaining, query, child));
        }
        remaining.push(next);
    }

    private static void evalChild(final List<Entry<YangInstanceIdentifier, NormalizedNode<?,?>>> result,
            final Deque<PathArgument> path, final ArrayDeque<PathArgument> remaining, final DOMQuery query,
            final NormalizedNode<?, ?> child) {
        path.addLast(child.getIdentifier());
        evalPath(result, path, remaining, child, query);
        path.removeLast();
    }

    static boolean matches(final NormalizedNode<?, ?> data, final DOMQuery query) {
        for (DOMQueryPredicate pred : query.getPredicates()) {
            // Okay, now we need to deal with predicates, but do it in a smart fashion, so we do not end up iterating
            // all over the place. Typically we will be matching just a leaf.
            final YangInstanceIdentifier path = pred.getPath();
            final Optional<NormalizedNode<?, ?>> node;
            if (path.coerceParent().isEmpty()) {
                node = NormalizedNodes.getDirectChild(data, path.getLastPathArgument());
            } else {
                node = NormalizedNodes.findNode(data, path);
            }

            if (!pred.test(node.orElse(null))) {
                return false;
            }
        }
        return true;
    }
}
