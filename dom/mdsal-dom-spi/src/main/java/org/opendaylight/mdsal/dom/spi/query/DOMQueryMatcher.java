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
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

/**
 * Generalized utility for matching predicates. Split out of {@link DOMQueryIterator} for simplicity.
 */
final class DOMQueryMatcher {
    private DOMQueryMatcher() {
        // Utility class
    }

    static boolean matchesAll(final NormalizedNode data, final List<? extends DOMQueryPredicate> predicates) {
        // TODO: it would be nice if predicates were somehow structured -- can we perhaps sort them by their
        //       InstanceIdentifier? If the predicates are sharing a common subpath. Hence if we can guarantee
        //       predicates are in a certain order, we would not end up in subsequent re-lookups of the same node.
        Deque<PathArgument> pathArgs = null;
        for (DOMQueryPredicate pred : predicates) {
            // So now, dealing with implementations: YangInstanceIdentifier.getLastPathArgument() is always cheap.
            // If its parent is YangInstanceIdentifier.ROOT (i.e. isEmpty() == true), we are dealing with a last-step
            // lookup -- in which case we forgo iteration:
            final YangInstanceIdentifier path = pred.relativePath();
            if (path.coerceParent().isEmpty()) {
                if (!matchesChild(pred.match(), data, path.getLastPathArgument())) {
                    return false;
                }
                continue;
            }

            // We are leaking path arguments in a bid for object reuse: we end up reusing same object as needed
            if (pathArgs == null) {
                pathArgs = new ArrayDeque<>();
            }
            pathArgs.addAll(path.getPathArguments());

            // The stage is set, we now have to deal with potential negation.
            if (!matchesAny(pred.match(), data, pathArgs)) {
                return false;
            }

            pathArgs.clear();
        }
        return true;
    }

    private static boolean matchesAny(final Match match, final NormalizedNode data,
            final Deque<PathArgument> pathArgs) {
        // Guaranteed to have at least one item
        final PathArgument pathArg = pathArgs.pop();
        // Ultimate item -- reuse lookup & match
        if (pathArgs.isEmpty()) {
            pathArgs.push(pathArg);
            return matchesChild(match, data, pathArg);
        }

        final Optional<NormalizedNode> direct = NormalizedNodes.getDirectChild(data, pathArg);
        if (direct.isPresent()) {
            final boolean ret = matchesAny(match, direct.orElseThrow(), pathArgs);
            pathArgs.push(pathArg);
            return ret;
        }

        // We may be dealing with a wildcard here. NodeIdentifier is a final class, hence this is as fast as it gets.
        if (pathArg instanceof NodeIdentifier && data instanceof MapNode map) {
            for (MapEntryNode child : map.body()) {
                if (matchesAny(match, child, pathArgs)) {
                    pathArgs.push(pathArg);
                    return true;
                }
            }
        }

        pathArgs.push(pathArg);
        return false;
    }

    private static boolean matchesChild(final Match match, final NormalizedNode data, final PathArgument pathArg) {
        // Try the direct approach...
        final Optional<NormalizedNode> direct = NormalizedNodes.getDirectChild(data, pathArg);
        if (direct.isPresent()) {
            return match.test(direct.orElseThrow());
        }

        // We may be dealing with a wildcard here. NodeIdentifier is a final class, hence this is as fast as it gets.
        if (pathArg instanceof NodeIdentifier && data instanceof MapNode map) {
            for (MapEntryNode child : map.body()) {
                if (match.test(child)) {
                    return true;
                }
            }
        }

        return match.test(null);
    }
}
