/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import com.google.common.collect.ImmutableList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

public final class DOMQueryEvaluator {
    private DOMQueryEvaluator() {

    }

    public static List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> evaluate(final DOMQuery query,
            final NormalizedNode<?, ?> root) {
        final YangInstanceIdentifier path = query.getSelect();
        return path.isEmpty() ? evalSingle(root, query)
                : evalPath(new ArrayDeque<>(path.getPathArguments()), root, query);
    }

    private static List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> evalPath(
            final ArrayDeque<PathArgument> remaining, final NormalizedNode<?,?> data, final DOMQuery query) {
        final List<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> result = new ArrayList<>();
        evalPath(result, query.getRoot(), remaining, data, query);
        return result;
    }

    private static void evalPath(final List<Entry<YangInstanceIdentifier, NormalizedNode<?,?>>> result,
            final YangInstanceIdentifier path, final ArrayDeque<PathArgument> remaining,
            final NormalizedNode<?, ?> data, final DOMQuery query) {
        final PathArgument next = remaining.poll();
        if (next == null) {
            if (matches(data, query)) {
                result.add(new SimpleImmutableEntry<>(query.getRoot(), data));
            }
            return;
        }

        // TODO: this is probably insufficient
        NormalizedNodes.findNode(data, next)
            .ifPresent(child -> evalPath(result, path.node(next), remaining, child, query));
        remaining.push(next);
    }

    private static List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> evalSingle(
            final NormalizedNode<?, ?> data, final DOMQuery query) {
        return matches(data, query) ? ImmutableList.of()
                : ImmutableList.of(new SimpleImmutableEntry<>(query.getRoot(), data));
    }

    private static boolean matches(final NormalizedNode<?, ?> data, final DOMQuery query) {
        for (DOMQueryPredicate pred : query.getPredicates()) {
            if (!pred.test(data)) {
                return false;
            }
        }
        return true;
    }
}
