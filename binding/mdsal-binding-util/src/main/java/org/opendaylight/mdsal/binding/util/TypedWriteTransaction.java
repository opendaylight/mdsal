/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.binding.api.WriteOperations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Write transaction which is specific to a single logical datastore (configuration or operational). Designed for use
 * with {@link ManagedNewTransactionRunner} (it doesn’t support explicit cancel or commit operations).
 *
 * @param <D> The logical datastore handled by the transaction.
 * @see WriteOperations
 */
public interface TypedWriteTransaction<D extends Datastore> extends Transaction {
    /**
     * Writes an object to the given path.
     *
     * @see WriteOperations#put(LogicalDatastoreType, InstanceIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to write.
     * @param <T> The type of the provided object.
     */
    <T extends DataObject> void put(InstanceIdentifier<T> path, T data);

    /**
     * Writes an object to the given path, creating missing parents if requested.
     *
     * @see WriteOperations#put(LogicalDatastoreType, InstanceIdentifier, DataObject, boolean)
     *
     * @param path The path to write to.
     * @param data The object to write.
     * @param createMissingParents {@link WriteOperations#CREATE_MISSING_PARENTS} to create missing parents,
     *                             {@link WriteOperations#FAIL_ON_MISSING_PARENTS} to fail if parents are missing.
     * @param <T> The type of the provided object.
     * @deprecated Use {@link #put(InstanceIdentifier, DataObject)} or
     *             {@link #createSignificantParentsAndPut(InstanceIdentifier, DataObject)} instead.
     */
    @Deprecated
    <T extends DataObject> void put(InstanceIdentifier<T> path, T data, boolean createMissingParents);

    /**
     * Writes an object to the given path, creating significant parents, like presence containers and list entries,
     * if needed.
     *
     * @see WriteOperations#createSignificantParentsAndPut(LogicalDatastoreType, InstanceIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to write.
     * @param <T> The type of the provided object.
     */
    // TODO: can we come up with a better name?
    @Beta
    @SuppressWarnings("deprecation")
    default <T extends DataObject> void createSignificantParentsAndPut(final InstanceIdentifier<T> path, final T data) {
        put(path, data, WriteOperations.CREATE_MISSING_PARENTS);
    }

    /**
     * Merges an object with the data already present at the given path.
     *
     * @see WriteOperations#merge(LogicalDatastoreType, InstanceIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to merge.
     * @param <T> The type of the provided object.
     */
    <T extends DataObject> void merge(InstanceIdentifier<T> path, T data);

    /**
     * Merges an object with the data already present at the given path, creating significant parents, like presence
     * containers and list entries, if needed.
     *
     * @see WriteOperations#merge(LogicalDatastoreType, InstanceIdentifier, DataObject, boolean)
     *
     * @param path The path to write to.
     * @param data The object to merge.
     * @param createMissingParents {@link WriteOperations#CREATE_MISSING_PARENTS} to create missing parents,
     *                             {@link WriteOperations#FAIL_ON_MISSING_PARENTS} to fail if parents are missing.
     * @param <T> The type of the provided object.
     * @deprecated Use {@link #merge(InstanceIdentifier, DataObject)} or
     *             {@link #createSignificantParentsAndMerge(InstanceIdentifier, DataObject)} instead.
     */
    @Deprecated
    <T extends DataObject> void merge(InstanceIdentifier<T> path, T data, boolean createMissingParents);

    /**
     * Merges an object with the data already present at the given path, creating missing parents if requested.
     *
     * @see WriteOperations#merge(LogicalDatastoreType, InstanceIdentifier, DataObject, boolean)
     *
     * @param path The path to write to.
     * @param data The object to merge.
     * @param <T> The type of the provided object.
     */
    // FIXME: 4.0.0: make this method non-default
    // TODO: can we come up with a better name?
    @Beta
    @SuppressWarnings("deprecation")
    default <T extends DataObject> void createSignificantParentsAndMerge(final InstanceIdentifier<T> path,
            final T data) {
        merge(path, data, WriteOperations.CREATE_MISSING_PARENTS);
    }

    /**
     * Deletes the object present at the given path.
     *
     * @see WriteOperations#delete(LogicalDatastoreType, InstanceIdentifier)
     *
     * @param path The path to delete.
     */
    void delete(InstanceIdentifier<?> path);
}
