/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Read-like operations supported by {@link ReadTransaction} and {@link ReadWriteTransaction}. This interface defines
 * the operations without a tie-in with lifecycle management.
 */
public interface ReadOperations {
    /**
     * Reads data from the provided logical data store located at the provided path.
     *
     * <p>
     * If the target is a subtree, then the whole subtree is read (and will be accessible from the returned data
     * object).
     *
     * @param store Logical data store from which read should occur.
     * @param path Path which uniquely identifies subtree which client want to read
     * @return a FluentFuture containing the result of the read. The Future blocks until the operation is complete. Once
     *         complete:
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns an Optional object containing the data.
     *         </li>
     *         <li>If the data at the supplied path does not exist, the Future returns Optional.empty().</li>
     *         <li>If the read of the data fails, the Future will fail with a {@link ReadFailedException} or
     *         an exception derived from ReadFailedException.</li>
     *         </ul>
     * @throws IllegalArgumentException if the path is
     *      {@link org.opendaylight.yangtools.yang.binding.InstanceIdentifier#isWildcarded()}
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    <T extends DataObject> @NonNull FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store,
            org.opendaylight.yangtools.yang.binding.@NonNull InstanceIdentifier<T> path);

    <T extends DataObject> @NonNull FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store,
            @NonNull InstanceIdentifier<T> path);
    /**
     * Determines if data data exists in the provided logical data store located at the provided path.
     *
     * <p>
     * Default implementation just delegates to {@link #read(LogicalDatastoreType, InstanceIdentifier)}. Implementations
     * are recommended to override with a more efficient implementation.
     *
     * @param store Logical data store from which read should occur.
     * @param path Path which uniquely identifies subtree which client want to read
     * @return a FluentFuture containing the result of the check. The Future blocks until the operation is complete.
     *         Once complete:
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns {@link Boolean#TRUE}.
     *         </li>
     *         <li>If the data at the supplied path does not exist, the Future returns {@link Boolean#FALSE}.</li>
     *         <li>If the check fails, the Future will fail with a {@link ReadFailedException} or an exception derived
     *             from ReadFailedException.</li>
     *         </ul>
     * @throws IllegalArgumentException if the path is
     *      {@link org.opendaylight.yangtools.yang.binding.InstanceIdentifier#isWildcarded()} and the implementation
     *                                  does not support evaluating wildcards.
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */

    @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store,
            org.opendaylight.yangtools.yang.binding.@NonNull InstanceIdentifier<?> path);

    @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path);
}
