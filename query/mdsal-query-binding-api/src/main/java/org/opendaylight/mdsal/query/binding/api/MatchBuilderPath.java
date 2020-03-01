/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.api;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@Beta
public interface MatchBuilderPath<O extends DataObject, T extends DataObject> extends Mutable {

    <C extends ChildOf<? super T>> MatchBuilderPath<O, C> childObject(Class<C> childClass);

    MatchBuilderValue<O, Empty> leaf(EmptyLeafReference<T> methodRef);

    MatchBuilderValueString<O> leaf(StringLeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Byte> leaf(Int8LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Short> leaf(Int16LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Integer> leaf(Int32LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Long> leaf(Int64LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Uint8> leaf(Uint8LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Uint16> leaf(Uint16LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Uint32> leaf(Uint32LeafReference<T> methodRef);

    MatchBuilderValueComparable<O, Uint64> leaf(Uint64LeafReference<T> methodRef);

    <I extends BaseIdentity> MatchBuilderValue<O, I> leaf(IdentityLeafReference<T, I> methodRef);

    <C extends TypeObject> MatchBuilderValue<O, C> leaf(TypeObjectLeafReference<T, C> methodRef);

    /**
     * Base interface for capturing binding getter method references through lambda expressions. This interface should
     * never be used directly, but rather through one of its specializations.
     *
     * <p>
     * This interface uncharacteristically extends {@link Serializable} for the purposes of making the resulting lambda
     * also Serializable. This part is critical to introspecting the lambda and identifying the method being invoked.
     *
     * @param <P> Parent type
     * @param <C> Child type
     */
    @FunctionalInterface
    public interface LeafReference<P, C> extends Serializable {
        /**
         * Dummy method to express the method signature of a typical getter. This method
         *
         * @param parent Parent object
         * @return Leaf value
         * @deprecated This method is present only for technical realization of taking the method reference and should
         *             never be involved directly.
         */
        @Deprecated(forRemoval = true)
        C dummyMethod(P parent);
    }

    @FunctionalInterface
    public interface EmptyLeafReference<P> extends LeafReference<P, Empty> {

    }

    @FunctionalInterface
    public interface StringLeafReference<P> extends LeafReference<P, String> {

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

    @FunctionalInterface
    public interface IdentityLeafReference<P, T extends BaseIdentity> extends LeafReference<P, T> {

    }

    @FunctionalInterface
    public interface TypeObjectLeafReference<P, T extends TypeObject> extends LeafReference<P, T> {

    }
}
