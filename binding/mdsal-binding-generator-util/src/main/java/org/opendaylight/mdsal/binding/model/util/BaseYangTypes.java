/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class BaseYangTypes {
    /**
     * {@code Type} representation of {@code boolean} YANG type.
     */
    public static final Type BOOLEAN_TYPE = Types.BOOLEAN;

    /**
     * {@code Type} representation of {@code empty} YANG type.
     */
    public static final Type EMPTY_TYPE = Types.typeForClass(Empty.class);

    public static final Type ENUM_TYPE = Types.typeForClass(Enum.class);

    /**
     * {@code Type} representation of {@code int8} YANG type.
     */
    public static final Type INT8_TYPE = Types.typeForClass(Byte.class);

    /**
     * {@code Type} representation of {@code int16} YANG type.
     */
    public static final Type INT16_TYPE = Types.typeForClass(Short.class);

    /**
     * {@code Type} representation of {@code int32} YANG type.
     */
    public static final Type INT32_TYPE = Types.typeForClass(Integer.class);

    /**
     * {@code Type} representation of {@code int64} YANG type.
     */
    public static final Type INT64_TYPE = Types.typeForClass(Long.class);

    /**
     * {@code Type} representation of {@code string} YANG type.
     */
    public static final Type STRING_TYPE = Types.STRING;

    /**
     * {@code Type} representation of {@code decimal64} YANG type.
     */
    public static final Type DECIMAL64_TYPE = Types.typeForClass(BigDecimal.class);

    /**
     * {@code Type} representation of {@code uint8} YANG type.
     */
    public static final Type UINT8_TYPE = Types.typeForClass(Uint8.class);

    /**
     * {@code Type} representation of {@code uint16} YANG type.
     */
    public static final Type UINT16_TYPE = Types.typeForClass(Uint16.class);

    /**
     * {@code Type} representation of {@code uint32} YANG type.
     */
    public static final Type UINT32_TYPE = Types.typeForClass(Uint32.class);

    /**
     * {@code Type} representation of {@code uint64} YANG type.
     */
    public static final Type UINT64_TYPE = Types.typeForClass(Uint64.class);

    /**
     * {@code Type} representation of {@code binary} YANG type.
     */
    public static final Type BINARY_TYPE = Types.BYTE_ARRAY;

    public static final Type INSTANCE_IDENTIFIER = Types.parameterizedTypeFor(BindingTypes.INSTANCE_IDENTIFIER);

    /**
     * mapping of basic built-in YANG types (keys) to JAVA {@link org.opendaylight.mdsal.binding.model.api.Type Type}.
     * This map is filled with mapping data in static initialization block.
     */
    private static final ImmutableMap<String, Type> TYPE_MAP = ImmutableMap.<String, Type>builder()
            .put("boolean", BOOLEAN_TYPE)
            .put("empty", EMPTY_TYPE)
            .put("enumeration", ENUM_TYPE)
            .put("int8", INT8_TYPE)
            .put("int16", INT16_TYPE)
            .put("int32", INT32_TYPE)
            .put("int64", INT64_TYPE)
            .put("string", STRING_TYPE)
            .put("decimal64", DECIMAL64_TYPE)
            .put("uint8", UINT8_TYPE)
            .put("uint16", UINT16_TYPE)
            .put("uint32", UINT32_TYPE)
            .put("uint64", UINT64_TYPE)
            .put("binary", BINARY_TYPE)
            .put("instance-identifier", INSTANCE_IDENTIFIER)
            .build();

    private BaseYangTypes() {
        // Hidden on purpose
    }

    /**
     * Searches {@code Type} value to which is YANG {@code type} mapped.
     *
     * @param type string with YANG type name
     * @return Java {@code Type} representation of {@code type}, or null if the type is not mapped.
     */
    public static Type javaTypeForYangType(final String type) {
        return TYPE_MAP.get(type);
    }
}
