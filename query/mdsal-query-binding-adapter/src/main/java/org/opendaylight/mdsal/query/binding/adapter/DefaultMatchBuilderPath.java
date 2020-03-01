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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class DefaultMatchBuilderPath<O extends DataObject, T extends DataObject> implements MatchBuilderPath<O, T> {
    private final QueryBuilderState builder;
    private final InstanceIdentifier<O> select;
    private final InstanceIdentifierBuilder<T> target;

    DefaultMatchBuilderPath(final QueryBuilderState builder, final InstanceIdentifier<O> select,
            final InstanceIdentifierBuilder<T> target) {
        this.builder = requireNonNull(builder);
        this.select = requireNonNull(select);
        this.target = requireNonNull(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ChildOf<? super T>> MatchBuilderPath<O, C> childObject(final Class<C> childClass) {
        target.child(childClass);
        return (MatchBuilderPath<O, C>) this;
    }

    @Override
    public MatchBuilderValue<O, Empty> leaf(final EmptyLeafReference<T> methodRef) {
        // FIXME: implement this
        throw new UnsupportedOperationException("IMPLEMENT THIS");
    }

    @Override
    public MatchBuilderValueString<O> leaf(final StringLeafReference<T> methodRef) {
        return new DefaultMatchBuilderValueString<>(builder, select, builder.bindMethod(target.build(), methodRef));
    }

    @Override
    public MatchBuilderValueComparable<O, Byte> leaf(final Int8LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Short> leaf(final Int16LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Integer> leaf(final Int32LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Long> leaf(final Int64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Uint8> leaf(final Uint8LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Uint16> leaf(final Uint16LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Uint32> leaf(final Uint32LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public MatchBuilderValueComparable<O, Uint64> leaf(final Uint64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public <I extends BaseIdentity> MatchBuilderValue<O, I> leaf(final IdentityLeafReference<T, I> methodRef) {
        // FIXME: implement this
        throw new UnsupportedOperationException("IMPLEMENT THIS");
    }

    @Override
    public <C extends TypeObject> MatchBuilderValue<O, C> leaf(final TypeObjectLeafReference<T, C> methodRef) {
        // FIXME: implement this
        throw new UnsupportedOperationException("IMPLEMENT THIS");
    }

    private <F extends Comparable<F>> MatchBuilderValueComparable<O, F> comparableFor(final LeafReference<T, F> ref) {
        return new DefaultMatchBuilderValueComparable<>(builder, select, builder.bindMethod(target.build(), ref));
    }
}
