/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.LeafNodeCodecContext;
import org.opendaylight.yangtools.concepts.Codec;

/**
 * Context for serialize/deserialize leaf.
 */
@Beta
public final class ValueContext {

    private static final MethodType OBJECT_METHOD = MethodType.methodType(Object.class, Object.class);
    private final Codec<Object, Object> codec;
    private final MethodHandle getter;
    private final Class<?> identifier;
    private final String getterName;

    /**
     * Prepare codec of leaf value and getter of binding leaf object for getting leaf.
     *
     * @param identifier
     *            - binding class
     * @param leaf
     *            - leaf codec context
     */
    public ValueContext(final Class<?> identifier, final LeafNodeCodecContext<?> leaf) {
        getterName = leaf.getGetter().getName();
        try {
            getter = MethodHandles.publicLookup().unreflect(identifier.getMethod(getterName)).asType(OBJECT_METHOD);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(
                String.format("Cannot find method %s in class %s", getterName, identifier), e);
        }
        this.identifier = identifier;
        codec = leaf.getValueCodec();
    }

    /**
     * Get object via invoking getter with input and serializes it by prepared codec of leaf.
     *
     * @param obj
     *            - input object
     * @return serialized invoked object
     */
    @SuppressWarnings("checkstyle:illegalCatch")
    public Object getAndSerialize(final Object obj) {
        final Object value;
        try {
            value = getter.invokeExact(obj);
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }

        Preconditions.checkArgument(value != null,
                "All keys must be specified for %s. Missing key is %s. Supplied key is %s",
                identifier, getterName, obj);
        return codec.serialize(value);
    }

    /**
     * Deserialize input object by prepared codec.
     *
     * @param obj
     *            - input object
     * @return deserialized object
     */
    public Object deserialize(final Object obj) {
        return codec.deserialize(obj);
    }
}
