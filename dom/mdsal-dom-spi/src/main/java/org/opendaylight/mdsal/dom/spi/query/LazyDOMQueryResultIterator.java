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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
final class LazyDOMQueryResultIterator implements Iterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
    // Current data item
    private final ArrayDeque<NormalizedNode<?, ?>> currentData = new ArrayDeque<>();
    // Absolute path from root of current data item
    private final ArrayDeque<PathArgument> currentPath;
    // Steps remaining in the select part of the query
    private final ArrayDeque<PathArgument> selectSteps;
    // The query which needs to be executed
    private final DOMQuery query;

    // FIXME: MDSAL-610: this needs to be eliminated
    private final Iterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> iter;

    LazyDOMQueryResultIterator(final DOMQuery query, final NormalizedNode<?, ?> queryRoot) {
        this.query = requireNonNull(query);
        currentPath = new ArrayDeque<>(query.getRoot().getPathArguments());
        selectSteps = new ArrayDeque<>(query.getSelect().getPathArguments());
        currentData.push(queryRoot);

        // FIXME: MDSAL-610: this is a recursive algo, filling provided list. Turn it around into a state mutator.
        final List<Entry<YangInstanceIdentifier, NormalizedNode<?,?>>> result = new ArrayList<>();
        evalPath(result);
        this.iter = result.iterator();
        currentPath.clear();
        selectSteps.clear();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> next() {
        return iter.next();
    }

    private void evalPath(final List<Entry<YangInstanceIdentifier, NormalizedNode<?,?>>> result) {
        final NormalizedNode<?, ?> data = currentData.pop();
        final PathArgument next = selectSteps.poll();
        if (next == null) {
            if (matches(data, query)) {
                result.add(new SimpleImmutableEntry<>(YangInstanceIdentifier.create(currentPath), data));
            }
            return;
        }

        if (data instanceof MapNode && next instanceof NodeIdentifier) {
            checkArgument(data.getIdentifier().equals(next), "Unexpected step %s", next);
            for (MapEntryNode child : ((MapNode) data).getValue()) {
                evalChild(result, child);
            }
        } else {
            NormalizedNodes.getDirectChild(data, next).ifPresent(child -> evalChild(result, child));
        }
        selectSteps.push(next);
    }

    private void evalChild(final List<Entry<YangInstanceIdentifier, NormalizedNode<?,?>>> result,
            final NormalizedNode<?, ?> child) {
        currentPath.addLast(child.getIdentifier());
        currentData.push(child);
        evalPath(result);
        currentPath.removeLast();
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
