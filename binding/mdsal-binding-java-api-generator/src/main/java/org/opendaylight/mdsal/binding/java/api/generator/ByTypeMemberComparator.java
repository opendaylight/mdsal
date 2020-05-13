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
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMember;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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
     * Singleton instance.
     */
    private static final ByTypeMemberComparator<?> INSTANCE = new ByTypeMemberComparator<>();

    private ByTypeMemberComparator(){
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
        final Type type1 = member1.getReturnType();
        final Type type2 = member2.getReturnType();
        final String className1 = getClassName(type1);
        final String className2 = getClassName(type2);
        if (!type1.getIdentifier().equals(type2.getIdentifier())) {
            if (isNumericBooleanIdentityref(type1, className1) && !isNumericBooleanIdentityref(type2, className2)) {
                return -1;
            } else if (isNumericBooleanIdentityref(type2, className2)
                    && !isNumericBooleanIdentityref(type1, className1)) {
                return 1;
            } else if (isStringBinaryBits(type1, className1) && !isStringBinaryBits(type2, className2)) {
                return -1;
            } else if (isStringBinaryBits(type2, className2) && !isStringBinaryBits(type1, className1)) {
                return 1;
            } else if (isInstanceIdentifier(type1, className1) && !isInstanceIdentifier(type2, className2)) {
                return -1;
            } else if (isInstanceIdentifier(type2, className2) && !isInstanceIdentifier(type1, className1)) {
                return 1;
            }
        }
        return member1.getName().compareTo(member2.getName());
    }

    private boolean isNumericBooleanIdentityref(final Type type, final String className) {
        try {
            final Class<?> clazz = Class.forName(className);
            return Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
                || Class.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isStringBinaryBits(final Type type, final String className) {
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject transferObject =
                    GeneratorUtil.getTopParentTransportObject((GeneratedTransferObject) type);
            if (transferObject.getBaseType() != null) {
                return BitsTypeDefinition.class.isAssignableFrom(transferObject.getBaseType().getClass());
            }
        }
        return className.equals("byte[]") || className.equals("java.lang.String");
    }

    private boolean isInstanceIdentifier(final Type type, final String className) {
        try {
            return InstanceIdentifier.class.isAssignableFrom(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private String getClassName(final Type type) {
        try {
            final Type baseYangType = TypeUtils.getBaseYangType(type);
            return baseYangType.getIdentifier().toString();
        } catch (IllegalArgumentException e) {
            return type.getIdentifier().toString();
        }
    }

    private Object readResolve()  {
        return INSTANCE;
    }
}
