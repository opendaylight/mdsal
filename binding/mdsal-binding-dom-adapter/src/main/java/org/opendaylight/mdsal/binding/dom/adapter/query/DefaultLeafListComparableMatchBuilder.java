/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsComparableMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.LeafListComparableMatchBuilder;
import org.opendaylight.mdsal.binding.dom.adapter.query.DefaultContainsMatchBuilder.Operator;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultLeafListComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends AbstractLeafListMatchBuilder<T, V> implements LeafListComparableMatchBuilder<T, V> {
    DefaultLeafListComparableMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ContainsComparableMatchBuilder<T, V> contains() {
        return new DefaultContainsComparableMatchBuilder<>(this, getValueCodec(), Operator.ITEM);
    }

    @Override
    public ContainsComparableMatchBuilder<T, V> allMatch() {
        return new DefaultContainsComparableMatchBuilder<>(this, getValueCodec(), Operator.ALL);
    }

    @Override
    public ContainsComparableMatchBuilder<T, V> anyMatch() {
        return new DefaultContainsComparableMatchBuilder<>(this, getValueCodec(), Operator.ANY);
    }

    @Override
    public ContainsComparableMatchBuilder<T, V> noneMatch() {
        return new DefaultContainsComparableMatchBuilder<>(this, getValueCodec(), Operator.NONE);
    }
}
