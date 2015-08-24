package org.opendaylight.mdsal.dom.api;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.BackendFailedException;

public interface DOMDataTreeWriteCursor extends DOMDataTreeCursor {

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
    void merge(PathArgument child, NormalizedNode<?, ?> data);

    /**
     * Replace the data at specified path with supplied data.
     *
     * @param child Child identifier
     * @param data New node data
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the
     *         request.
     */
    void write(PathArgument child, NormalizedNode<?, ?> data);
}
