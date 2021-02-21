/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Write-like operations supported by {@link WriteTransaction} and {@link ReadWriteTransaction}. This interface defines
 * the operations without a tie-in with lifecycle management.
 */
public interface WriteOperations {
    /**
     * Stores a piece of data at the specified path. This acts as an add / replace operation, which is to say that
     * whole subtree will be replaced by the specified data.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify its pre-existing state by using
     * put, consider using {@link #merge} instead.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    <T extends DataObject> void put(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull T data);

    /**
     * Stores a piece of data at the specified path. This acts as an add / replace operation, which is to say that whole
     * subtree will be replaced by the specified data. Unlike
     * {@link #put(LogicalDatastoreType, InstanceIdentifier, DataObject)}, this method will attempt to create
     * semantically-significant parent nodes, like list entries and presence containers, as indicated by {@code path}.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify its pre-existing state by using
     * put, consider using {@link #merge} instead.
     *
     * <p>
     * <b>WARNING:</b> Using this method may introduce garbage in data store, or recreate nodes, which were deleted by
     *                 a previous transaction. It also has a significantly higher cost than
     *                 {@link #put(LogicalDatastoreType, InstanceIdentifier, DataObject)} and should only be used when
     *                 absolutely necessary.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    // TODO: can we come up with a better name?
    @Beta
    <T extends DataObject> void mergeParentStructurePut(@NonNull LogicalDatastoreType store,
            @NonNull InstanceIdentifier<T> path, @NonNull T data);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    <T extends DataObject> void merge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull T data);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged. Unlike
     * {@link #merge(LogicalDatastoreType, InstanceIdentifier, DataObject)}, this method will attempt to create
     * semantically-significant parent nodes, like list entries and presence containers, as indicated by {@code path}.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     *
     * <p>
     * <b>WARNING:</b> Using this method may introduce garbage in data store, or recreate nodes, which were deleted by
     *                 a previous transaction. It is not necessary in most scenarios and has a significantly higher cost
     *                 than {@link #merge(LogicalDatastoreType, InstanceIdentifier, DataObject)}. It should only be used
     *                 when absolutely necessary.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    // TODO: can we come up with a better name?
    @Beta
    <T extends DataObject> void mergeParentStructureMerge(@NonNull LogicalDatastoreType store,
            @NonNull InstanceIdentifier<T> path, @NonNull T data);

    /**
     * Removes a piece of data from specified path. This operation does not fail if the specified path does not exist.
     *
     * @param store Logical data store which should be modified
     * @param path Data object path
     * @throws IllegalStateException if the transaction was committed or canceled.
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    void delete(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path);
}
