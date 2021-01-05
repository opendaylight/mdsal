/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsComparableMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultContainsComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends DefaultContainsMatchBuilder<T, V> implements ContainsComparableMatchBuilder<T, V> {

    DefaultContainsComparableMatchBuilder(final DefaultComparableLeafListMatchBuilder<T, V> builder) {
        super(builder);
    }

    @Override
    public ValueMatch<T> lessThan(final V value) {
        // FIXME: implement this
        return matching(null);
    }

    @Override
    public ValueMatch<T> lessThanOrEqual(final V value) {
        // FIXME: implement this
        return matching(null);
    }

    @Override
    public ValueMatch<T> greaterThan(final V value) {
        // FIXME: implement this
        return matching(null);
    }

    @Override
    public ValueMatch<T> greaterThanOrEqual(final V value) {
        // FIXME: implement this
        return matching(null);
    }
}
