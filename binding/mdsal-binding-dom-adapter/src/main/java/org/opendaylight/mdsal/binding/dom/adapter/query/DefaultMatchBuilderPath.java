/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.ComparableMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath;
import org.opendaylight.mdsal.binding.api.query.StringMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatchBuilder;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class DefaultMatchBuilderPath<O extends DataObject, T extends DataObject> implements MatchBuilderPath<O, T> {
    private final QueryBuilderState builder;
    private final InstanceIdentifier<O> select;
    private final InstanceIdentifier.Builder<T> target;

    DefaultMatchBuilderPath(final QueryBuilderState builder, final InstanceIdentifier<O> select,
            final InstanceIdentifier.Builder<T> target) {
        this.builder = requireNonNull(builder);
        this.select = requireNonNull(select);
        this.target = requireNonNull(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends ChildOf<? super T>> MatchBuilderPath<O, N> childObject(final Class<N> childClass) {
        target.child(childClass);
        return (MatchBuilderPath<O, N>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
            MatchBuilderPath<O, N> extractChild(final Class<C> caseClass, final Class<N> childClass) {
        target.child(caseClass, childClass);
        return (MatchBuilderPath<O, N>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
            MatchBuilderPath<O, N> extractChild(final Class<@NonNull N> listItem, final K listKey) {
        target.child(listItem, listKey);
        return (MatchBuilderPath<O, N>) this;
    }

    @Override
    public ValueMatchBuilder<O, Boolean> leaf(final BooleanLeafReference<T> methodRef) {
        return defaultFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Decimal64> leaf(final Decimal64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ValueMatchBuilder<O, Empty> leaf(final EmptyLeafReference<T> methodRef) {
        return defaultFor(methodRef);
    }

    @Override
    public StringMatchBuilder<O> leaf(final StringLeafReference<T> methodRef) {
        return new DefaultStringMatchBuilder<>(builder, select, builder.bindMethod(target.build(), methodRef));
    }

    @Override
    public ComparableMatchBuilder<O, Byte> leaf(final Int8LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Short> leaf(final Int16LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Integer> leaf(final Int32LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Long> leaf(final Int64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Uint8> leaf(final Uint8LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Uint16> leaf(final Uint16LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Uint32> leaf(final Uint32LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public ComparableMatchBuilder<O, Uint64> leaf(final Uint64LeafReference<T> methodRef) {
        return comparableFor(methodRef);
    }

    @Override
    public <I extends BaseIdentity> ValueMatchBuilder<O, I> leaf(final IdentityLeafReference<T, I> methodRef) {
        return defaultFor(methodRef);
    }

    @Override
    public <C extends TypeObject> ValueMatchBuilder<O, C> leaf(final TypeObjectLeafReference<T, C> methodRef) {
        return defaultFor(methodRef);
    }

    private <F> @NonNull ValueMatchBuilder<O, F> defaultFor(final LeafReference<T, F> ref) {
        return new DefaultValueMatchBuilder<>(builder, select, builder.bindMethod(target.build(), ref));
    }

    private <F extends Comparable<F>> @NonNull ComparableMatchBuilder<O, F> comparableFor(
            final LeafReference<T, F> ref) {
        return new DefaultComparableMatchBuilder<>(builder, select, builder.bindMethod(target.build(), ref));
    }
}
