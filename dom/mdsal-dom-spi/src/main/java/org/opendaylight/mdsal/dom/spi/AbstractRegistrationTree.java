/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract tree of registrations. Allows a read-only snapshot to be taken.
 *
 * @param <T> Type of registered object
 */
public abstract class AbstractRegistrationTree<T> {
    /**
     * This is a single node within the registration tree. Note that the data returned from and instance of this class
     * is guaranteed to have any relevance or consistency only as long as the {@link Snapshot} instance through which it
     * is reached remains unclosed.
     *
     * @param <T> registration type
     */
    protected static final class Node<T> implements Identifiable<PathArgument> {
        private static final Logger LOG = LoggerFactory.getLogger(Node.class);

        private final Map<PathArgument, Node<T>> children = new HashMap<>();
        private final List<T> registrations = new ArrayList<>(2);
        private final List<T> publicRegistrations = Collections.unmodifiableList(registrations);
        private final Reference<Node<T>> parent;
        private final PathArgument identifier;

        Node(final Node<T> parent, final PathArgument identifier) {
            this.parent = new WeakReference<>(parent);
            this.identifier = identifier;
        }

        @Override
        public PathArgument getIdentifier() {
            return identifier;
        }

        /**
         * Return the child matching a {@link PathArgument} specification.
         *
         * @param arg Child identifier
         * @return Child matching exactly, or {@code null}.
         */
        public @Nullable Node<T> getExactChild(final @NonNull PathArgument arg) {
            return children.get(requireNonNull(arg));
        }

        /**
         * Return a collection children which match a {@link PathArgument} specification inexactly.
         * This explicitly excludes the child returned by {@link #getExactChild(PathArgument)}.
         *
         * @param arg Child identifier
         * @return Collection of children, guaranteed to be non-null.
         */
        public @NonNull Collection<Node<T>> getInexactChildren(final @NonNull PathArgument arg) {
            requireNonNull(arg);
            if (arg instanceof NodeWithValue || arg instanceof NodeIdentifierWithPredicates) {
                /*
                 * TODO: This just all-or-nothing wildcards, which we have historically supported. Given
                 *       that the argument is supposed to have all the elements filled out, we could support
                 *       partial wildcards by iterating over the registrations and matching the maps for
                 *       partial matches.
                 */
                final var child = children.get(new NodeIdentifier(arg.getNodeType()));
                return child == null ? List.of() : Collections.singletonList(child);
            }

            return List.of();
        }

        public Collection<T> getRegistrations() {
            return publicRegistrations;
        }

        @VisibleForTesting
        @NonNull Node<T> ensureChild(final @NonNull PathArgument child) {
            return children.computeIfAbsent(requireNonNull(child), key -> new Node<>(this, key));
        }

        @VisibleForTesting
        void addRegistration(final @NonNull T registration) {
            registrations.add(requireNonNull(registration));
            LOG.debug("Registration {} added", registration);
        }

        @VisibleForTesting
        void removeRegistration(final @NonNull T registration) {
            if (registrations.remove(requireNonNull(registration))) {
                LOG.debug("Registration {} removed", registration);

                // We have been called with the write-lock held, so we can perform some cleanup.
                removeThisIfUnused();
            }
        }

        private void removeThisIfUnused() {
            final var p = parent.get();
            if (p != null && registrations.isEmpty() && children.isEmpty()) {
                p.removeChild(identifier);
            }
        }

        private void removeChild(final PathArgument arg) {
            children.remove(arg);
            removeThisIfUnused();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("identifier", identifier)
                .add("registrations", registrations.size())
                .add("children", children.size())
                .toString();
        }
    }

    /**
     * A stable read-only snapshot of a {@link AbstractRegistrationTree}.
     */
    @NonNullByDefault
    protected static final class Snapshot<T> extends AbstractRegistration {
        private final Node<T> node;
        private final Lock lock;

        Snapshot(final Lock lock, final Node<T> node) {
            this.lock = requireNonNull(lock);
            this.node = requireNonNull(node);
        }

        public Node<T> getRootNode() {
            return node;
        }

        @Override
        protected void removeRegistration() {
            lock.unlock();
        }
    }

    private final @NonNull Node<T> rootNode = new Node<>(null, null);
    private final @NonNull Lock writeLock;
    private final @NonNull Lock readLock;

    protected AbstractRegistrationTree() {
        final var lock = new StampedLock();
        readLock = lock.asReadLock();
        writeLock = lock.asWriteLock();
    }

    /**
     * Acquire the read-write lock. This should be done before invoking {@link #findNodeFor(Iterable)}. This method
     * must not be called when the lock is already held by this thread.
     */
    protected final void takeLock() {
        writeLock.lock();
    }

    /**
     * Release the read-write lock. This should be done after invocation of {@link #findNodeFor(Iterable)}
     * and modification of the returned node. Note that callers should do so in a finally block.
     */
    protected final void releaseLock() {
        writeLock.unlock();
    }

    /**
     * Find an existing, or allocate a fresh, node for a particular path. Must be called with the lock held.
     *
     * @param path Path to find a node for
     * @return A registration node for the specified path
     */
    protected final @NonNull Node<T> findNodeFor(final @NonNull Iterable<PathArgument> path) {
        var walkNode = rootNode;
        for (var arg : path) {
            walkNode = walkNode.ensureChild(arg);
        }
        return walkNode;
    }

    /**
     * Add a registration to a particular node. The node must have been returned via {@link #findNodeFor(Iterable)}
     * and the lock must still be held.
     *
     * @param node Tree node
     * @param registration Registration instance
     */
    protected final void addRegistration(final @NonNull Node<T> node, final @NonNull T registration) {
        node.addRegistration(registration);
    }

    /**
     * Remove a registration from a particular node. This method must not be called while the lock is held.
     *
     * @param node Tree node
     * @param registration Registration instance
     */
    protected final void removeRegistration(final @NonNull Node<T> node,
            final @NonNull T registration) {
        // Take the write lock
        writeLock.lock();
        try {
            node.removeRegistration(registration);
        } finally {
            // Always release the lock
            writeLock.unlock();
        }
    }

    /**
     * Obtain a tree snapshot. This snapshot ensures a consistent view of registrations. The snapshot should be closed
     * as soon as it is not required, because each unclosed instance blocks modification of this tree.
     *
     * @return A snapshot instance.
     */
    protected final @NonNull Snapshot<T> takeSnapshot() {
        readLock.lock();
        return new Snapshot<>(readLock, rootNode);
    }
}
