/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.FluentFuture;
import javax.annotation.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.AsyncWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A transaction that provides mutation capabilities on a data tree.
 *
 * <p>
 * Initial state of write transaction is a stable snapshot of the current data tree. The state is captured when
 * the transaction is created and its state and underlying data tree are not affected by other concurrently running
 * transactions.
 *
 * <p>
 * Write transactions are isolated from other concurrent write transactions. All writes are local to the transaction
 * and represent only a proposal of state change for the data tree and it is not visible to any other concurrently
 * running transaction.
 *
 * <p>
 * Applications make changes to the local data tree in the transaction by via the <b>put</b>, <b>merge</b>,
 * and <b>delete</b> operations.
 *
 * <h2>Put operation</h2>
 * Stores a piece of data at a specified path. This acts as an add / replace operation, which is to say that whole
 * subtree will be replaced by the specified data.
 *
 * <p>
 * Performing the following put operations:
 *
 * <pre>
 * 1) container { list [ a ] }
 * 2) container { list [ b ] }
 * </pre>
 * will result in the following data being present:
 *
 * <pre>
 * container { list [ b ] }
 * </pre>
 * <h2>Merge operation</h2>
 * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
 * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
 *
 * <p>
 * Performing the following merge operations:
 *
 * <pre>
 * 1) container { list [ a ] }
 * 2) container { list [ b ] }
 * </pre>
 * will result in the following data being present:
 *
 * <pre>
 * container { list [ a, b ] }
 * </pre>
 * This also means that storing the container will preserve any augmentations which have been attached to it.
 *
 * <h2>Delete operation</h2>
 * Removes a piece of data from a specified path.
 *
 * <p>
 * After applying changes to the local data tree, applications publish the changes proposed in the transaction
 * by calling {@link #commit} on the transaction. This seals the transaction (preventing any further writes using this
 * transaction) and commits it to be processed and applied to global conceptual data tree.
 *
 * <p>
 * The transaction commit may fail due to a concurrent transaction modifying and committing data in an incompatible way.
 * See {@link #commit} for more concrete commit failure examples.
 *
 * <p>
 * <b>Implementation Note:</b> This interface is not intended to be implemented by users of MD-SAL, but only to be
 * consumed by them.
 */
public interface WriteTransaction extends AsyncWriteTransaction<InstanceIdentifier<?>, DataObject> {
    @Override
    Object getIdentifier();

    @Override
    boolean cancel();

    @Override
    @CheckReturnValue
    @NonNull FluentFuture<? extends @NonNull CommitInfo> commit();

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
     * For more information on usage and examples, please see the documentation in {@link AsyncWriteTransaction}.
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
    <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data);

    /**
     * Stores a piece of data at the specified path. This acts as an add /
     * replace operation, which is to say that whole subtree will be replaced by
     * the specified data.
     *
     * <p>
     * For more information on usage and examples, please see the documentation
     * in {@link AsyncWriteTransaction}.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want
     * modify its pre-existing state by using put, consider using {@link #merge}
     * instead.
     *
     * <p>
     * Note: Using <code>createMissingParents</code> with value true, may
     * introduce garbage in data store, or recreate nodes, which were deleted by
     * previous transaction.
     *
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @param createMissingParents if {@link #CREATE_MISSING_PARENTS}, any missing parent nodes will be automatically
     *                             created using a merge operation.
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents);

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
     * For more information on usage and examples, please see the documentation in {@link AsyncWriteTransaction}.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     * @param store the logical data store which should be modified
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
     *
     * <p>
     * For more information on usage and examples, please see the documentation in {@link AsyncWriteTransaction}.
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
    <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents);

    @Override
    void delete(LogicalDatastoreType store, InstanceIdentifier<?> path);

    /**
     * Flag value indicating that missing parents should be created.
     */
    boolean CREATE_MISSING_PARENTS = true;

    /**
     * Flag value indicating that missing parents should cause an error.
     */
    boolean FAIL_ON_MISSING_PARENTS = false;
}
