/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

@NonNullByDefault
final class LazyDOMQueryResultIterator extends AbstractIterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
    private static final class Frame {
        final NormalizedNode<?, ?> data;
        final @Nullable PathArgument selectArg;
        final @Nullable Iterator<DataContainerChild<?, ?>> childIter;

        Frame(final NormalizedNode<?, ?> data) {
            this.data = requireNonNull(data);
            selectArg = null;
            childIter = null;
        }

        Frame(final NormalizedNode<?, ?> data, final PathArgument selectArg) {
            this.data = requireNonNull(data);
            this.selectArg = requireNonNull(selectArg);
            childIter = null;
        }

        Frame(final NormalizedNode<?, ?> data, final PathArgument selectArg,
                final Iterator<DataContainerChild<?, ?>> childIter) {
            this.data = requireNonNull(data);
            this.selectArg = requireNonNull(selectArg);
            this.childIter = requireNonNull(childIter);
        }

        boolean hasNext() {
            return childIter != null && childIter.hasNext();
        }
    }

    // Work backlog, in terms of frames that need to be processed
    private final ArrayDeque<Frame> frames = new ArrayDeque<>();
    // Steps remaining in the select part of the query
    private final ArrayDeque<PathArgument> remainingSelect;
    // Absolute path from root of current data item
    private final ArrayDeque<PathArgument> currentPath;
    // The query which needs to be executed
    private final DOMQuery query;

    LazyDOMQueryResultIterator(final DOMQuery query, final NormalizedNode<?, ?> queryRoot) {
        this.query = requireNonNull(query);
        currentPath = new ArrayDeque<>(query.getRoot().getPathArguments());
        // Note: DOMQueryEvaluator has taken care of the empty case, this is always non-empty
        remainingSelect = new ArrayDeque<>(query.getSelect().getPathArguments());
        frames.push(new Frame(queryRoot));
    }

    @Override
    protected Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> computeNext() {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> next = findNext();
        if (next != null) {
            return next;
        }

        // Consistency check and clean up of leftover state
        verify(frames.isEmpty());
        remainingSelect.clear();
        currentPath.clear();
        return endOfData();
    }

    private @Nullable Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> findNext() {
        // We always start with non-empty frames, as we signal end of data when we reach the end
        Frame current = frames.pop();
        do {
            final PathArgument next = remainingSelect.poll();
            if (next == null) {
                // We are matching this frame, and if we got here it must have a stashed iterator, as we deal with
                // single match entries without using the stack. Look for first matching child and return it.
                final Iterator<DataContainerChild<?, ?>> iter = verifyNotNull(current.childIter);
                while (iter.hasNext()) {
                    final DataContainerChild<?, ?> child = iter.next();
                    if (matches(child, query)) {
                        return mapChildMatch(current, child);
                    }
                }

                // Unwind this frame's state and select the next frame from the stack
                current = unwindFrames(current.selectArg);
                continue;
            }

            // Alright, here we are looking for a child to select. This is where things get dicey, as there is a number
            // of possibilities:

            // 1. we are iterating a map. We are matching the next child against 'next', which can have a number of
            //    outcomes in and of itself.
            final Iterator<DataContainerChild<?, ?>> iter = current.childIter;
            if (iter != null) {
                if (remainingSelect.isEmpty()) {
                    // ... and this is the last-step map. In this case we want to find the next matching child without
                    //     going to stack. We want to push next back, though, as we either need to resume from it
                    //     (arriving back here), or will have dealt with it.
                    while (iter.hasNext()) {
                        final DataContainerChild<?, ?> child = iter.next();
                        if (matches(child, query)) {
                            remainingSelect.push(next);
                            return mapChildMatch(current, child);
                        }
                    }

                    // Unwind frames and retry
                    current = unwindFrames(current, next);
                    continue;
                }

                // ... and this is an intermediate step. If we have a child, we'll push the map entry and set the child
                //     frame as current. Let the loop deal with the rest of the lookup.
                if (iter.hasNext()) {
                    final DataContainerChild<?, ?> child = iter.next();
                    frames.push(current);
                    currentPath.addLast(child.getIdentifier());
                    current = new Frame(child, next);
                    continue;
                }

                // So now, we have nothing more contribute to the conversation: we ended up with no items. Rewind the
                // stack and continue onwards.
                current = unwindFrames(current, next);
                continue;
            }

            // 2. we are at a normal container, where we need to resolve a child. This is also a bit involved, so now:
            //
            // If we do not find it, we will not find it anywhere underneath, so we end up unwinding the stack.
            final Optional<NormalizedNode<?, ?>> optChild = NormalizedNodes.getDirectChild(current.data, next);
            if (optChild.isEmpty()) {
                current = unwindFrames(current, next);
                continue;
            }

            // If we have a child see if this is the ultimate select step, if so, short circuit stack. We do not record
            // ourselves.
            final NormalizedNode<?, ?> child = optChild.orElseThrow();
            if (remainingSelect.isEmpty()) {
                if (matches(child, query)) {
                    // Construct child path
                    currentPath.addLast(child.getIdentifier());
                    final YangInstanceIdentifier childPath = YangInstanceIdentifier.create(currentPath);
                    currentPath.removeLast();

                    // Unwind stack, as we have nothing more to add -- we can have only one match.
                    current = unwindFrames(current, next);
                    return new SimpleImmutableEntry<>(childPath, child);
                }

                // Unwind stack, as we have nothing more to add.
                current = unwindFrames(current, next);
                continue;
            }

            // Push our state back, it's just a placeholder for 'currentSelect'
            currentPath.addLast(current.data.getIdentifier());
            frames.push(current);

            // Now decide what sort of entry to push. For maps we want to start an iterator already, so it gets
            // picked up as a continuation.
            current = child instanceof MapEntryNode
                ? new Frame(child, next, ((MapEntryNode) child).getValue().iterator())
                    : new Frame(child, next);
        } while (current != null);

        return null;
    }

    Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> mapChildMatch(final Frame parent,
            final DataContainerChild<?, ?> child) {
        // Construct child path
        currentPath.addLast(child.getIdentifier());
        final YangInstanceIdentifier childPath = YangInstanceIdentifier.create(currentPath);
        currentPath.removeLast();

        // Push the frame back to work, return the result
        frames.push(parent);
        return new SimpleImmutableEntry<>(childPath, child);
    }

    @Nullable Frame unwindFrames(final Frame current, final PathArgument next) {
        remainingSelect.push(next);
        return unwindFrames(current.selectArg);
    }

    @Nullable Frame unwindFrames(final @Nullable PathArgument selectArg) {
        @Nullable PathArgument select = selectArg;
        while (true) {
            currentPath.removeLast();
            if (select == null) {
                verify(frames.isEmpty());
                return null;
            }

            remainingSelect.push(select);
            final Frame next = frames.poll();
            if (next == null) {
                return null;
            }
            if (next.hasNext()) {
                return next;
            }
            select = next.selectArg;
        }
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
