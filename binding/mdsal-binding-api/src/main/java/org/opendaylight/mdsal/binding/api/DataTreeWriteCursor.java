/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.BackendFailedException;

public interface DataTreeWriteCursor extends DataTreeCursor {

    /**
     * Delete the specified child.
     *
     * @param child Child identifier
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    void delete(PathArgument child);

    /**
     * Merge the specified data with the currently-present data at specified path.
     *
     * @param child Child identifier
     * @param data Data to be merged
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    <T extends DataObject> void merge(PathArgument child, T data);

    /**
     * Replace the data at specified path with supplied data.
     *
     * @param child Child identifier
     * @param data New node data
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    <T extends DataObject> void write(PathArgument child, T data);
}
