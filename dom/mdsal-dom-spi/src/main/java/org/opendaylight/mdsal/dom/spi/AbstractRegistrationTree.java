/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * An abstract tree of registrations. Allows a read-only snapshot to be taken.
 *
 * @param <T> Type of registered object
 */
public abstract class AbstractRegistrationTree<T> {
    private final RegistrationTreeNode<T> rootNode = new RegistrationTreeNode<>(null, null);
    private final Lock writeLock;
    private final Lock readLock;

    protected AbstractRegistrationTree() {
        final StampedLock lock = new StampedLock();
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
    protected final @NonNull RegistrationTreeNode<T> findNodeFor(final @NonNull Iterable<PathArgument> path) {
        RegistrationTreeNode<T> walkNode = rootNode;
        for (final PathArgument arg : path) {
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
    protected final void addRegistration(final @NonNull RegistrationTreeNode<T> node, final @NonNull T registration) {
        node.addRegistration(registration);
    }

    /**
     * Remove a registration from a particular node. This method must not be called while the lock is held.
     *
     * @param node Tree node
     * @param registration Registration instance
     */
    protected final void removeRegistration(final @NonNull RegistrationTreeNode<T> node,
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
     * Obtain a tree snapshot. This snapshot ensures a consistent view of
     * registrations. The snapshot should be closed as soon as it is not required,
     * because each unclosed instance blocks modification of this tree.
     *
     * @return A snapshot instance.
     */
    public final @NonNull RegistrationTreeSnapshot<T> takeSnapshot() {
        readLock.lock();
        return new RegistrationTreeSnapshot<>(readLock, rootNode);
    }
}
