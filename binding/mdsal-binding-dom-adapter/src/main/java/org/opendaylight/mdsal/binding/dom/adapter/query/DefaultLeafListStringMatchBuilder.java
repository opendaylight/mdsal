/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsStringMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.LeafListStringMatchBuilder;
import org.opendaylight.mdsal.binding.dom.adapter.query.DefaultContainsMatchBuilder.Operator;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultLeafListStringMatchBuilder<T extends DataObject> extends AbstractLeafListMatchBuilder<T, String>
        implements LeafListStringMatchBuilder<T> {
    DefaultLeafListStringMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ContainsStringMatchBuilder<T> contains() {
        return new DefaultContainsStringMatchBuilder<>(this, getValueCodec(), Operator.ITEM);
    }

    @Override
    public ContainsStringMatchBuilder<T> allMatch() {
        return new DefaultContainsStringMatchBuilder<>(this, getValueCodec(), Operator.ALL);
    }

    @Override
    public ContainsStringMatchBuilder<T> anyMatch() {
        return new DefaultContainsStringMatchBuilder<>(this, getValueCodec(), Operator.ANY);
    }

    @Override
    public ContainsStringMatchBuilder<T> noneMatch() {
        return new DefaultContainsStringMatchBuilder<>(this, getValueCodec(), Operator.NONE);
    }
}
