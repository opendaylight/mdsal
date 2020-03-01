/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi.query;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderValue;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderValueComparable;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderValueString;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * @author Robert Varga
 *
 */
final class DefaultMatchBuilderPath<R extends DataObject, O extends DataObject, T extends DataObject>
        implements MatchBuilderPath<T> {
    private final InstanceIdentifier<O> childPath;
    private final InstanceIdentifier<R> rootPath;
    private final InstanceIdentifierBuilder<T> target;

    DefaultMatchBuilderPath(final InstanceIdentifier<R> rootPath, final InstanceIdentifier<O> childPath,
            final InstanceIdentifierBuilder<T> target) {
        this.rootPath = requireNonNull(rootPath);
        this.childPath = requireNonNull(childPath);
        this.target = requireNonNull(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ChildOf<? super T>> MatchBuilderPath<C> childObject(final Class<C> childClass) {
        target.child(childClass);
        return (MatchBuilderPath<C>) this;
    }

    @Override
    public <F> MatchBuilderValue<T, F> leaf(final LeafReference<T, F> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueString<T> leaf(final StringLeafReference<T> methodRef) {
        return new DefaultMatchBuilderValueString<>(rootPath, childPath, target.build(),
                LambdaDecoder.resolveLambda(methodRef));
    }

    @Override
    public MatchBuilderValueComparable<T, Byte> leaf(final Int8LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Short> leaf(final Int16LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Integer> leaf(final Int32LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Long> leaf(final Int64LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Uint8> leaf(final Uint8LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Uint16> leaf(final Uint16LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Uint32> leaf(final Uint32LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchBuilderValueComparable<T, Uint64> leaf(final Uint64LeafReference<T> methodRef) {
        // TODO Auto-generated method stub
        return null;
    }

}
