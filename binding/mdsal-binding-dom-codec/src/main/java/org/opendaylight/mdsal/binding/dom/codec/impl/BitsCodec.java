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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

// FIXME: 'SchemaUnawareCodec' is not correct: we use BitsTypeDefinition in construction
// FIXME: require the base class to be a TypeObject
// FIXME: MDSAL-743: require BitsTypeObject base class
final class BitsCodec extends SchemaUnawareCodec {
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
    private static final Cache<Class<?>, @NonNull BitsCodec> CACHE = CacheBuilder.newBuilder().weakKeys().softValues()
        .build();
    private static final MethodType CONSTRUCTOR_INVOKE_TYPE = MethodType.methodType(Object.class, Object.class);

    // Ordered by position
    private final ImmutableMap<String, Method> getters;
    private final ImmutableMap<String, Method> setters;
    private final MethodHandle ctor;

    private final Object builder;

    private BitsCodec(final MethodHandle ctor, final Map<String, Method> getters, final Map<String, Method> setters,
                      final Object builder) {
        this.ctor = requireNonNull(ctor);
        this.getters = ImmutableMap.copyOf(getters);
        this.setters = ImmutableMap.copyOf(setters);
        this.builder = builder;
    }

    static @NonNull BitsCodec of(final Class<?> returnType, final BitsTypeDefinition rootType)
            throws ExecutionException {
        return CACHE.get(returnType, () -> {
            final Map<String, Method> getters = new LinkedHashMap<>();
            final Map<String, Method> setters = new LinkedHashMap<>();

            for (Bit bit : rootType.getBits()) {
                final Method valueGetter = returnType.getMethod(BindingMapping.GETTER_PREFIX
                    + BindingMapping.getClassName(bit.getName()));
                getters.put(bit.getName(), valueGetter);
                if (returnType.getClasses().length > 0) {
                    final Method valueSetter = returnType.getClasses()[0].getMethod(BindingMapping.SETTER_PREFIX
                            + BindingMapping.getClassName(bit.getName()), boolean.class);
                    setters.put(bit.getName(), valueSetter);
                }
            }
            Constructor<?> constructor = null;
            for (Constructor<?> cst : returnType.getConstructors()) {
                Class<?>[] parameterTypes = cst.getParameterTypes();
                // Checks the length in case of constructor with no parameters
                if (parameterTypes.length > 0) {
                    if (!parameterTypes[0].equals(returnType)) {
                        constructor = cst;
                    }
                }
            }
            Object builder = returnType.getMethod("builder").invoke(null);

            final MethodHandle ctor = MethodHandles.publicLookup().unreflectConstructor(constructor)
                    .asType(CONSTRUCTOR_INVOKE_TYPE);
            return new BitsCodec(ctor, getters, setters, builder);
        });
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    protected Object deserializeImpl(final Object input) {
        checkArgument(input instanceof Set);
        @SuppressWarnings("unchecked")
        final Set<String> casted = (Set<String>) input;

        /*
        * Due to immutability, we have a builder class in the bits object.
        * We can set its values by invoking the setter methods of this builder.
        */
        for (Entry<String, Method> valueSet : setters.entrySet()) {
            try {
                valueSet.getValue().invoke(builder, casted.contains(valueSet.getKey()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to set bit " + valueSet.getKey(), e);
            }
        }

        try {
            return ctor.invokeExact(builder);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to instantiate object for " + input, e);
        }
    }

    @Override
    protected Set<String> serializeImpl(final Object input) {
        final Collection<String> result = new ArrayList<>(getters.size());
        for (Entry<String, Method> valueGet : getters.entrySet()) {
            final boolean value;
            try {
                value = (boolean) valueGet.getValue().invoke(input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to get bit " + valueGet.getKey(), e);
            }

            if (value) {
                result.add(valueGet.getKey());
            }
        }
        return result.size() == getters.size() ? getters.keySet() : ImmutableSet.copyOf(result);
    }
}
