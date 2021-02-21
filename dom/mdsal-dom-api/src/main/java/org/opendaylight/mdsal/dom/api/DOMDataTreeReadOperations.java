/*
 * Copyright (c) 2018 Pantheon Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface DOMDataTreeReadOperations {
    /**
     * Reads data from provided logical data store located at the provided path.
     *
     *<p>
     * If the target is a subtree, then the whole subtree is read (and will be accessible from the returned data
     * object).
     *
     * @param store Logical data store from which read should occur.
     * @param path Path which uniquely identifies subtree which client want to read
     * @return a FluentFuture containing the result of the read. The Future blocks until the commit operation is
     *         complete. Once complete:
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns an Optional object containing the data.
     *         </li>
     *         <li>If the data at the supplied path does not exist, the Future returns Optional.empty().</li>
     *         <li>If the read of the data fails, the Future will fail with a {@link ReadFailedException} or
     *         an exception derived from ReadFailedException.</li>
     *         </ul>
     * @throws NullPointerException if any argument is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    FluentFuture<Optional<NormalizedNode>> read(LogicalDatastoreType store, YangInstanceIdentifier path);

    /**
     * Checks if data is available in the logical data store located at provided path.
     *
     * <p>
     * Note: a successful result from this method makes no guarantee that a subsequent call to {@link #read} will
     * succeed. It is possible that the data resides in a data store on a remote node and, if that node goes down or
     * a network failure occurs, a subsequent read would fail. Another scenario is if the data is deleted in between
     * the calls to <code>exists</code> and <code>read</code>
     *
     * @param store Logical data store from which read should occur.
     * @param path Path which uniquely identifies subtree which client want to check existence of
     * @return a FluentFuture containing the result of the check.
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns a Boolean whose value is true,
     *         false otherwise</li>
     *         <li>If checking for the data fails, the Future will fail with a {@link ReadFailedException} or
     *         an exception derived from ReadFailedException.</li>
     *         </ul>
     * @throws NullPointerException if any argument is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    FluentFuture<Boolean> exists(LogicalDatastoreType store, YangInstanceIdentifier path);
}
