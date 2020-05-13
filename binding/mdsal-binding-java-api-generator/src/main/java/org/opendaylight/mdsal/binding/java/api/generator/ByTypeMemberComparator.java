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

    private static final Set<Type> NUMBER_TYPES = Set.of(
            BaseYangTypes.INT8_TYPE,
            BaseYangTypes.INT16_TYPE,
            BaseYangTypes.INT32_TYPE,
            BaseYangTypes.INT64_TYPE,
            BaseYangTypes.DECIMAL64_TYPE,
            BaseYangTypes.UINT8_TYPE,
            BaseYangTypes.UINT16_TYPE,
            BaseYangTypes.UINT32_TYPE,
            BaseYangTypes.UINT64_TYPE);
    /**
     * Singleton instance.
     */
    private static final ByTypeMemberComparator<?> INSTANCE = new ByTypeMemberComparator<>();

    private ByTypeMemberComparator() {
    }

    /**
     * Returns the one and only instance of this class.
     *
     * @return this comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends TypeMember> ByTypeMemberComparator<T> getInstance() {
        return (ByTypeMemberComparator<T>) INSTANCE;
    }

    @Override
    public int compare(final T member1, final T member2) {
        final Type type1 = getConcreteType(member1.getReturnType());
        final Type type2 = getConcreteType(member2.getReturnType());
        if (!type1.getIdentifier().equals(type2.getIdentifier())) {
            if (isNumericBooleanIdentityref(type1) && !isNumericBooleanIdentityref(type2)) {
                return -1;
            } else if (isNumericBooleanIdentityref(type2) && !isNumericBooleanIdentityref(type1)) {
                return 1;
            } else if (isStringBinaryBits(type1) && !isStringBinaryBits(type2)) {
                return -1;
            } else if (isStringBinaryBits(type2) && !isStringBinaryBits(type1)) {
                return 1;
            } else if (isInstanceIdentifier(type1) && !isInstanceIdentifier(type2)) {
                return -1;
            } else if (isInstanceIdentifier(type2) && !isInstanceIdentifier(type1)) {
                return 1;
            }
        }
        return member1.getName().compareTo(member2.getName());
    }

    private boolean isNumericBooleanIdentityref(final Type type) {
        return type.equals(BaseYangTypes.BOOLEAN_TYPE) || type.equals(Types.CLASS)
                || NUMBER_TYPES.contains(type);
    }

    private boolean isStringBinaryBits(final Type type) {
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject transferObject =
                    GeneratorUtil.getTopParentTransportObject((GeneratedTransferObject) type);
            if (transferObject.getBaseType() != null) {
                return BitsTypeDefinition.class.isAssignableFrom(transferObject.getBaseType().getClass());
            }
        }
        return type.equals(Types.BYTE_ARRAY) || type.equals(BaseYangTypes.STRING_TYPE);
    }

    private boolean isInstanceIdentifier(final Type type) {
        return type.equals(BindingTypes.INSTANCE_IDENTIFIER)
                || type.equals(BindingTypes.KEYED_INSTANCE_IDENTIFIER);
    }

    private Type getConcreteType(final Type type) {
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

    private Object readResolve() {
        return INSTANCE;
    }
}
