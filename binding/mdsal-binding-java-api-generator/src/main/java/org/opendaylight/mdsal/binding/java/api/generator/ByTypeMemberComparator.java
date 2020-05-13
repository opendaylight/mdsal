/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

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
public class ByTypeMemberComparator<T extends TypeMember> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 1L;

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
        try {
            return className.equals("byte[]") || String.class.isAssignableFrom(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isInstanceIdentifier(final Type type, final String className) {
        try {
            return InstanceIdentifier.class.isAssignableFrom(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private String getClassName(final Type type) {
        String className;
        try {
            final Type baseYangType = TypeUtils.getBaseYangType(type);
            className = baseYangType.getIdentifier().toString();
        } catch (IllegalArgumentException e) {
            className = type.getIdentifier().toString();
        }
        return className;
    }
}
