/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMember;
import org.opendaylight.mdsal.binding.model.util.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * By type member {@link Comparator} which provides sorting by type for members (variables)
 * in a generated class.
 *
 * @param <T> TypeMember type
 */
@Beta
public final class ByTypeMemberComparator<T extends TypeMember> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Fixed-size comparison. These are all numeric types, boolean, empty, identityref.
     */
    private static final int RANK_FIXED_SIZE          = 0;
    /**
     * Variable-sized comparison across simple components. These are string, binary and bits type.
     */
    private static final int RANK_VARIABLE_ARRAY      = 1;
    /**
     * Variable-size comparison across complex components.
     */
    private static final int RANK_INSTANCE_IDENTIFIER = 2;
    /**
     * Composite structure. DataObject, OpaqueObject and similar.
     */
    private static final int RANK_COMPOSITE           = 3;

    private static final Set<Type> FIXED_TYPES = Set.of(
        BaseYangTypes.INT8_TYPE,
        BaseYangTypes.INT16_TYPE,
        BaseYangTypes.INT32_TYPE,
        BaseYangTypes.INT64_TYPE,
        BaseYangTypes.DECIMAL64_TYPE,
        BaseYangTypes.UINT8_TYPE,
        BaseYangTypes.UINT16_TYPE,
        BaseYangTypes.UINT32_TYPE,
        BaseYangTypes.UINT64_TYPE,
        BaseYangTypes.BOOLEAN_TYPE,
        BaseYangTypes.EMPTY_TYPE,
        Types.CLASS);

    /**
     * Singleton instance.
     */
    private static final @NonNull ByTypeMemberComparator<?> INSTANCE = new ByTypeMemberComparator<>();

    private ByTypeMemberComparator() {
        // Hidden on purpose
    }

    /**
     * Returns the one and only instance of this class.
     *
     * @return this comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends TypeMember> @NonNull ByTypeMemberComparator<T> getInstance() {
        return (ByTypeMemberComparator<T>) INSTANCE;
    }

    @Override
    public int compare(final T member1, final T member2) {
        final Type type1 = getConcreteType(member1.getReturnType());
        final Type type2 = getConcreteType(member2.getReturnType());
        if (!type1.getIdentifier().equals(type2.getIdentifier())) {
            final int cmp = rankOf(type1) - rankOf(type2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return member1.getName().compareTo(member2.getName());
    }

    private static Type getConcreteType(final Type type) {
        if (type instanceof ConcreteType) {
            return type;
        } else if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getRawType();
        } else if (type instanceof GeneratedTransferObject) {
            GeneratedTransferObject rootGto = (GeneratedTransferObject) type;
            while (rootGto.getSuperType() != null) {
                rootGto = rootGto.getSuperType();
            }
            for (GeneratedProperty s : rootGto.getProperties()) {
                if (TypeConstants.VALUE_PROP.equals(s.getName())) {
                    return s.getReturnType();
                }
            }
        }
        return type;
    }

    private static int rankOf(final Type type) {
        if (FIXED_TYPES.contains(type)) {
            return RANK_FIXED_SIZE;
        }
        if (type.equals(BaseYangTypes.STRING_TYPE) || type.equals(Types.BYTE_ARRAY)) {
            return RANK_VARIABLE_ARRAY;
        }
        if (type.equals(BindingTypes.INSTANCE_IDENTIFIER) || type.equals(BindingTypes.KEYED_INSTANCE_IDENTIFIER)) {
            return RANK_INSTANCE_IDENTIFIER;
        }
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject gto =
                    GeneratorUtil.getTopParentTransportObject((GeneratedTransferObject) type);
            final TypeDefinition<?> typedef = gto.getBaseType();
            if (typedef instanceof BitsTypeDefinition) {
                return RANK_VARIABLE_ARRAY;
            }
        }
        return RANK_COMPOSITE;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
