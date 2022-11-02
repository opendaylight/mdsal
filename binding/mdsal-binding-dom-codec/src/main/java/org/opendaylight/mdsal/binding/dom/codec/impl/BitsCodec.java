/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

// FIXME: 'SchemaUnawareCodec' is not correct: we use BitsTypeDefinition in construction
// FIXME: require the base class to be a TypeObject
// FIXME: MDSAL-743: require BitsTypeObject base class
abstract class BitsCodec<N> extends SchemaUnawareCodec {
    /*
     * Use identity comparison for keys and allow classes to be GCd themselves.
     *
     * Since codecs can (and typically do) hold a direct or indirect strong reference to the class, they need to be also
     * accessed via reference. Using a weak reference could be problematic, because the codec would quite often be only
     * weakly reachable. We therefore use a soft reference, whose implementation guidance is suitable to our use case:
     *
     *     "Virtual machine implementations are, however, encouraged to bias against clearing recently-created or
     *      recently-used soft references."
     */
    private static final Cache<Class<?>, @NonNull BitsCodec<?>> CACHE = CacheBuilder.newBuilder()
            .weakKeys().softValues().build();
    private static final MethodType CONSTRUCTOR_INVOKE_TYPE = MethodType.methodType(Object.class, Object.class);
    private final MethodHandle ctor;
    private final ImmutableMap<String, Method> setters;
    private final Method getter;
    private final Object builder;

    private BitsCodec(final MethodHandle ctor, final Map<String, Method> setters, final Method getter,
                      final Object builder) {
        this.ctor = requireNonNull(ctor);
        this.builder = requireNonNull(builder);
        this.getter = requireNonNull(getter);
        this.setters = ImmutableMap.copyOf(setters);
    }

    static BitsCodec<?> ofType(final BitsTypeDefinition type, final MethodHandle ctor,
                               final Map<String, Method> setters, final Method getter, final Object builder) {
        final int size = type.getBits().size();
        if (size < 32) {
            return new BitsCodecInteger(ctor, setters, getter, builder);
        } else if (size < 64) {
            return new BitsCodecLong(ctor, setters, getter, builder);
        }
        return new BitsCodecIntArray(ctor, setters, getter, builder);
    }

    static @NonNull BitsCodec<?> of(final Class<?> returnType, final BitsTypeDefinition rootType)
            throws ExecutionException {
        return CACHE.get(returnType, () -> {
            final var setters = new LinkedHashMap<String, Method>();

            for (final Bit bit : rootType.getBits()) {
                if (returnType.getClasses().length > 0) {
                    final Method valueSetter = returnType.getClasses()[0].getMethod(BindingMapping.SETTER_PREFIX
                            + BindingMapping.getClassName(bit.getName()), boolean.class);
                    setters.put(bit.getName(), valueSetter);
                }
            }
            Constructor<?> constructor = null;
            for (final Constructor<?> cst : returnType.getConstructors()) {
                final Class<?>[] parameterTypes = cst.getParameterTypes();
                if (parameterTypes.length > 0) {
                    if (!parameterTypes[0].equals(returnType)) {
                        constructor = cst;
                    }
                }
            }
            final var builder = returnType.getMethod("builder").invoke(null);
            final var getBits = returnType.getMethod("getBits");
            final var ctor = MethodHandles.publicLookup().unreflectConstructor(constructor)
                    .asType(CONSTRUCTOR_INVOKE_TYPE);
            return BitsCodec.ofType(rootType, ctor, setters, getBits, builder);
        });
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    protected @NonNull Object deserializeImpl(final @NonNull Object input) {
        checkArgument(input instanceof Integer || input instanceof Long || input instanceof int[]);
        final N casted = (N) input;
        /*
         * We can do this walk based on field set sorted by name, since constructor arguments in Java Binding are
         * sorted by name.
         *
         * This means we will construct correct array for construction of bits object.
         */
        final var settersList = ImmutableList.copyOf(setters.entrySet());
        for (final var valueSet : settersList) {
            try {
                valueSet.getValue().invoke(builder, isBitSet(settersList.indexOf(valueSet), casted));
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to set bit " + valueSet.getKey(), e);
            }
        }
        try {
            return ctor.invokeExact(builder);
        } catch (final Throwable e) {
            throw new IllegalStateException("Failed to instantiate object for " + input, e);
        }
    }

    @Override
    protected @NonNull N serializeImpl(final @NonNull Object input) {
        try {
            return (N) getter.invoke(input);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to get bits in object " + input, e);
        }
    }

    protected abstract boolean isBitSet(int position, N bits);

    static final class BitsCodecInteger extends BitsCodec<Integer> {

        private BitsCodecInteger(final MethodHandle ctor, final Map<String, Method> setters, final Method getter,
                                 final Object builder) {
            super(ctor, setters, getter, builder);
        }

        @Override
        protected boolean isBitSet(final int position, final Integer bits) {
            return (bits & (1 << position)) != 0;
        }
    }

    static final class BitsCodecLong extends BitsCodec<Long> {

        private BitsCodecLong(final MethodHandle ctor, final Map<String, Method> setters, final Method getBits,
                              final Object builder) {
            super(ctor, setters, getBits, builder);
        }

        @Override
        protected boolean isBitSet(final int position, final Long bits) {
            return (bits & (1L << position)) != 0;
        }
    }

    static final class BitsCodecIntArray extends BitsCodec<int[]> {
        private static final int WORD_SIZE = 32;

        private BitsCodecIntArray(final MethodHandle ctor, final Map<String, Method> setters, final Method getBits,
                                  final Object builder) {
            super(ctor, setters, getBits, builder);
        }

        @Override
        protected boolean isBitSet(final int position, final int[] bits) {
            final int word = bits[position / WORD_SIZE];
            return (word & 1 << (position % WORD_SIZE)) != 0;
        }
    }
}
