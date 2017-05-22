/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Provides access to {#link DataTreeCursor}'s anchored at the specified path.
 */
@Beta
public interface DataTreeCursorProvider {

    /**
     * Create a new {@link DataTreeCursor} at specified path. May fail if specified path
     * does not exist.
     *
     * @param path Path at which the cursor is to be anchored
     * @param <T> data tree type
     * @return A new cursor, or null if the path does not exist.
     * @throws IllegalStateException if there is another cursor currently open, or the transaction
     *         is already closed (closed or submitted).
     */
    @Nullable
    <T extends TreeNode> DataTreeCursor createCursor(@Nonnull DataTreeIdentifier<T> path);
}