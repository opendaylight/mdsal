/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.binding.api.WriteOperations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

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
     * @see WriteOperations#put(LogicalDatastoreType, DataObjectIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to write.
     * @param <T> The type of the provided object.
     */
    <T extends DataObject> void put(DataObjectIdentifier<T> path, T data);

    /**
     * Writes an object to the given path, creating significant parents, like presence containers and list entries,
     * if needed.
     *
     * @see WriteOperations#mergeParentStructurePut(LogicalDatastoreType, DataObjectIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to write.
     * @param <T> The type of the provided object.
     */
    // TODO: can we come up with a better name?
    @Beta
    <T extends DataObject> void mergeParentStructurePut(DataObjectIdentifier<T> path, T data);

    /**
     * Merges an object with the data already present at the given path.
     *
     * @see WriteOperations#merge(LogicalDatastoreType, DataObjectIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to merge.
     * @param <T> The type of the provided object.
     */
    <T extends DataObject> void merge(DataObjectIdentifier<T> path, T data);

    /**
     * Merges an object with the data already present at the given path, creating missing parents if requested.
     *
     * @see WriteOperations#merge(LogicalDatastoreType, DataObjectIdentifier, DataObject)
     *
     * @param path The path to write to.
     * @param data The object to merge.
     * @param <T> The type of the provided object.
     */
    // TODO: can we come up with a better name?
    @Beta
    <T extends DataObject> void mergeParentStructureMerge(DataObjectIdentifier<T> path, T data);

    /**
     * Deletes the object present at the given path.
     *
     * @see WriteOperations#delete(LogicalDatastoreType, DataObjectIdentifier)
     *
     * @param path The path to delete.
     */
    void delete(DataObjectIdentifier<?> path);

    /**
     * Return a {@link FluentFuture} which completes.
     *
     * @return A future which completes when the requested operations complete.
     * @see WriteOperations#completionFuture()
     */
    @Beta
    @CheckReturnValue
    FluentFuture<?> completionFuture();
}
