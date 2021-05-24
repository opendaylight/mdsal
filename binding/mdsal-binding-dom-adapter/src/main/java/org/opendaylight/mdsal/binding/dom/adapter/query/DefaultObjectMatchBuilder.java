/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultObjectMatchBuilder<T extends DataObject, V extends ChildOf<T>>
    extends AbstractObjectMatchBuilder<T, V> {

    DefaultObjectMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public final ValueMatch<T> valueEquals(final V object) {
        return withMatch(Match.objectEquals(translateObject(object)));
    }

    private Object translateObject(final V object) {
        return getObjectCodec().serialize(object);
    }
}
