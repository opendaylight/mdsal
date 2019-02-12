/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value.EncapsulatedValueCodec;
import org.opendaylight.yangtools.concepts.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context for serializing input value of union type and deserializing objects to binding.
 *
 */
@Beta
public class UnionValueOptionContext {

    private static final Logger LOG = LoggerFactory.getLogger(UnionValueOptionContext.class);

    private static final MethodType OBJECT_TYPE = MethodType.methodType(Object.class, Object.class);

    private final Class<?> bindingType;
    private final Codec<Object, Object> codec;
    private final MethodHandle getter;
    private final MethodHandle unionCtor;

    /**
     * Prepare union as binding object and codec for this object, make a direct method handle of getting union
     * type and constructor of union type for initializing it.
     *
     * @param unionType
     *            - union as binding object
     * @param valueType
     *            - returned type of union
     * @param getter
     *            - method for getting union type
     * @param codec
     *            - codec for serialize/deserialize type of union
     */
    public UnionValueOptionContext(final Class<?> unionType, final Class<?> valueType, final Method getter,
            final Codec<Object, Object> codec) {
        this.bindingType = requireNonNull(valueType);
        this.codec = requireNonNull(codec);

        try {
            this.getter = MethodHandles.publicLookup().unreflect(getter).asType(OBJECT_TYPE);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Failed to access method " + getter, e);
        }

        try {
            this.unionCtor = MethodHandles.publicLookup()
                    .findConstructor(unionType, MethodType.methodType(void.class, valueType)).asType(OBJECT_TYPE);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(
                    String.format("Failed to access constructor for %s in type %s", valueType, unionType), e);
        }
    }

    /**
     * Serialize input based on prepared codec.
     *
     * @param input
     *            - object to serialize
     * @return serialized objetc
     */
    public Object serialize(final Object input) {
        final Object baValue = getValueFrom(input);
        return baValue == null ? null : codec.serialize(baValue);
    }

    /**
     * Deserialize input object via prepared codec for invoking new object of union as binding.
     *
     * @param input
     *            - input object for deserializing
     * @return deserialized union binding type object
     */
    @SuppressWarnings("checkstyle:illegalCatch")
    public Object deserializeUnion(final Object input) {
        // Side-step potential exceptions by checking the type if it is available
        if (codec instanceof EncapsulatedValueCodec && !((EncapsulatedValueCodec) codec).canAcceptObject(input)) {
            return null;
        }

        final Object value;
        try {
            value = codec.deserialize(input);
        } catch (final Exception e) {
            LOG.debug("Codec {} failed to deserialize input {}", codec, input, e);
            return null;
        }

        try {
            return unionCtor.invokeExact(value);
        } catch (final ClassCastException e) {
            // This case can happen. e.g. NOOP_CODEC
            LOG.debug("Failed to instantiate {} for input {} value {}", bindingType, input, value, e);
            return null;
        } catch (final Throwable e) {
            throw new IllegalArgumentException("Failed to construct union for value " + value, e);
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private Object getValueFrom(final Object input) {
        try {
            return getter.invokeExact(input);
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public int hashCode() {
        return bindingType.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UnionValueOptionContext)) {
            return false;
        }

        final UnionValueOptionContext other = (UnionValueOptionContext) obj;
        return bindingType.equals(other.bindingType);
    }
}
