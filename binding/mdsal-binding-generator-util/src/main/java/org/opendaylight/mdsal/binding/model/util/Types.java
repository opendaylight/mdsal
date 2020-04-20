/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.AbstractBaseType;
import org.opendaylight.mdsal.binding.model.api.BaseTypeWithRestrictions;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.WildcardType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

public final class Types {
    private static final CacheLoader<Class<?>, ConcreteType> TYPE_LOADER = new CacheLoader<>() {
        @Override
        public ConcreteType load(final Class<?> key) {
            return new ConcreteTypeImpl(JavaTypeName.create(key), null);
        }
    };
    private static final LoadingCache<Class<?>, ConcreteType> TYPE_CACHE =
            CacheBuilder.newBuilder().weakKeys().build(TYPE_LOADER);

    public static final @NonNull ConcreteType BOOLEAN = typeForClass(Boolean.class);
    public static final @NonNull ConcreteType STRING = typeForClass(String.class);
    public static final @NonNull ConcreteType VOID = typeForClass(Void.class);
    public static final @NonNull ConcreteType BYTE_ARRAY = typeForClass(byte[].class);

    private static final @NonNull ConcreteType BUILDER = typeForClass(Builder.class);
    private static final @NonNull ConcreteType CLASS = typeForClass(Class.class);
    private static final @NonNull ConcreteType LIST_TYPE = typeForClass(List.class);
    private static final @NonNull ConcreteType LISTENABLE_FUTURE = typeForClass(ListenableFuture.class);
    private static final @NonNull ConcreteType MAP_TYPE = typeForClass(Map.class);
    private static final @NonNull ConcreteType OBJECT = typeForClass(Object.class);
    private static final @NonNull ConcreteType PRIMITIVE_VOID = typeForClass(void.class);
    private static final @NonNull ConcreteType SERIALIZABLE = typeForClass(Serializable.class);
    private static final @NonNull ConcreteType SET_TYPE = typeForClass(Set.class);

    /**
     * It is not desirable to create instance of this class.
     */
    private Types() {
    }

    /**
     * Returns an instance of {@link ParameterizedType} which represents JAVA <code>java.lang.Class</code> type
     * specialized to specified type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Class<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static @NonNull ParameterizedType classType(final Type type) {
        return parameterizedTypeFor(CLASS, type);
    }

    /**
     * Returns an instance of {@link ConcreteType} which represents JAVA <code>java.lang.Void</code> type.
     *
     * @return <code>ConcreteType</code> instance which represents JAVA <code>java.lang.Void</code>
     */
    public static @NonNull ConcreteType voidType() {
        return VOID;
    }

    /**
     * Returns an instance of {@link ConcreteType} which represents {@link Object} type.
     *
     * @return <code>ConcreteType</code> instance which represents {@link Object}
     */
    public static @NonNull ConcreteType objectType() {
        return OBJECT;
    }

    /**
     * Returns an instance of {@link ConcreteType} which represents JAVA <code>void</code> type.
     *
     * @return <code>ConcreteType</code> instance which represents JAVA <code>void</code>
     */
    public static @NonNull ConcreteType primitiveVoidType() {
        return PRIMITIVE_VOID;
    }

    /**
     * Returns an instance of {@link ConcreteType} which represents {@link Serializable} type.
     *
     * @return <code>ConcreteType</code> instance which represents JAVA <code>{@link Serializable}</code>
     */
    public static @NonNull ConcreteType serializableType() {
        return SERIALIZABLE;
    }

    /**
     * Returns an instance of {@link ConcreteType} describing the class.
     *
     * @param cls Class to describe
     * @return Description of class
     */
    public static @NonNull ConcreteType typeForClass(final @NonNull Class<?> cls) {
        return TYPE_CACHE.getUnchecked(cls);
    }

    public static @NonNull ConcreteType typeForClass(final @NonNull Class<?> cls,
            final @Nullable Restrictions restrictions) {
        return restrictions == null ? typeForClass(cls) : restrictedType(JavaTypeName.create(cls), restrictions);
    }

    @Beta
    public static @NonNull ConcreteType restrictedType(final Type type, final @NonNull Restrictions restrictions) {
        return restrictedType(type.getIdentifier(), restrictions);
    }

    private static @NonNull ConcreteType restrictedType(final JavaTypeName identifier,
            final @NonNull Restrictions restrictions) {
        return restrictions instanceof DefaultRestrictions ? new ConcreteTypeImpl(identifier, restrictions)
                : new BaseTypeWithRestrictionsImpl(identifier, restrictions);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link Map}&lt;K,V&gt;.
     *
     * @param keyType Key Type
     * @param valueType Value Type
     * @return Description of generic type instance
     */
    public static @NonNull ParameterizedType mapTypeFor(final Type keyType, final Type valueType) {
        return parameterizedTypeFor(MAP_TYPE, keyType, valueType);
    }

    public static boolean isMapType(final ParameterizedType type) {
        return MAP_TYPE.equals(type.getRawType());
    }

    public static boolean isMapType(final Type type) {
        return type instanceof ParameterizedType && isMapType((ParameterizedType) type);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link Set}&lt;V&gt; with concrete type
     * of value.
     *
     * @param valueType Value Type
     * @return Description of generic type instance of Set
     */
    public static @NonNull ParameterizedType setTypeFor(final Type valueType) {
        return parameterizedTypeFor(SET_TYPE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link List}&lt;V&gt; with concrete type
     * of value.
     *
     * @param valueType Value Type
     * @return Description of type instance of List
     */
    public static @NonNull ParameterizedType listTypeFor(final Type valueType) {
        return parameterizedTypeFor(LIST_TYPE, valueType);
    }

    public static boolean isListType(final ParameterizedType type) {
        return LIST_TYPE.equals(type.getRawType());
    }

    public static boolean isListType(final Type type) {
        return type instanceof ParameterizedType && isListType((ParameterizedType) type);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link ListenableFuture}&lt;V&gt;
     * with concrete type of value.
     *
     * @param valueType Value Type
     * @return Description of type instance of ListenableFuture
     */
    public static @NonNull ParameterizedType listenableFutureTypeFor(final Type valueType) {
        return parameterizedTypeFor(LISTENABLE_FUTURE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link Builder}&lt;V&gt; with concrete type of value.
     *
     * @param valueType Value Type
     * @return Description of type instance of Builder
     */
    public static @NonNull ParameterizedType builderTypeFor(final Type valueType) {
        return parameterizedTypeFor(BUILDER, valueType);
    }

    /**
     * Creates instance of type {@link org.opendaylight.mdsal.binding.model.api.ParameterizedType ParameterizedType}.
     *
     * @param type JAVA <code>Type</code> for raw type
     * @param parameters JAVA <code>Type</code>s for actual parameter types
     * @return <code>ParametrizedType</code> representation of <code>type</code> and its <code>parameters</code>
     * @throws NullPointerException if any argument or any member of {@code parameters} is null
     */
    public static @NonNull ParameterizedType parameterizedTypeFor(final Type type, final Type... parameters) {
        return new ParametrizedTypeImpl(type, parameters);
    }

    /**
     * Creates instance of type {@link org.opendaylight.mdsal.binding.model.api.WildcardType}.
     *
     * @param identifier JavaTypeName of the type
     * @return <code>WildcardType</code> representation of specified identifier
     */
    public static WildcardType wildcardTypeFor(final JavaTypeName identifier) {
        return new WildcardTypeImpl(identifier);
    }

    public static @Nullable String getOuterClassName(final Type valueType) {
        return valueType.getIdentifier().immediatelyEnclosingClass().map(Object::toString).orElse(null);
    }

    /**
     * Represents concrete JAVA type.
     */
    private static final class ConcreteTypeImpl extends AbstractBaseType implements ConcreteType {
        private final Restrictions restrictions;

        /**
         * Creates instance of this class with package <code>pkName</code> and with the type name <code>name</code>.
         *
         * @param pkName string with package name
         * @param name string with the name of the type
         */
        ConcreteTypeImpl(final JavaTypeName identifier, final Restrictions restrictions) {
            super(identifier);
            this.restrictions = restrictions;
        }

        @Override
        public Restrictions getRestrictions() {
            return this.restrictions;
        }
    }

    /**
     * Represents concrete JAVA type with changed restriction values.
     */
    private static final class BaseTypeWithRestrictionsImpl extends AbstractBaseType implements
            BaseTypeWithRestrictions {
        private final Restrictions restrictions;

        /**
         * Creates instance of this class with package <code>pkName</code> and with the type name <code>name</code>.
         *
         * @param pkName string with package name
         * @param name string with the name of the type
         */
        BaseTypeWithRestrictionsImpl(final JavaTypeName identifier, final Restrictions restrictions) {
            super(identifier);
            this.restrictions = requireNonNull(restrictions);
        }

        @Override
        public Restrictions getRestrictions() {
            return this.restrictions;
        }
    }

    /**
     * Represents parametrized JAVA type.
     */
    private static class ParametrizedTypeImpl extends AbstractBaseType implements ParameterizedType {
        /**
         * Array of JAVA actual type parameters.
         */
        private final Type[] actualTypes;

        /**
         * JAVA raw type (like List, Set, Map...).
         */
        private final Type rawType;

        /**
         * Creates instance of this class with concrete rawType and array of actual parameters.
         *
         * @param rawType JAVA <code>Type</code> for raw type
         * @param actTypes array of actual parameters
         */
        ParametrizedTypeImpl(final Type rawType, final Type[] actTypes) {
            super(rawType.getIdentifier());
            this.rawType = requireNonNull(rawType);
            actualTypes = actTypes.clone();
            if (Arrays.stream(actualTypes).anyMatch(Objects::isNull)) {
                throw new NullPointerException("actTypes contains a null");
            }
        }

        @Override
        public Type[] getActualTypeArguments() {

            return this.actualTypes;
        }

        @Override
        public Type getRawType() {
            return this.rawType;
        }
    }

    /**
     * Represents JAVA bounded wildcard type.
     */
    private static class WildcardTypeImpl extends AbstractBaseType implements WildcardType {
        /**
         * Creates instance of this class with concrete package and type name.
         *
         * @param packageName string with the package name
         * @param typeName string with the name of type
         */
        WildcardTypeImpl(final JavaTypeName identifier) {
            super(identifier);
        }
    }

    public static <T extends Number & Comparable<T>> DefaultRestrictions<T> getDefaultRestrictions(final T min,
            final T max) {
        return new DefaultRestrictions<>(min, max);
    }

    private static final class DefaultRestrictions<T extends Number & Comparable<T>> implements Restrictions {
        private final RangeConstraint<?> rangeConstraint;

        DefaultRestrictions(final T min, final T max) {
            this.rangeConstraint = new DefaultRangeConstraint<>(min, max);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Optional<? extends RangeConstraint<?>> getRangeConstraint() {
            return Optional.of(rangeConstraint);
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return Collections.emptyList();
        }

        @Override
        public Optional<LengthConstraint> getLengthConstraint() {
            return Optional.empty();
        }
    }

    private static final class DefaultRangeConstraint<T extends Number & Comparable<T>> implements RangeConstraint<T> {
        private final T min;
        private final T max;

        DefaultRangeConstraint(final T min, final T max) {
            this.min = requireNonNull(min);
            this.max = requireNonNull(max);
        }

        @Override
        public Optional<String> getErrorAppTag() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getErrorMessage() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getReference() {
            return Optional.empty();
        }

        @Override
        public RangeSet<T> getAllowedRanges() {
            return ImmutableRangeSet.of(Range.closed(min, max));
        }
    }
}
