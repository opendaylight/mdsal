/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsMatchBuilder;
import org.opendaylight.mdsal.binding.dom.adapter.query.DefaultContainsMatchBuilder.Operator;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultLeafListMatchBuilder<T extends DataObject, V> extends AbstractLeafListMatchBuilder<T, V> {
    DefaultLeafListMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ContainsMatchBuilder<T, V> contains() {
        return new DefaultContainsMatchBuilder<>(this, getValueCodec(), Operator.ITEM);
    }

    @Override
    public ContainsMatchBuilder<T, V> allMatch() {
        return new DefaultContainsMatchBuilder<>(this, getValueCodec(), Operator.ALL);
    }

    @Override
    public ContainsMatchBuilder<T, V> anyMatch() {
        return new DefaultContainsMatchBuilder<>(this, getValueCodec(), Operator.ANY);
    }

    @Override
    public ContainsMatchBuilder<T, V> noneMatch() {
        return new DefaultContainsMatchBuilder<>(this, getValueCodec(), Operator.NONE);
    }
}
