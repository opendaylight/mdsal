/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

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
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    <T extends DataObject> @NonNull FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store,
            @NonNull DataObjectIdentifier<T> path);

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
     * @throws IllegalArgumentException if the path is {@link InstanceIdentifier#isWildcarded()}
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull FluentFuture<Optional<T>> read(final @NonNull LogicalDatastoreType store,
            final @NonNull InstanceIdentifier<T> path) {
        return read(store, LegacyUtils.legacyToIdentifier(path));
    }

    /**
     * Determines if data data exists in the provided logical data store located at the provided path.
     *
     * <p>
     * Default implementation just delegates to {@link #read(LogicalDatastoreType, DataObjectIdentifier)}.
     * Implementations are recommended to override with a more efficient implementation.
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
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    default @NonNull FluentFuture<Boolean> exists(final @NonNull LogicalDatastoreType store,
            final @NonNull DataObjectIdentifier<?> path) {
        return read(store, path).transform(Optional::isPresent, MoreExecutors.directExecutor());
    }

    /**
     * Determines if data data exists in the provided logical data store located at the provided path.
     *
     * <p>
     * Default implementation just delegates to {@link #read(LogicalDatastoreType, DataObjectIdentifier)} and throws
     * if the reference is not a {@link DataObjectIdentifier}. Implementations are recommended to override this method
     * if they supported {@link DataObjectReference} matching.
     *
     * @param store Logical data store from which read should occur.
     * @param path Path which uniquely identifies subtree which client want to read
     * @return a FluentFuture containing the result of the check. The Future blocks until the operation is complete.
     *         Once complete:
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns {@link Boolean#TRUE}.</li>
     *         <li>If the data at the supplied path does not exist, the Future returns {@link Boolean#FALSE}.</li>
     *         <li>If the check fails, the Future will fail with a {@link ReadFailedException} or an exception derived
     *             from ReadFailedException.</li>
     *         </ul>
     * @throws IllegalArgumentException if the path is not an {@link DataObjectIdentifier} and the implementation does
     *                                  not support evaluating wildcards.
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    default @NonNull FluentFuture<Boolean> exists(final @NonNull LogicalDatastoreType store,
            final @NonNull DataObjectReference<?> path) {
        return switch (path) {
            case DataObjectIdentifier<?> id -> exists(store, id);
            case InstanceIdentifier<?> id -> exists(store, id.toReference());
            default -> throw new IllegalArgumentException("Unsuported reference " + path);
        };
    }

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
     * @throws IllegalArgumentException if the path is {@link InstanceIdentifier#isWildcarded()} and the implementation
     *                                  does not support evaluating wildcards.
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     * @deprecated Use {@link #exists(LogicalDatastoreType, DataObjectIdentifier)} or
     *             {@link #exists(LogicalDatastoreType, DataObjectReference)} instead.
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default @NonNull FluentFuture<Boolean> exists(final @NonNull LogicalDatastoreType store,
            final @NonNull InstanceIdentifier<?> path) {
        return exists(store, path.toReference());
    }
}
