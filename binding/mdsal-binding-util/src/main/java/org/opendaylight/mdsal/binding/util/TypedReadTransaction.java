/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Read transaction which is specific to a single logical datastore (configuration or operational). Designed for use
 * with {@link ManagedNewTransactionRunner} (it doesn’t support explicit cancel or commit operations).
 *
 * @see ReadTransaction
 *
 * @param <D> The logical datastore handled by the transaction.
 */
public interface TypedReadTransaction<D extends Datastore> extends Transaction {
    /**
     * Reads an object from the given path.
     *
     * @see ReadTransaction#read(LogicalDatastoreType, InstanceIdentifier)
     *
     * @param path The path to read from.
     * @param <T> The type of the expected object.
     * @return A future providing access to the result of the read, when it’s available, or any error encountered.
     */
    <T extends DataObject> FluentFuture<Optional<T>> read(InstanceIdentifier<T> path);

    <T extends DataObject> FluentFuture<Optional<T>> read(
            org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path);

    /**
     * Determines if an object exists at the given path.
     *
     * @see ReadTransaction#exists(LogicalDatastoreType, InstanceIdentifier)
     *
     * @param path The path to read from.
     * @return A future providing access to the result of the check, when it’s available, or any error encountered.
     */
    FluentFuture<Boolean> exists(InstanceIdentifier<?> path);

    FluentFuture<Boolean> exists(org.opendaylight.mdsal.binding.api.InstanceIdentifier<?> path);

    /**
     * Executes a {@link QueryExpression}.
     *
     * @param query Query to execute
     * @param <T> The type of the expected object
     * @return a FluentFuture containing the result of the query. The Future blocks until the operation is complete.
     *         Once complete:
     *         <ul>
     *           <li>The Future returns the result of the query</li>
     *           <li>If the query execution fails, the Future will fail with a {@link ReadFailedException} or
     *               an exception derived from ReadFailedException.
     *            </li>
     *         </ul>
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if the query is not supported
     */
    <T extends @NonNull DataObject> FluentFuture<QueryResult<T>> execute(QueryExpression<T> query);
}
