/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import org.opendaylight.mdsal.query.binding.adapter.QueryBuilderState.BoundMethod;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderValueComparable;
import org.opendaylight.mdsal.query.binding.api.ValueMatch;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.GreaterThan;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.GreaterThanOrEqual;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.LessThan;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.LessThanOrEqual;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultMatchBuilderValueComparable<T extends DataObject, V extends Comparable<V>>
        extends AbstractMatchBuilderValue<T, V> implements MatchBuilderValueComparable<T, V> {
    DefaultMatchBuilderValueComparable(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ValueMatch<T> lessThan(final V value) {
        return withPredicate(new LessThan<>(relativePath(), value));
    }

    @Override
    public ValueMatch<T> lessThanOrEqual(final V value) {
        return withPredicate(new LessThanOrEqual<>(relativePath(), value));
    }

    @Override
    public ValueMatch<T> greaterThan(final V value) {
        return withPredicate(new GreaterThan<>(relativePath(), value));
    }

    @Override
    public ValueMatch<T> greaterThanOrEqual(final V value) {
        return withPredicate(new GreaterThanOrEqual<>(relativePath(), value));
    }
}
