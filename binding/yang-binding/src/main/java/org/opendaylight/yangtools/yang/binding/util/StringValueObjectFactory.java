/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for instantiating value-type generated objects with String being the base type. Unlike the normal
 * constructor, instances of this class bypass string validation.
 *
 *
 *
 * @param <T> Resulting object type.
 */
@Beta
public final class StringValueObjectFactory<T> {
    private static final MethodType CONSTRUCTOR_METHOD_TYPE = MethodType.methodType(Object.class, Object.class);
    private static final MethodType SETTER_METHOD_TYPE = MethodType.methodType(void.class, Object.class, String.class);
    private static final Logger LOG = LoggerFactory.getLogger(StringValueObjectFactory.class);
    private static final Lookup LOOKUP = MethodHandles.lookup();

    private final MethodHandle constructor;
    private final MethodHandle setter;

    private StringValueObjectFactory(final MethodHandle constructor, final MethodHandle setter) {
        this.constructor = Preconditions.checkNotNull(constructor);
        this.setter = Preconditions.checkNotNull(setter);
    }

    public static <T> StringValueObjectFactory<T> create(final Class<T> clazz, final String templateString) {
        final Constructor<T> stringConstructor;
        try {
            stringConstructor = clazz.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s does not have a String constructor", clazz), e);
        }

        final T template;
        try {
            template = stringConstructor.newInstance(templateString);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Failed to instantiate template %s for '%s'", clazz,
                templateString), e);
        }

        final Constructor<T> copyConstructor;
        try {
            copyConstructor = clazz.getConstructor(clazz);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s does not have a copy constructor", clazz), e);
        }

        final Field f;
        try {
            f = clazz.getDeclaredField("_value");
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("%s does not have required internal field", clazz), e);
        }
        f.setAccessible(true);

        final StringValueObjectFactory<T> ret;
        try {
            ret = new StringValueObjectFactory<>(
                    LOOKUP.unreflectConstructor(copyConstructor).asType(CONSTRUCTOR_METHOD_TYPE).bindTo(template),
                    LOOKUP.unreflectSetter(f).asType(SETTER_METHOD_TYPE));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to instantiate method handles", e);
        }

        LOG.info("Instantiated factory for {}", clazz);
        return ret;
    }

    public T newInstance(final String string) {
        Preconditions.checkNotNull(string, "Argument may not be null");

        try {
            final T ret = (T) constructor.invokeExact();
            setter.invokeExact(ret, string);
            LOG.trace("Instantiated new object {} value {}", ret.getClass(), string);
            return ret;
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }
}
