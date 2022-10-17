/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.ds;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Write-like operations supported by {@link WriteTransaction} and {@link ReadWriteTransaction}. This interface defines
 * the operations without a tie-in with lifecycle management.
 */
@NonNullByDefault
public interface WriteOperations<D extends Datastore> {
    /**
     * Stores a piece of data at the specified path. This acts as an add / replace operation, which is to say that
     * whole subtree will be replaced by the specified data.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify its pre-existing state by using
     * put, consider using {@link #merge} instead.
     *
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void put(InstanceIdentifier<T> path, @NonNull T data);

    /**
     * Stores a piece of data at the specified path. This acts as an add / replace operation, which is to say that whole
     * subtree will be replaced by the specified data. Unlike {@link #put(InstanceIdentifier, DataObject)}, this method
     * will attempt to create semantically-significant parent nodes, like list entries and presence containers, as
     * indicated by {@code path}.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify its pre-existing state by using
     * put, consider using {@link #merge} instead.
     *
     * <p>
     * <b>WARNING:</b> Using this method may introduce garbage in data store, or recreate nodes, which were deleted by
     *                 a previous transaction. It also has a significantly higher cost than
     *                 {@link #put(InstanceIdentifier, DataObject)} and should only be used when absolutely necessary.
     *
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    // TODO: can we come up with a better name?
    @Beta
    <T extends DataObject> void mergeParentStructurePut(InstanceIdentifier<T> path, T data);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     *
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    <T extends DataObject> void merge(InstanceIdentifier<T> path, T data);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged. Unlike
     * {@link #merge(InstanceIdentifier, DataObject)}, this method will attempt to create semantically-significant
     * parent nodes, like list entries and presence containers, as indicated by {@code path}.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     *
     * <p>
     * <b>WARNING:</b> Using this method may introduce garbage in data store, or recreate nodes, which were deleted by
     *                 a previous transaction. It is not necessary in most scenarios and has a significantly higher cost
     *                 than {@link #merge(InstanceIdentifier, DataObject)}. It should only be used when absolutely
     *                 necessary.
     *
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any of the arguments is null
     */
    // TODO: can we come up with a better name?
    @Beta
    <T extends DataObject> void mergeParentStructureMerge(InstanceIdentifier<T> path, T data);

    /**
     * Removes a piece of data from specified path. This operation does not fail if the specified path does not exist.
     *
     * @param path Data object path
     * @throws IllegalStateException if the transaction was committed or canceled.
     */
    void delete(InstanceIdentifier<?> path);
}
