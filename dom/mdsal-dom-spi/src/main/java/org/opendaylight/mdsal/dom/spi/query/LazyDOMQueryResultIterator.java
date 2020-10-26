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
import com.google.common.collect.AbstractIterator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
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
final class LazyDOMQueryResultIterator extends AbstractIterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
    private static class Frame {
        final NormalizedNode<?, ?> data;
        final @Nullable PathArgument select;

        @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "Ungrokked @Nullable")
        Frame(final NormalizedNode<?, ?> data) {
            this.data = requireNonNull(data);
            // The only case when this can be null: if this a top-level container, as ensured by the sole caller
            select = null;
        }

        Frame(final NormalizedNode<?, ?> data, final PathArgument selectArg) {
            this.data = requireNonNull(data);
            this.select = requireNonNull(selectArg);
        }

        // Bimorphic invocation here, MapFrame checks with its iterator.
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

    // Steps remaining in the select part of the query. @Nullable helps with null analysis with Deque.poll()
    private final ArrayDeque<@Nullable PathArgument> remainingSelect = new ArrayDeque<>();
    // Absolute path from root of current data item
    private final ArrayDeque<PathArgument> currentPath = new ArrayDeque<>();
    // Work backlog, in terms of frames that need to be processed
    private final ArrayDeque<Frame> frames = new ArrayDeque<>();
    // The predicates which need to be evaluated
    private final List<? extends DOMQueryPredicate> predicates;

    LazyDOMQueryResultIterator(final DOMQuery query, final NormalizedNode<?, ?> queryRoot) {
        // Note: DOMQueryEvaluator has taken care of the empty case, this is always non-empty
        remainingSelect.addAll(query.getSelect().getPathArguments());
        currentPath.addAll(query.getRoot().getPathArguments());
        predicates = query.getPredicates();
        frames.push(new Frame(queryRoot));
    }

    @Override
    protected Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> computeNext() {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> next = findNext();
        return next != null ? next : endOfData();
    }

    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Ungrokked @Nullable")
    private @Nullable Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> findNext() {
        // We always start with non-empty frames, as we signal end of data when we reach the end. We know this never
        // null, by Eclipse insists. We do not care (that much) and use a poll() here.
        // TODO: this is a huge method which could be restructured with hard tailcalls, alas we do not have those (yet?)
        //       Any such refactor better have some benchmarks to show non-regression.
        Frame current = frames.poll();
        while (current != null) {
            final PathArgument next = remainingSelect.poll();
            if (next == null) {
                // We are matching this frame, and if we got here it must have a stashed iterator, as we deal with
                // single match entries without using the stack. Look for first matching child and return it.
                final Iterator<MapEntryNode> iter = ((MapFrame) current).iter;
                while (iter.hasNext()) {
                    final MapEntryNode child = iter.next();
                    if (matches(child)) {
                        return pushAndReturn(current, child);
                    }
                }

                // Unwind this frame's state and select the next frame from the stack
                current = unwind(current.select);
                continue;
            }

            // Alright, here we are looking for a child to select. This is where things get dicey, as there is a number
            // of possibilities:

            // 1. we are iterating a map. We are matching the next child against 'next', which can have a number of
            //    outcomes in and of itself.
            if (current instanceof MapFrame) {
                final Iterator<MapEntryNode> iter = ((MapFrame) current).iter;
                if (remainingSelect.isEmpty()) {
                    // ... so all of 1) and this is the last-step map. In this case we want to find the next matching
                    //     child without going to stack. We want to push next back, though, as we either need to resume
                    //     from it (arriving back here), or will have dealt with it.
                    while (iter.hasNext()) {
                        final MapEntryNode child = iter.next();
                        if (matches(child)) {
                            remainingSelect.push(next);
                            return pushAndReturn(current, child);
                        }
                    }

                    // Unwind frames and retry
                    current = unwind(current, next);
                    continue;
                }

                // ... so all of 1) but this time this is an intermediate step. If we have a child, we'll push the map
                //     entry and set the child frame as current. Let the loop deal with the rest of the lookup.
                if (iter.hasNext()) {
                    final MapEntryNode child = iter.next();
                    frames.push(current);
                    currentPath.addLast(child.getIdentifier());
                    current = new Frame(child, next);
                    continue;
                }

                // ... all of 1) but we do not have any more children to match. Discard this frame and move on.
                current = unwind(current, next);
                continue;
            }

            // 2. we are at a normal container, where we need to resolve a child. This is also a bit involved, so now:
            final Optional<NormalizedNode<?, ?>> optChild = NormalizedNodes.getDirectChild(current.data, next);
            if (optChild.isEmpty()) {
                // If we did not find the child, as we can have only a single match. Unwind to next possible match.
                current = unwind(current, next);
                continue;
            }

            // If we have a child see if this is the ultimate select step, if so, short circuit stack. We do not record
            // ourselves.
            final NormalizedNode<?, ?> child = optChild.orElseThrow();
            if (remainingSelect.isEmpty()) {
                // This is the ultimate step in lookup, process it without churning the stack by imposing a dedicated
                // Frame. In either case we are done with this frame, unwinding it in both cases.
                if (matches(child)) {
                    final YangInstanceIdentifier childPath = createIdentifier(child);
                    unwind(current, next);
                    return new SimpleImmutableEntry<>(childPath, child);
                }

                current = unwind(current, next);
                continue;
            }

            // Push our state back, it's just a placeholder for 'currentSelect'. Current path points at us and so does
            // the saved Frame.
            currentPath.addLast(current.data.getIdentifier());
            frames.push(current);

            // Now decide what sort of entry to push. For maps we want to start an iterator already, so it gets
            // picked up as a continuation.
            current = child instanceof MapNode ? new MapFrame(child, next, ((MapNode) child).getValue().iterator())
                : new Frame(child, next);
        }

        // All done, there be entries no more.
        // Consistency check and clear leftover state
        verify(frames.isEmpty());
        remainingSelect.clear();
        currentPath.clear();
        return null;
    }

    // Construct child path. This concatenates currentPath and child's identifier.
    private YangInstanceIdentifier createIdentifier(final NormalizedNode<?, ?> child) {
        currentPath.addLast(child.getIdentifier());
        final YangInstanceIdentifier ret = YangInstanceIdentifier.create(currentPath);
        currentPath.removeLast();
        return ret;
    }

    // Save a frame for further processing return its child as an item.
    private Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> pushAndReturn(final Frame frame,
            final MapEntryNode child) {
        final YangInstanceIdentifier childPath = createIdentifier(child);

        // Push the frame back to work, return the result
        frames.push(frame);
        return new SimpleImmutableEntry<>(childPath, child);
    }

    /**
     * Unwind the stack, discarding current frame, and possibly some others. The unwind starts with pushing {@code next}
     * to {@link #remainingSelect}, hence we remember to handle it next time around. It then defers to
     * {@link #unwind(PathArgument)}.
     *
     * @param current Current frame
     * @param next Next path argument to lookup (after this frame)
     * @return Next frame to process, null if there is no other work
     */
    private @Nullable Frame unwind(final Frame current, final PathArgument next) {
        remainingSelect.push(next);
        return unwind(current.select);
    }

    /**
     * Unwind the stack, discarding current frame, and possibly some others. Unwind removes contents of
     * {@link #currentPath}, walking back towards the query root.
     *
     * <p>
     * Since we are unwinding a data item, we pop its path -- hence {@link #currentPath} points to the parent path.
     * We then examine {@link Frame#select} to see if it's null -- if it is, we have reached the top-most frame and
     * hence have nothing left to do.
     *
     * <p>
     * Otherwise we remember {@code select} back to {@link #remainingSelect} and pop the next frame to be processed.
     * If the frame does not have work, as indicated by {@link Frame#hasNext()}, we unwind it as well.
     *
     * <p>
     * We repeat this process until we find a frame with some work or we run out of frames.
     *
     * @param current Current frame
     * @param next Next path argument to lookup (after this frame)
     * @return Next frame to process, null if there is no other work
     */
    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Ungrokked @Nullable")
    private @Nullable Frame unwind(final @Nullable PathArgument selectArg) {
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

    private boolean matches(final NormalizedNode<?, ?> data) {
        return matches(data, predicates);
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
