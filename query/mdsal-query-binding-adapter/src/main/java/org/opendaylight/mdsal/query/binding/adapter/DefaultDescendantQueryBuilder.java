/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.query.binding.api.DescendantQuery;
import org.opendaylight.mdsal.query.binding.api.DescendantQueryBuilder;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderPath;
import org.opendaylight.mdsal.query.binding.api.QueryStructureException;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

final class DefaultDescendantQueryBuilder<R extends DataObject, T extends DataObject>
        implements DescendantQueryBuilder<T> {
    private final InstanceIdentifierBuilder<T> childPath;
    private final AdaptingQueryBuilder builder;

    DefaultDescendantQueryBuilder(final AdaptingQueryBuilder builder, final InstanceIdentifierBuilder<T> childPath) {
        this.builder = requireNonNull(builder);
        this.childPath = requireNonNull(childPath);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ChildOf<? super T>> DescendantQueryBuilder<C> extractChild(final Class<C> childClass) {
        childPath.child(childClass);
        return (DefaultDescendantQueryBuilder<R, C>) this;
    }

    @Override
    public MatchBuilderPath<T> matching() {
        builder.setSelectPath(childPath.build());
        return new DefaultMatchBuilderPath<>(builder, childPath);
    }

    @Override
    public DescendantQuery<T> build() throws QueryStructureException {
        builder.setSelectPath(childPath.build());
        return new DefaultDescendantQuery<>(builder);
    }
}
