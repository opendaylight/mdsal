/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

/**
 * @author nite
 *
 */
@NonNullByDefault
final class LazyDOMQueryResult implements DOMQueryResult {
    private final ArrayDeque<PathArgument> remaining;
    private final NormalizedNode<?, ?> data;
    private final DOMQuery query;

    LazyDOMQueryResult(final DOMQuery query, final ArrayDeque<PathArgument> remaining,
            final NormalizedNode<?, ?> data) {
        this.query = requireNonNull(query);
        this.remaining = requireNonNull(remaining);
        this.data = requireNonNull(data);
    }

    @Override
    public Iterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> iterator() {
        return new Iter(query, remaining, data);
    }

    static boolean matches(final NormalizedNode<?, ?> data, final DOMQuery query) {
        for (DOMQueryPredicate pred : query.getPredicates()) {
            // Okay, now we need to deal with predicates, but do it in a smart fashion, so we do not end up
            // iterating all over the place. Typically we will be matching just a leaf.
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

    private static final class Iter extends AbstractIterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
        private abstract static class Step {

        }

        private static final class NodeStep extends Step {
            final NormalizedNode<?, ?> data;

            NodeStep(final NormalizedNode<?, ?> data) {
                this.data = data;
            }
        }

        private static final class ChildrenStep extends Step {
            final Iterator<MapEntryNode> it;

            ChildrenStep(final Iterator<MapEntryNode> it) {
                this.it = it;
            }
        }

        private final ArrayDeque<Step> steps = new ArrayDeque<>();
        private final ArrayDeque<PathArgument> remaining;
        private final ArrayDeque<PathArgument> path;
        private final DOMQuery query;

        Iter(final DOMQuery query, final ArrayDeque<PathArgument> remaining, final NormalizedNode<?, ?> data) {
            this.path = new ArrayDeque<>(query.getRoot().getPathArguments());
            this.remaining = new ArrayDeque<>(remaining);
            this.query = query;
            steps.push(new NodeStep(data));
        }

        @Override
        protected Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> computeNext() {
            final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> nextEntry = findNextEntry(steps.poll());
            return nextEntry != null ? nextEntry : endOfData();
        }

        protected @Nullable Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> findNextEntry(final Step step) {
            Step currentStep = step;
            while (currentStep != null) {
                if (currentStep instanceof ChildrenStep) {
                    final Iterator<MapEntryNode> it = ((ChildrenStep) currentStep).it;
                    if (it.hasNext()) {
                        final MapEntryNode child = it.next();
                        steps.push(currentStep);
                        path.addLast(child.getIdentifier());
                        currentStep = new NodeStep(child);
                        continue;
                    }

                    path.removeLast();
                    currentStep = steps.poll();
                    continue;
                }

                verify(currentStep instanceof NodeStep, "Unexpected step %s", currentStep);
                final NormalizedNode<?, ?> data = ((NodeStep) currentStep).data;

                final PathArgument next = remaining.poll();
                if (next == null) {
                    if (matches(data, query)) {
                        return new SimpleImmutableEntry<>(YangInstanceIdentifier.create(path), data);
                    }

                    path.removeLast();
                    currentStep = steps.poll();
                    continue;
                }

                // Now the ugly part:
                if (data instanceof MapNode && next instanceof NodeIdentifier) {
                    checkArgument(data.getIdentifier().equals(next), "Unexpected step %s", next);
                    steps.push(currentStep);
                    currentStep = new ChildrenStep(((MapNode) data).getValue().iterator());
                    continue;
                }

                final Optional<NormalizedNode<?, ?>> optChild = NormalizedNodes.getDirectChild(data, next);
                if (optChild.isPresent()) {
                    steps.push(currentStep);
                    currentStep = new NodeStep(optChild.orElseThrow());
                    continue;
                }
                remaining.push(next);
            }
            return null;
        }
    }
}
