/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.MissingSchemaException;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;

/**
 * Thrown when Java Binding class was used in data for which codec does not
 * have schema.
 *
 * <p>
 * By serialization / deserialization of this exception {@link #getBindingClass()}
 * will return null.
 */
@Beta
public final class MissingSchemaForClassException extends MissingSchemaException {

    private static final long serialVersionUID = 1L;

    private final transient Class<?> bindingClass;

    private MissingSchemaForClassException(final Class<?> clz) {
        super(String.format("Schema is not available for %s", clz));
        this.bindingClass = requireNonNull(clz);
    }

    static MissingSchemaForClassException forClass(final Class<?> clz) {
        return new MissingSchemaForClassException(clz);
    }

    public Class<?> getBindingClass() {
        return bindingClass;
    }

    public static void check(final BindingRuntimeContext runtimeContext, final Class<?> bindingClass) {
        final Object schema;
        if (Augmentation.class.isAssignableFrom(bindingClass)) {
            schema = runtimeContext.getAugmentationDefinition(bindingClass);
        } else {
            schema = runtimeContext.getSchemaDefinition(bindingClass);
        }
        if (schema == null) {
            throw MissingSchemaForClassException.forClass(bindingClass);
        }
    }
}