/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ComparableMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.GreaterThan;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.GreaterThanOrEqual;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.LessThan;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.LessThanOrEqual;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends AbstractValueMatchBuilder<T, V> implements ComparableMatchBuilder<T, V> {
    DefaultComparableMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
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
