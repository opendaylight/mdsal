/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.ds;

import com.google.common.util.concurrent.FluentFuture;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Query-like operations supported by {@link ReadTransaction} and {@link ReadWriteTransaction}. This interface defines
 * the operations without a tie-in with lifecycle management.
 */
public interface QueryOperations<D extends Datastore> {
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
