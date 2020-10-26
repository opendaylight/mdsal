/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

/**
 * Generalized utility for matching predicates. Split out of {@link LazyDOMQueryResultIterator} for simplicity.
 */
final class DOMPredicateMatcher {
    DOMPredicateMatcher() {
        // Utility class
    }

    static boolean matches(final NormalizedNode<?, ?> data, final List<? extends DOMQueryPredicate> predicates) {
        // TODO: it would be nice if predicates were somehow structured -- can we perhaps sort them by their
        //       InstanceIdentifier? If the predicates are sharing a common subpath. Hence if we can guarantee
        //       predicates are in a certain order, we would not end up in subsequent re-lookups of the same node.
        for (DOMQueryPredicate pred : predicates) {
            if (!matches(pred, data)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(final DOMQueryPredicate pred, final NormalizedNode<?, ?> data) {
        // So now, dealing with implementations: YangInstanceIdentifier.getLastPathArgument() is always cheap.
        // If its parent is YangInstanceIdentifier.ROOT (i.e. isEmpty() == true), we are dealing with a last-step
        // lookup -- in which case we forgo iteration:
        final YangInstanceIdentifier path = pred.getPath();
        return path.coerceParent().isEmpty() ? matchesChild(pred, data, path.getLastPathArgument())
            : matchesAnyChild(pred, data, new ArrayDeque<>(path.getPathArguments()));
    }

    private static boolean matchesChild(final DOMQueryPredicate pred, final NormalizedNode<?, ?> data,
            final PathArgument pathArg) {
        // Try the direct approach...
        final Optional<NormalizedNode<?, ?>> direct = NormalizedNodes.getDirectChild(data, pathArg);
        if (direct.isPresent()) {
            return pred.test(direct.orElseThrow());
        }

        // We may be dealing with a wildcard here. NodeIdentifier is a final class, hence this is as fast as it gets.
        if (pathArg instanceof NodeIdentifier && data instanceof MapNode) {
            for (MapEntryNode child : ((MapNode) data).getValue()) {
                if (pred.test(data)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean matchesAnyChild(final DOMQueryPredicate pred, final NormalizedNode<?, ?> data,
            final Deque<PathArgument> pathArgs) {
        // Guaranteed to have at least one item
        final PathArgument pathArg = pathArgs.pop();
        // Ultimate item -- reuse lookup & match
        if (pathArgs.isEmpty()) {
            pathArgs.push(pathArg);
            return matchesChild(pred, data, pathArg);
        }

        final Optional<NormalizedNode<?, ?>> direct = NormalizedNodes.getDirectChild(data, pathArg);
        if (direct.isPresent()) {
            final boolean ret = matchesAnyChild(pred, direct.orElseThrow(), pathArgs);
            pathArgs.push(pathArg);
            return ret;
        }

        // We may be dealing with a wildcard here. NodeIdentifier is a final class, hence this is as fast as it gets.
        if (pathArg instanceof NodeIdentifier && data instanceof MapNode) {
            for (MapEntryNode child : ((MapNode) data).getValue()) {
                if (matchesAnyChild(pred, child, pathArgs)) {
                    pathArgs.push(pathArg);
                    return true;
                }
            }
        }

        pathArgs.push(pathArg);
        return false;
    }
}
