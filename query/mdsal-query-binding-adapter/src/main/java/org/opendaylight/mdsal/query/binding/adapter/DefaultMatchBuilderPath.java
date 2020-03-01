/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.query.binding.api.MatchBuilderPath;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderValue;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderValueComparable;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderValueString;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class DefaultMatchBuilderPath<R extends DataObject, O extends DataObject, T extends DataObject>
        implements MatchBuilderPath<T> {
    private final AdaptingQueryBuilder builder;
    private final InstanceIdentifierBuilder<T> target;

    DefaultMatchBuilderPath(final AdaptingQueryBuilder builder, final InstanceIdentifierBuilder<T> target) {
        this.builder = requireNonNull(builder);
        this.target = requireNonNull(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ChildOf<? super T>> MatchBuilderPath<C> childObject(final Class<C> childClass) {
        target.child(childClass);
        return (MatchBuilderPath<C>) this;
    }

    @Override
    public MatchBuilderValue<T, Empty> leaf(final EmptyLeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueString<T> leaf(final StringLeafReference<T> methodRef) {
        return new DefaultMatchBuilderValueString<>(builder, builder.bindMethod(target.build(), methodRef));
    }

    @Override
    public MatchBuilderValueComparable<T, Byte> leaf(final Int8LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Short> leaf(final Int16LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Integer> leaf(final Int32LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Long> leaf(final Int64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Uint8> leaf(final Uint8LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Uint16> leaf(final Uint16LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Uint32> leaf(final Uint32LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<T, Uint64> leaf(final Uint64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public <I extends BaseIdentity> MatchBuilderValue<T, I> leaf(final IdentityLeafReference<T, I> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <C extends TypeObject> MatchBuilderValue<T, C> leaf(final TypeObjectLeafReference<T, C> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    private <F extends Comparable<F>> MatchBuilderValueComparable<T, F> comparableFor(final LeafReference<T, F> ref) {
        return new DefaultMatchBuilderValueComparable<>(builder, builder.bindMethod(target.build(), ref));
    }
}
