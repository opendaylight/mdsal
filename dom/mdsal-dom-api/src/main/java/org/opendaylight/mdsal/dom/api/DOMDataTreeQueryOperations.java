/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;

/**
 * Query-like operations supported by {@link DOMDataTreeReadTransaction} and {@link DOMDataTreeReadWriteTransaction}.
 * This interface defines the operations without a tie-in with lifecycle management.
 */
@Beta
public interface DOMDataTreeQueryOperations {
    /**
     * Executes a query on the provided logical data store.
     *
     * @param store Logical data store from which read should occur.
     * @param query DOMQuery to execute
     * @return a FluentFuture containing the result of the query. The Future blocks until the operation is complete.
     *         Once complete:
     *         <ul>
     *           <li>The Future returns the result of the query</li>
     *           <li>If the query execution fails, the Future will fail with a {@link ReadFailedException} or
     *               an exception derived from ReadFailedException.
     *            </li>
     *         </ul>
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws IllegalArgumentException if the {@code query} or {@code store} is not supported
     */
    FluentFuture<DOMQueryResult> execute(LogicalDatastoreType store, DOMQuery query);
}
