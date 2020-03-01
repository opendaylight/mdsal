/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Beta
public interface QueryFactory {

    <T extends DataObject> @NonNull QueryRoot<T> newQueryRoot(InstanceIdentifier<T> rootPath);

    <T extends DataObject> @NonNull QueryResultTypeBuilder<T, T> newQueryResultTypeBuilder(QueryRoot<T> root);

    <R extends DataObject, T extends DataObject> @NonNull Query<T> newQuery(QueryRoot<R> root,
            QueryResultType<R, T> result);
}
