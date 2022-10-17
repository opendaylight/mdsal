/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.ds;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Read-like operations supported by {@link ReadTransaction} and {@link ReadWriteTransaction}. This interface defines
 * the operations without a tie-in with lifecycle management.
 */
@NonNullByDefault
public interface ReadOperations<D extends Datastore> {
    /**
     * Reads data from the provided logical data store located at the provided path.
     *
     * <p>
     * If the target is a subtree, then the whole subtree is read (and will be accessible from the returned data
     * object).
     *
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
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if the path is {@link InstanceIdentifier#isWildcarded()}
     */
    <T extends DataObject> FluentFuture<Optional<T>> read(InstanceIdentifier<T> path);

    /**
     * Determines if data data exists in the provided logical data store located at the provided path.
     *
     * <p>
     * Default implementation just delegates to {@link #read(InstanceIdentifier)}. Implementations
     * are recommended to override with a more efficient implementation.
     *
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
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if the path is {@link InstanceIdentifier#isWildcarded()} and the implementation
     *                                  does not support evaluating wildcards.
     */
    default FluentFuture<Boolean> exists(final InstanceIdentifier<?> path) {
        return read(path).transform(Optional::isPresent, MoreExecutors.directExecutor());
    }

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
    <T extends DataObject> FluentFuture<QueryResult<T>> execute(QueryExpression<T> query);
}
