/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ContainsStringMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.StringLeafListMatchBuilder;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultStringLeafListMatchBuilder<T extends DataObject> extends AbstractLeafListValueMatchBuilder<T, String>
        implements StringLeafListMatchBuilder<T> {
    DefaultStringLeafListMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ContainsStringMatchBuilder<T> contains() {
        return new DefaultContainsStringMatchBuilder<>(this);
    }
}
