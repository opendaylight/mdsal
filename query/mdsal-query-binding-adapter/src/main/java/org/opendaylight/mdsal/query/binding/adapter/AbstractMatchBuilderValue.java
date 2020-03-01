/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.query.binding.adapter.LambdaDecoder.LambdaTarget;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderValue;
import org.opendaylight.mdsal.query.binding.api.ValueMatch;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.Exists;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.ValueEquals;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

abstract class AbstractMatchBuilderValue<R extends DataObject, O extends DataObject, T extends DataObject, V>
        implements MatchBuilderValue<T, V> {
    private final AdaptingQueryBuilder builder;

    private final InstanceIdentifier<T> targetPath;
    private final LambdaTarget targetLeaf;

    AbstractMatchBuilderValue(final AdaptingQueryBuilder builder, final InstanceIdentifier<T> targetPath,
            final LambdaTarget targetLeaf) {
        this.builder = requireNonNull(builder);
        this.targetPath = requireNonNull(targetPath);
        this.targetLeaf = requireNonNull(targetLeaf);
    }

    @Override
    public final ValueMatch<T> nonNull() {
        return withPredicate(new Exists());
    }

    @Override
    public final ValueMatch<T> valueEquals(final V value) {
        return withPredicate(new ValueEquals<>(value));
    }

    final ValueMatch<T> withPredicate(final DOMQueryPredicate predicate) {
        // FIXME: convert path to target and add predicate
        return null;
    }
}
