/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
     * This method does not automatically create missing parent nodes. It is equivalent to invoking
     * {@link #put(LogicalDatastoreType, InstanceIdentifier, DataObject, boolean)}
     * with <code>createMissingParents</code> set to false.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify
     * its pre-existing state by using put, consider using {@link #merge} instead.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void put(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull T data);

    /**
     * Stores a piece of data at the specified path. This acts as an add / replace operation, which is to say that whole
     * subtree will be replaced by the specified data.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify its pre-existing state by using
     * put, consider using {@link #merge} instead.
     *
     * <p>
     * Note: Using <code>createMissingParents</code> with value true, may introduce garbage in data store, or recreate
     * nodes, which were deleted by previous transaction.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @param createMissingParents if {@link #CREATE_MISSING_PARENTS}, any missing parent nodes will be automatically
     *                             created using a merge operation.
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void put(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull T data, boolean createMissingParents);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
     *
     * <p>
     * This method does not automatically create missing parent nodes. It is equivalent to invoking
     * {@link #merge(LogicalDatastoreType, InstanceIdentifier, DataObject, boolean)}
     * with <code>createMissingParents</code> set to false.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void merge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull T data);

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
     * @param createMissingParents if {@link #CREATE_MISSING_PARENTS}, any missing parent nodes will be automatically
     *                             created using a merge operation.
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void merge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull T data, boolean createMissingParents);

    /**
     * Removes a piece of data from specified path. This operation does not fail if the specified path does not exist.
     *
     * @param store Logical data store which should be modified
     * @param path Data object path
     * @throws IllegalStateException if the transaction was committed or canceled.
     */
    void delete(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path);

    /**
     * Flag value indicating that missing parents should be created.
     */
    boolean CREATE_MISSING_PARENTS = true;

    /**
     * Flag value indicating that missing parents should cause an error.
     */
    boolean FAIL_ON_MISSING_PARENTS = false;
}
