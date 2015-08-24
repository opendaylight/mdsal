package org.opendaylight.mdsal.dom.api;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface DOMDataTreeReadCursor extends DOMDataTreeCursor {

    /**
     * Read a particular node from the snapshot.
     *
     * @param child Child identifier
     * @return Optional result encapsulating the presence and value of the node
     * @throws IllegalArgumentException when specified path does not identify a valid child.
     */
    CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> readNode(@Nonnull PathArgument child);

    /**
     * Checks if data is available in the logical data store located at provided path.
     * <p>
     *
     * Note: a successful result from this method makes no guarantee that a subsequent call to
     * {@link #read} will succeed. It is possible that the data resides in a data store on a remote
     * node and, if that node goes down or a network failure occurs, a subsequent read would fail.
     * Another scenario is if the data is deleted in between the calls to <code>exists</code> and
     * <code>read</code>
     *
     * @param store Logical data store from which read should occur.
     * @param path Path which uniquely identifies subtree which client want to check existence of
     * @return a CheckFuture containing the result of the check.
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns a Boolean whose value
     *         is true, false otherwise</li> <li>If checking for the data fails, the Future will
     *         fail with a {@link ReadFailedException} or an exception derived from
     *         ReadFailedException.</li>
     *         </ul>
     */
    CheckedFuture<Boolean, ReadFailedException> exists(@Nonnull PathArgument child);
}
