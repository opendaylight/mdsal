/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.LeafListValueMatchBuilder;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultLeafListValueMatchBuilder<T extends DataObject, V>
        extends AbstractLeafListValueMatchBuilder<T, V> implements LeafListValueMatchBuilder<T, V> {
    DefaultLeafListValueMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ContainsMatchBuilder<T, V> contains() {
        return new DefaultContainsMatchBuilder<>(this);
    }
}
