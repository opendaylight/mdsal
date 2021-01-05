/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsComparableMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueNodeCodecContext;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.binding.DataObject;

// FIXME: this is a dead ringer DefaultComparableMatchBuilder
final class DefaultContainsComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends DefaultContainsMatchBuilder<T, V> implements ContainsComparableMatchBuilder<T, V> {
    DefaultContainsComparableMatchBuilder(final DefaultLeafListComparableMatchBuilder<T, V> builder,
        final ValueNodeCodecContext valueCodec, final Operator combine) {
        super(builder, valueCodec, combine);
    }

    @Override
    public ValueMatch<T> lessThan(final V value) {
        return matchingItems(Match.lessThan(value));
    }

    @Override
    public ValueMatch<T> lessThanOrEqual(final V value) {
        return matchingItems(Match.lessThanOrEqual(value));
    }

    @Override
    public ValueMatch<T> greaterThan(final V value) {
        return matchingItems(Match.greaterThan(value));
    }

    @Override
    public ValueMatch<T> greaterThanOrEqual(final V value) {
        return matchingItems(Match.greaterThanOrEqual(value));
    }
}
