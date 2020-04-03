/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yangtools.yang.binding.DataObject;

@Beta
public interface QueryExecutor {

    <T extends DataObject> ListenableFuture<? extends QueryResult<T>> executeQuery(QueryExpression<T> query);
}
