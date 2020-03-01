/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.query.binding.api.MatchBuilderPath;
import org.opendaylight.mdsal.query.binding.api.Query;
import org.opendaylight.mdsal.query.binding.api.ValueMatch;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultValueMatch<T extends DataObject> implements ValueMatch<T> {
    private final AdaptingQueryBuilder builder;

    DefaultValueMatch(final AdaptingQueryBuilder builder) {
        this.builder = requireNonNull(builder);
    }

    @Override
    public MatchBuilderPath<T> and() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Query<T> build() {
        return builder.buildQuery();
    }
}
