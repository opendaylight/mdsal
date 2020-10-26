/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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
    public static DOMQueryResult evaluateOn(final DOMQuery query, final NormalizedNode<?, ?> queryRoot) {
        final YangInstanceIdentifier path = query.getSelect();
        return path.isEmpty() ? evalSingle(query, queryRoot) : new LazyDOMQueryResult(query, queryRoot);
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
    public static DOMQueryResult evaluateOnRoot(final DOMQuery query, final NormalizedNode<?, ?> root) {
        NormalizedNode<?, ?> evalRoot = root;
        for (PathArgument arg : query.getRoot().getPathArguments()) {
            final Optional<NormalizedNode<?, ?>> next = NormalizedNodes.findNode(root, arg);
            if (next.isEmpty()) {
                return DOMQueryResult.of();
            }
            evalRoot = next.orElseThrow();
        }
        return evaluateOn(query, evalRoot);
    }

    private static DOMQueryResult evalSingle(final DOMQuery query, final NormalizedNode<?, ?> data) {
        return LazyDOMQueryResultIterator.matches(data, query.getPredicates()) ? DOMQueryResult.of()
                : DOMQueryResult.of(new SimpleImmutableEntry<>(query.getRoot(), data));
    }
}
