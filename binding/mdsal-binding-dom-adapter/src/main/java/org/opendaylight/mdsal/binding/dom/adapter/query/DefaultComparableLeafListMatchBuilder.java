/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ComparableLeafListMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ContainsComparableMatchBuilder;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultComparableLeafListMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends AbstractLeafListValueMatchBuilder<T, V> implements ComparableLeafListMatchBuilder<T, V> {
    DefaultComparableLeafListMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ContainsComparableMatchBuilder<T, V> contains() {
        return new DefaultContainsComparableMatchBuilder<>(this);
    }
}
