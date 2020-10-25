/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
public final class DOMQueryEvaluator {
    private DOMQueryEvaluator() {

    }

    /**
     * Evaluate {@link DOMQuery} on its data element. The element is expected to correspond to
     * {@link DOMQuery#getRoot()}.
     *
     * @param query Query to execute
     * @param queryRoot Query root object
     * @return Result of evaluation
     * @throws NullPointerException if any argument is null
     */
    public static List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> evaluateOn(final DOMQuery query,
            final NormalizedNode<?, ?> queryRoot) {
        final YangInstanceIdentifier path = query.getSelect();
        return path.isEmpty() ? evalSingle(queryRoot, query)
            : evalPath(new ArrayDeque<>(path.getPathArguments()), queryRoot, query);
    }

    /**
     * Evaluate {@link DOMQuery} on a conceptual root. The element is expected to correspond to the conceptual data tree
     * root. This method will first find the {@link DOMQuery#getRoot()} and then defer to
     * {@link #evaluateOn(DOMQuery, NormalizedNode)}.
     *
     * @param query Query to execute
     * @param root Conceptual root object
     * @return Result of evaluation
     * @throws NullPointerException if any argument is null
     */
    public static List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> evaluateOnRoot(
            final DOMQuery query, final NormalizedNode<?, ?> root) {
        NormalizedNode<?, ?> evalRoot = root;
        for (PathArgument arg : query.getRoot().getPathArguments()) {
            final Optional<NormalizedNode<?, ?>> next = NormalizedNodes.findNode(root, arg);
            if (next.isEmpty()) {
                return ImmutableList.of();
            }
            evalRoot = next.orElseThrow();
        }
        return evaluateOn(query, evalRoot);
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
                result.add(new SimpleImmutableEntry<>(path, data));
            }
            return;
        }

        if (data instanceof MapNode && next instanceof NodeIdentifier) {
            checkArgument(data.getIdentifier().equals(next), "Unexpected step %s", next);
            for (MapEntryNode child : ((MapNode) data).getValue()) {
                evalPath(result, path.node(child.getIdentifier()), remaining, child, query);
            }
        } else {
            NormalizedNodes.getDirectChild(data, next).ifPresent(
                child -> evalPath(result, path.node(next), remaining, child, query));
        }
        remaining.push(next);
    }

    private static List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> evalSingle(
            final NormalizedNode<?, ?> data, final DOMQuery query) {
        return matches(data, query) ? ImmutableList.of()
                : ImmutableList.of(new SimpleImmutableEntry<>(query.getRoot(), data));
    }

    private static boolean matches(final NormalizedNode<?, ?> data, final DOMQuery query) {
        for (DOMQueryPredicate pred : query.getPredicates()) {
            if (!pred.test(NormalizedNodes.findNode(data, pred.getPath()).orElse(null))) {
                return false;
            }
        }
        return true;
    }
}
