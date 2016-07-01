/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import org.opendaylight.mdsal.binding.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public final class BaseYangTypes {
    /**
     * <code>Type</code> representation of <code>boolean</code> YANG type.
     */
    public static final Type BOOLEAN_TYPE = Types.BOOLEAN;

    /**
     * <code>Type</code> representation of <code>empty</code> YANG type.
     */
    public static final Type EMPTY_TYPE = BOOLEAN_TYPE;

    public static final Type ENUM_TYPE = Types.typeForClass(Enum.class);

    /**
     * <code>Type</code> representation of <code>int8</code> YANG type.
     */
    public static final Type INT8_TYPE = Types.typeForClass(Byte.class);

    /**
     * <code>Type</code> representation of <code>int16</code> YANG type.
     */
    public static final Type INT16_TYPE = Types.typeForClass(Short.class);

    /**
     * <code>Type</code> representation of <code>int32</code> YANG type.
     */
    public static final Type INT32_TYPE = Types.typeForClass(Integer.class);

    /**
     * <code>Type</code> representation of <code>int64</code> YANG type.
     */
    public static final Type INT64_TYPE = Types.typeForClass(Long.class);

    /**
     * <code>Type</code> representation of <code>string</code> YANG type.
     */
    public static final Type STRING_TYPE = Types.STRING;

    /**
     * <code>Type</code> representation of <code>decimal64</code> YANG type.
     */
    public static final Type DECIMAL64_TYPE = Types.typeForClass(BigDecimal.class);

    /**
     * <code>Type</code> representation of <code>uint8</code> YANG type.
     */
    public static final Type UINT8_TYPE = Types.typeForClass(Short.class, singleRangeRestrictions((short)0,
        (short)255));

    /**
     * <code>Type</code> representation of <code>uint16</code> YANG type.
     */
    public static final Type UINT16_TYPE = Types.typeForClass(Integer.class, singleRangeRestrictions(0, 65535));

    /**
     * <code>Type</code> representation of <code>uint32</code> YANG type.
     */
    public static final Type UINT32_TYPE = Types.typeForClass(Long.class, singleRangeRestrictions(0L, 4294967295L));

    /**
     * <code>Type</code> representation of <code>uint64</code> YANG type.
     */
    public static final Type UINT64_TYPE = Types.typeForClass(BigInteger.class,
            singleRangeRestrictions(BigInteger.ZERO, new BigInteger("18446744073709551615")));

    public static final Type UNION_TYPE = new UnionType();

    /**
     * <code>Type</code> representation of <code>binary</code> YANG type.
     */
    public static final Type BINARY_TYPE = Types.typeForClass(byte[].class);

    public static final Type INSTANCE_IDENTIFIER = Types.parameterizedTypeFor(Types
            .typeForClass(InstanceIdentifier.class));

    /**
     * mapping of basic built-in YANG types (keys) to JAVA {@link org.opendaylight.mdsal.binding.model.api.Type Type}.
     * This map is filled with mapping data in static initialization block.
     */
    private static final Map<String, Type> TYPE_MAP = ImmutableMap.<String, Type>builder()
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
            .put("union", UNION_TYPE)
            .put("binary", BINARY_TYPE)
            .put("instance-identifier", INSTANCE_IDENTIFIER)
            .build();

    /**
     * It is undesirable to create instance of this class.
     */
    private BaseYangTypes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Searches <code>Type</code> value to which is YANG <code>type</code>
     * mapped.
     *
     * @param type
     *            string with YANG type name
     * @return java <code>Type</code> representation of <code>type</code>
     */
    public static Type javaTypeForYangType(final String type) {
        return TYPE_MAP.get(type);
    }

    public static final TypeProvider BASE_YANG_TYPES_PROVIDER = new TypeProvider() {
        /**
         * Searches <code>Type</code> value to which is YANG <code>type</code>
         * mapped.
         *
         * @param type
         *            type definition representation of YANG type
         * @return java <code>Type</code> representation of <code>type</code>.
         *         If <code>type</code> isn't found then <code>null</code> is
         *         returned.
         */
        @Override
        public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
                final boolean lenientRelativeLeafrefs) {
            if (type != null) {
                return TYPE_MAP.get(type.getQName().getLocalName());
            }

            return null;
        }

        @Override
        public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
                final Restrictions restrictions, final boolean lenientRelativeLeafrefs) {
            String typeName = type.getQName().getLocalName();
            switch (typeName) {
                case "binary":
                    return restrictions == null ? Types.BYTE_ARRAY : Types.typeForClass(byte[].class, restrictions);
                case "decimal64":
                    return Types.typeForClass(BigDecimal.class, restrictions);
                case "enumeration":
                    return Types.typeForClass(Enum.class, restrictions);
                case "int8":
                    return Types.typeForClass(Byte.class, restrictions);
                case "int16":
                    return Types.typeForClass(Short.class, restrictions);
                case "int32":
                    return Types.typeForClass(Integer.class, restrictions);
                case "int64":
                    return Types.typeForClass(Long.class, restrictions);
                case "string":
                    return Types.typeForClass(String.class, restrictions);
                case "uint8":
                    return Types.typeForClass(Short.class, restrictions);
                case "uint16":
                    return Types.typeForClass(Integer.class, restrictions);
                case "uint32":
                    return Types.typeForClass(Long.class, restrictions);
                case "uint64":
                    return Types.typeForClass(BigInteger.class, restrictions);
                case "union" :
                    return UNION_TYPE;
                default:
                    return javaTypeForSchemaDefinitionType(type, parentNode, lenientRelativeLeafrefs);
            }
        }

        @Override
        public String getTypeDefaultConstruction(final LeafSchemaNode node) {
            return null;
        }

        @Override
        public String getConstructorPropertyName(final SchemaNode node) {
            return null;
        }

        @Override
        public String getParamNameFromType(final TypeDefinition<?> type) {
            return "_" + BindingMapping.getPropertyName(type.getQName().getLocalName());
        }
    };

    private static <T extends Number & Comparable<T>> Restrictions singleRangeRestrictions(final T min, final T max) {
        return Types.getDefaultRestrictions(min, max);
    }

    // FIXME: 4.0.0: remove this class
    @Deprecated
    public static final class UnionType implements Type {
        @Override
        public String getPackageName() {
            return null;
        }

        @Override
        public String getName() {
            return "Union";
        }

        @Override
        public String getFullyQualifiedName() {
            return "Union";
        }

        @Override
        @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
        public JavaTypeName getIdentifier() {
            return null;
        }
    }
}
