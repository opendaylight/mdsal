/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

@NonNullByDefault
final class LazyDOMQueryResultSpliterator implements Spliterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
    private static class Frame {
        final NormalizedNode<?, ?> data;

        // Nullable is accurate, as it marks our top-level container. Once that goes out, are ending anyway.
        final @Nullable PathArgument select;

        Frame(final NormalizedNode<?, ?> data) {
            this.data = requireNonNull(data);
            select = null;
        }

        Frame(final NormalizedNode<?, ?> data, final PathArgument selectArg) {
            this.data = requireNonNull(data);
            this.select = requireNonNull(selectArg);
        }

        boolean hasNext() {
            return false;
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
        }

        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("data", data.getIdentifier()).add("select", select);
        }
    }

    private static final class MapFrame extends Frame {
        final Iterator<MapEntryNode> iter;

        MapFrame(final NormalizedNode<?, ?> data, final PathArgument selectArg, final Iterator<MapEntryNode> iter) {
            super(data, selectArg);
            this.iter = requireNonNull(iter);
        }

        @Override
        boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("hasNext", iter.hasNext());
        }
    }

    // Work backlog, in terms of frames that need to be processed
    private final ArrayDeque<Frame> frames = new ArrayDeque<>();
    // Steps remaining in the select part of the query. @Nullable helps with null analysis with Deque.poll()
    private final ArrayDeque<@Nullable PathArgument> remainingSelect;
    // Absolute path from root of current data item
    private final ArrayDeque<PathArgument> currentPath;
    // The predicates which need to be evalued
    private final List<? extends DOMQueryPredicate> predicates;

    LazyDOMQueryResultSpliterator(final DOMQuery query, final NormalizedNode<?, ?> queryRoot) {
        currentPath = new ArrayDeque<>(query.getRoot().getPathArguments());
        // Note: DOMQueryEvaluator has taken care of the empty case, this is always non-empty
        remainingSelect = new ArrayDeque<>(query.getSelect().getPathArguments());
        predicates = query.getPredicates();
        frames.push(new Frame(queryRoot));
    }

    @Override
    public @Nullable Spliterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> trySplit() {
        // FIXME: implement this
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return DISTINCT | IMMUTABLE | NONNULL;
    }

    @Override
    public boolean tryAdvance(
            final @Nullable Consumer<? super Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> action) {
        requireNonNull(action);

        // We always start with non-empty frames, as we signal end of data when we reach the end
        Frame current = frames.pop();
        do {
            final PathArgument next = remainingSelect.poll();
            if (next == null) {
                // We are matching this frame, and if we got here it must have a stashed iterator, as we deal with
                // single match entries without using the stack. Look for first matching child and return it.
                final Iterator<MapEntryNode> iter = ((MapFrame) current).iter;
                while (iter.hasNext()) {
                    final MapEntryNode child = iter.next();
                    if (matches(child, predicates)) {
                        action.accept(mapChildMatch(current, child));
                        return true;
                    }
                }

                // Unwind this frame's state and select the next frame from the stack
                current = unwindFrames(current.select);
                continue;
            }

            // Alright, here we are looking for a child to select. This is where things get dicey, as there is a number
            // of possibilities:

            // 1. we are iterating a map. We are matching the next child against 'next', which can have a number of
            //    outcomes in and of itself.
            if (current instanceof MapFrame) {
                final Iterator<MapEntryNode> iter = ((MapFrame) current).iter;
                if (remainingSelect.isEmpty()) {
                    // ... and this is the last-step map. In this case we want to find the next matching child without
                    //     going to stack. We want to push next back, though, as we either need to resume from it
                    //     (arriving back here), or will have dealt with it.
                    while (iter.hasNext()) {
                        final MapEntryNode child = iter.next();
                        if (matches(child, predicates)) {
                            remainingSelect.push(next);
                            action.accept(mapChildMatch(current, child));
                            return true;
                        }
                    }

                    // Unwind frames and retry
                    current = unwindFrames(current, next);
                    continue;
                }

                // ... and this is an intermediate step. If we have a child, we'll push the map entry and set the child
                //     frame as current. Let the loop deal with the rest of the lookup.
                if (iter.hasNext()) {
                    final MapEntryNode child = iter.next();
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
                if (matches(child, predicates)) {
                    // Construct child path
                    currentPath.addLast(child.getIdentifier());
                    final YangInstanceIdentifier childPath = YangInstanceIdentifier.create(currentPath);
                    currentPath.removeLast();

                    // Unwind stack, as we have nothing more to add -- we can have only one match.
                    current = unwindFrames(current, next);
                    action.accept(new SimpleImmutableEntry<>(childPath, child));
                    return true;
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
            current = child instanceof MapNode
                ? new MapFrame(child, next, ((MapNode) child).getValue().iterator())
                    : new Frame(child, next);
        } while (current != null);

        // Consistency check and clean up of leftover state
        verify(frames.isEmpty());
        verify(remainingSelect.isEmpty());
        currentPath.clear();
        return false;
    }

    Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> mapChildMatch(final Frame parent, final MapEntryNode child) {
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
        return unwindFrames(current.select);
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
            // pop() for its state-checking properties. Last frame should have had select == null and we would have
            // bailed there.
            final Frame next = frames.pop();
            if (next.hasNext()) {
                return next;
            }
            select = next.select;
        }
    }

    static boolean matches(final NormalizedNode<?, ?> data, final List<? extends DOMQueryPredicate> predicates) {
        for (DOMQueryPredicate pred : predicates) {
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
