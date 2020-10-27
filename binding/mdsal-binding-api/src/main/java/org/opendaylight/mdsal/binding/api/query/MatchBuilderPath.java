/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@Beta
public interface MatchBuilderPath<O extends DataObject, T extends DataObject> extends Mutable {
    /**
     * Descend match into a child container.
     *
     * @param <N> Child container type
     * @param childClass Child container type class
     * @return This builder
     * @throws NullPointerException if childClass is null
     */
    <N extends ChildOf<? super T>> @NonNull MatchBuilderPath<O, N> childObject(Class<N> childClass);

    /**
     * Descend match into a child container in a particular case.
     *
     * @param <C> Case type
     * @param <N> Child container type
     * @param childClass Child container type class
     * @return This builder
     * @throws NullPointerException if any argument is null
     */
    <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
        @NonNull MatchBuilderPath<O, N> extractChild(Class<C> caseClass, Class<N> childClass);

    /**
     * Add a child path component to the specification of what needs to be extracted, specifying an exact match in
     * a keyed list. This method, along with its alternatives, can be used to specify which object type to select from
     * the root path.
     *
     * @param <N> List type
     * @param <K> Key type
     * @param listKey List key
     * @return This builder
     * @throws NullPointerException if childClass is null
     */
    <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
        @NonNull MatchBuilderPath<O, N> extractChild(final Class<@NonNull N> listItem, final K listKey);

    /**
     * Match an {@code empty} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ValueMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ValueMatchBuilder<O, Empty> leaf(EmptyLeafReference<T> methodRef);

    /**
     * Match an {@code string} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link StringMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull StringMatchBuilder<O> leaf(StringLeafReference<T> methodRef);

    /**
     * Match an {@code int8} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Byte> leaf(Int8LeafReference<T> methodRef);

    /**
     * Match an {@code int16} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Short> leaf(Int16LeafReference<T> methodRef);

    /**
     * Match an {@code int32} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Integer> leaf(Int32LeafReference<T> methodRef);

    /**
     * Match an {@code int64} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Long> leaf(Int64LeafReference<T> methodRef);

    /**
     * Match an {@code uint8} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Uint8> leaf(Uint8LeafReference<T> methodRef);

    /**
     * Match an {@code uint16} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Uint16> leaf(Uint16LeafReference<T> methodRef);

    /**
     * Match an {@code uint32} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Uint32> leaf(Uint32LeafReference<T> methodRef);

    /**
     * Match an {@code uint64} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ComparableMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    @NonNull ComparableMatchBuilder<O, Uint64> leaf(Uint64LeafReference<T> methodRef);

    /**
     * Match an {@code identityref} leaf's value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ValueMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    <I extends BaseIdentity> @NonNull ValueMatchBuilder<O, I> leaf(IdentityLeafReference<T, I> methodRef);

    /**
     * Match a generic leaf value.
     *
     * @param methodRef method reference to the getter method
     * @return A {@link ValueMatchBuilder}
     * @throws NullPointerException if methodRef is null
     */
    <C extends TypeObject> @NonNull ValueMatchBuilder<O, C> leaf(TypeObjectLeafReference<T, C> methodRef);

    /**
     * Base interface for capturing binding getter method references through lambda expressions. This interface should
     * never be used directly, but rather through one of its specializations.
     *
     * <p>
     * This interface uncharacteristically extends {@link Serializable} for the purposes of making the resulting lambda
     * also Serializable. This part is critical for the process of introspection into the lambda and identifying the
     * method being invoked.
     *
     * @param <P> Parent type
     * @param <C> Child type
     */
    @FunctionalInterface
    public interface LeafReference<P, C> extends Serializable {
        /**
         * Dummy method to express the method signature of a typical getter. Due to this match we can match any Java
         * method reference which takes in {@code parent} and results in {@code child} -- expose the feature of using
         * {@code Parent::getChild} method shorthand.
         *
         * @param parent Parent object
         * @return Leaf value
         * @deprecated This method is present only for technical realization of taking the method reference and should
         *             never be involved directly. See {@code LambdaDecoder} for gory details.
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
