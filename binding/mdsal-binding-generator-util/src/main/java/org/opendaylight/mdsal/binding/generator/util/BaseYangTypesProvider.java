/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import com.google.common.annotations.Beta;
import java.math.BigDecimal;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
public final class BaseYangTypesProvider implements TypeProvider {
    public static final @NonNull BaseYangTypesProvider INSTANCE = new BaseYangTypesProvider();

    private BaseYangTypesProvider() {
        // Hidden on purpose
    }

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
        return type == null ? null : BaseYangTypes.javaTypeForYangType(type.getQName().getLocalName());
    }

    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
            final Restrictions restrictions, final boolean lenientRelativeLeafrefs) {
        final String typeName = type.getQName().getLocalName();
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
                return Types.typeForClass(Uint8.class, restrictions);
            case "uint16":
                return Types.typeForClass(Uint16.class, restrictions);
            case "uint32":
                return Types.typeForClass(Uint32.class, restrictions);
            case "uint64":
                return Types.typeForClass(Uint64.class, restrictions);
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
}