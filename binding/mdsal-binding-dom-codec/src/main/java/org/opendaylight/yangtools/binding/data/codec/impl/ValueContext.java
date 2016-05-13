/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;

final class ValueContext {
    private static final Lookup LOOKUP = MethodHandles.publicLookup();
    private static final MethodType OBJECT_METHOD = MethodType.methodType(Object.class, Object.class);
    private final Codec<Object, Object> codec;
    private final MethodHandle getter;
    private final Class<?> identifier;
    private final String getterName;

    ValueContext(final Class<?> identifier, final LeafNodeCodecContext <?>leaf) {
        String getterPrefix = BindingCodecContext.GETTER_PREFIX;
        final String getterBody = BindingMapping.getClassName(leaf.getDomPathArgument().getNodeType());
        try {
            MethodHandle possibleGetter;
            try {
                possibleGetter = LOOKUP.unreflect(identifier.getMethod(getterPrefix + getterBody)).asType(OBJECT_METHOD);
            } catch (NoSuchMethodException e) {
                getterPrefix = BindingCodecContext.GETTER_PREFIX_BOOLEAN;
                possibleGetter = LOOKUP.unreflect(identifier.getMethod(getterPrefix + getterBody)).asType(OBJECT_METHOD);
            }
            getterName = getterPrefix + getterBody;
            getter = possibleGetter;
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(String.format("Cannot find method %s in class %s", getterPrefix + getterBody, identifier), e);
        }
        this.identifier = identifier;
        codec = leaf.getValueCodec();
    }

    Object getAndSerialize(final Object obj) {
        final Object value;
        try {
            value = getter.invokeExact(obj);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }

        Preconditions.checkArgument(value != null,
                "All keys must be specified for %s. Missing key is %s. Supplied key is %s",
                identifier, getterName, obj);
        return codec.serialize(value);
    }

    Object deserialize(final Object obj) {
        return codec.deserialize(obj);
    }

}