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

/**
 * A value-based match executed from some point in the data tree.
 *
 * @param <T> query result type
 */
@Beta
public interface ValueMatch<T extends DataObject> extends StructuralBuilder<QueryExpression<T>> {
    /**
     * Start chaining an additional match for the query. Query results are guaranteed both this match and that
     * additional match at the same time.
     *
     * @return A {@link MatchBuilderPath}
     */
    @NonNull MatchBuilderPath<T, T> and();
}
