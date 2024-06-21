/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultValueMatch<T extends DataObject> implements ValueMatch<T> {
    private final QueryBuilderState builder;
    private final InstanceIdentifier<T> select;

    DefaultValueMatch(final QueryBuilderState builder, final InstanceIdentifier<T> select) {
        this.builder = requireNonNull(builder);
        this.select = requireNonNull(select);
    }

    @Override
    public MatchBuilderPath<T, T> and() {
        return new DefaultMatchBuilderPath<>(builder, select, select.builder());
    }

    @Override
    public QueryExpression<T> build() {
        return builder.buildQuery();
    }
}
