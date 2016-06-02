/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;


/**
 * A cursor holding a logical position within a conceptual data tree. It allows operations relative
 * to that position, as well as moving the position up or down the tree.
 */
@Beta
@NotThreadSafe
public interface DataTreeCursor extends AutoCloseable {
    /**
     * Move the cursor to the specified child of the current position.
     *
     * @param child Child identifier
     * @throws IllegalArgumentException when specified identifier does not identify a valid child,
     *         or if that child is not an instance of {@link DataContainer}.
     */
    void enter(@Nonnull PathArgument child);

    /**
     * Move the cursor to the specified child of the current position. This is the equivalent of
     * multiple invocations of {@link #enter(PathArgument)}, except the operation is performed all
     * at once.
     *
     * @param path Nested child identifier
     * @throws IllegalArgumentException when specified path does not identify a valid child, or if
     *         that child is not an instance of {@link DataContainer}.
     */
    void enter(@Nonnull PathArgument... path);

    /**
     * Move the cursor to the specified child of the current position. This is equivalent to
     * {@link #enter(PathArgument...)}, except it takes an {@link Iterable} argument.
     *
     * @param path Nested child identifier
     * @throws IllegalArgumentException when specified path does not identify a valid child, or if
     *         that child is not an instance of {@link DataContainer}.
     */
    void enter(@Nonnull Iterable<PathArgument> path);

    /**
     * Move the cursor up to the parent of current position. This is equivalent of invoking
     * <code>exit(1)</code>.
     *
     * @throws IllegalStateException when exiting would violate containment, typically by attempting
     *         to exit more levels than previously entered.
     */
    void exit();

    /**
     * Move the cursor up by specified amounts of steps from the current position. This is
     * equivalent of invoking {@link #exit()} multiple times, except the operation is performed
     * atomically.
     *
     * @param depth number of steps to exit
     * @throws IllegalArgumentException when depth is negative.
     * @throws IllegalStateException when exiting would violate containment, typically by attempting
     *         to exit more levels than previously entered.
     */
    void exit(int depth);

    /**
     * Close this cursor. Attempting any further operations on the cursor will lead to undefined
     * behavior.
     */
    @Override
    void close();
}
