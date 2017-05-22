/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.BackendFailedException;

/**
 * {@inheritDoc}
 *
 * <p>
 * In addition this cursor also provides write operations(delete, merge, write).
 */
@Beta
public interface DataTreeWriteCursor extends DataTreeCursor {

    /**
     * Delete the specified child.
     *
     * @param child Child identifier
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    void delete(TreeArgument<?> child);

    /**
     * Merge the specified data with the currently-present data at specified path.
     *
     * @param child Child identifier
     * @param data Data to be merged
     * @param <T> data type
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    <T extends TreeNode> void merge(TreeArgument<T> child, T data);

    /**
     * Replace the data at specified path with supplied data.
     *
     * @param child Child identifier
     * @param data New node data
     * @param <T> data type
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    <T extends TreeNode> void write(TreeArgument<T> child, T data);
}