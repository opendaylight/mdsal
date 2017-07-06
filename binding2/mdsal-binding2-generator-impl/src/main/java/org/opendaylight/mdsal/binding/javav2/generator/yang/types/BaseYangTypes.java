/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.yang.types;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public final class BaseYangTypes {
    /**
     * Mapping of basic built-in YANG types (RFC7950) to JAVA
     * {@link org.opendaylight.mdsal.binding.javav2.model.api.Type Type}.
     * This map is filled with mapping data in static initialization block
     */
    private static final Map<String, Type> TYPE_MAP;

    /**
     * <code>Type</code> representation of <code>binary</code> YANG type
     */
    public static final Type BINARY_TYPE = Types.primitiveType("byte[]", null);

    /**
     * <code>Type</code> representation of <code>boolean</code> YANG type
     */
    public static final Type BOOLEAN_TYPE = Types.typeForClass(Boolean.class);

    /**
     * <code>Type</code> representation of <code>decimal64</code> YANG type
     */
    public static final Type DECIMAL64_TYPE = Types.typeForClass(BigDecimal.class);

    /**
     * <code>Type</code> representation of <code>empty</code> YANG type
     */
    public static final Type EMPTY_TYPE = Types.typeForClass(Boolean.class);

    /**
     * <code>Type</code> representation of <code>enumeration</code> YANG type
     */
    public static final Type ENUM_TYPE = Types.typeForClass(Enum.class);

    /**
     * <code>Type</code> representation of <code>instance-identifier</code> YANG type
     */
    public static final Type INSTANCE_IDENTIFIER = Types.parameterizedTypeFor(Types
            .typeForClass(InstanceIdentifier.class));

    /**
     * <code>Type</code> representation of <code>int8</code> YANG type
     */
    public static final Type INT8_TYPE = Types.typeForClass(Byte.class);

    /**
     * <code>Type</code> representation of <code>int16</code> YANG type
     */
    public static final Type INT16_TYPE = Types.typeForClass(Short.class);

    /**
     * <code>Type</code> representation of <code>int32</code> YANG type
     */
    public static final Type INT32_TYPE = Types.typeForClass(Integer.class);

    /**
     * <code>Type</code> representation of <code>int64</code> YANG type
     */
    public static final Type INT64_TYPE = Types.typeForClass(Long.class);

    /**
     * <code>Type</code> representation of <code>string</code> YANG type
     */
    public static final Type STRING_TYPE = Types.typeForClass(String.class);

    /**
     * <code>Type</code> representation of <code>uint8</code> YANG type
     */
    public static final Type UINT8_TYPE = Types.typeForClass(Short.class, singleRangeRestrictions((short)0, (short)255));

    /**
     * <code>Type</code> representation of <code>uint16</code> YANG type
     */
    public static final Type UINT16_TYPE = Types.typeForClass(Integer.class, singleRangeRestrictions(0, 65535));

    /**
     * <code>Type</code> representation of <code>uint32</code> YANG type
     */
    public static final Type UINT32_TYPE = Types.typeForClass(Long.class, singleRangeRestrictions(0L, 4294967295L));

    /**
     * <code>Type</code> representation of <code>uint64</code> YANG type
     */
    public static final Type UINT64_TYPE = Types.typeForClass(BigInteger.class,
            singleRangeRestrictions(BigInteger.ZERO, new BigInteger("18446744073709551615")));

    /**
     * <code>Type</code> representation of <code>union</code> YANG type
     */
    public static final Type UNION_TYPE = new UnionType();

    private BaseYangTypes() {
        throw new UnsupportedOperationException();
    }

    static {
        final Builder<String, Type> b = ImmutableMap.builder();

        b.put("binary", BINARY_TYPE);
        b.put("boolean", BOOLEAN_TYPE);
        b.put("decimal64", DECIMAL64_TYPE);
        b.put("empty", EMPTY_TYPE);
        b.put("enumeration", ENUM_TYPE);
        b.put("instance-identifier", INSTANCE_IDENTIFIER);
        b.put("int8", INT8_TYPE);
        b.put("int16", INT16_TYPE);
        b.put("int32", INT32_TYPE);
        b.put("int64", INT64_TYPE);
        b.put("string", STRING_TYPE);
        b.put("uint8", UINT8_TYPE);
        b.put("uint16", UINT16_TYPE);
        b.put("uint32", UINT32_TYPE);
        b.put("uint64", UINT64_TYPE);
        b.put("union", UNION_TYPE);

        TYPE_MAP = b.build();
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
                                                    ModuleContext context) {
            if (type != null) {
                return TYPE_MAP.get(type.getQName().getLocalName());
            }

            return null;
        }

        @Override
        public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
                                                    final Restrictions restrictions, ModuleContext context) {

            final String typeName = type.getQName().getLocalName();
            switch (typeName) {
                case "binary":
                    return restrictions == null ? Types.BYTE_ARRAY : Types.primitiveType("byte[]", restrictions);
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
                    return javaTypeForSchemaDefinitionType(type, parentNode, context);
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
            return JavaIdentifierNormalizer.normalizeSpecificIdentifier(type.getQName().getLocalName(),
                    JavaIdentifier.METHOD);
        }
    };

    private static <T extends Number> Restrictions singleRangeRestrictions(final T min, final T max) {
        return Types.getDefaultRestrictions(min, max);
    }

    public static final class UnionType implements Type {
        @Override
        public String getPackageName() {
            return BindingMapping.PACKAGE_PREFIX;
        }
        @Override
        public String getName() {
            return "Union";
        }
        @Override
        public String getFullyQualifiedName() {
            return "Union";
        }
    }
}
