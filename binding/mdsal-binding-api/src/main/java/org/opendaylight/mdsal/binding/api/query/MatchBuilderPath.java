/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@Beta
public interface MatchBuilderPath<R, T> {

    <C extends ChildOf<? super T>> MatchBuilderPath<R, C> childObject(Class<C> childClass);

    <F> MatchBuilderValue<R, F> leaf(LeafReference<R, F> methodRef);

    MatchBuilderValueString<R> leaf(StringLeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Byte> leaf(Int8LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Short> leaf(Int16LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Integer> leaf(Int32LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Long> leaf(Int64LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Uint8> leaf(Uint8LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Uint16> leaf(Uint16LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Uint32> leaf(Uint32LeafReference<R> methodRef);

    MatchBuilderValueComparable<R, Uint64> leaf(Uint64LeafReference<R> methodRef);

    @FunctionalInterface
    public interface LeafReference<P, C> {

        C getLeaf(P parent);
    }

    @FunctionalInterface
    public interface Int8LeafReference<P> extends LeafReference<P, Byte> {

    }

    @FunctionalInterface
    public interface Int16LeafReference<P> extends LeafReference<P, Short> {

    }

    @FunctionalInterface
    public interface Int32LeafReference<P> extends LeafReference<P, Integer> {

    }

    @FunctionalInterface
    public interface Int64LeafReference<P> extends LeafReference<P, Long> {

    }

    @FunctionalInterface
    public interface StringLeafReference<P> extends LeafReference<P, String> {

    }

    @FunctionalInterface
    public interface Uint8LeafReference<P> extends LeafReference<P, Uint8> {

    }

    @FunctionalInterface
    public interface Uint16LeafReference<P> extends LeafReference<P, Uint16> {

    }

    @FunctionalInterface
    public interface Uint32LeafReference<P> extends LeafReference<P, Uint32> {

    }

    @FunctionalInterface
    public interface Uint64LeafReference<P> extends LeafReference<P, Uint64> {

    }

}
