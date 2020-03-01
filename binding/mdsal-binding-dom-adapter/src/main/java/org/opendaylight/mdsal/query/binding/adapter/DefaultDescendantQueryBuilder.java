/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.query.DescendantQueryBuilder;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryStructureException;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

final class DefaultDescendantQueryBuilder<R extends DataObject, T extends DataObject>
        implements DescendantQueryBuilder<T> {
    private final InstanceIdentifierBuilder<T> childPath;
    private final QueryBuilderState builder;

    DefaultDescendantQueryBuilder(final QueryBuilderState builder, final InstanceIdentifierBuilder<T> childPath) {
        this.builder = requireNonNull(builder);
        this.childPath = requireNonNull(childPath);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends ChildOf<? super T>> DescendantQueryBuilder<N> extractChild(final Class<N> childClass) {
        childPath.child(childClass);
        return (DefaultDescendantQueryBuilder<R, N>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
            DescendantQueryBuilder<N> extractChild(final Class<C> caseClass, final Class<N> childClass) {
        childPath.child(caseClass, childClass);
        return (DefaultDescendantQueryBuilder<R, N>) this;
    }

    @Override
    public MatchBuilderPath<T, T> matching() {
        final InstanceIdentifier<T> selectPath = childPath.build();
        builder.setSelectPath(selectPath);
        return new DefaultMatchBuilderPath<>(builder, selectPath, childPath);
    }

    @Override
    public QueryExpression<T> build() throws QueryStructureException {
        builder.setSelectPath(childPath.build());
        return builder.buildQuery();
    }
}
