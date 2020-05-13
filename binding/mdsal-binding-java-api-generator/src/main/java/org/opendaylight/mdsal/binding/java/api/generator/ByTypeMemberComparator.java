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
import java.util.Objects;
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
        try {
            final Class<?> clazz = Class.forName(type.getFullyQualifiedName());
            return Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
                    || Class.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isStringBinaryBits(final Type type) {
        if (type instanceof GeneratedTransferObject
                && Objects.nonNull(((GeneratedTransferObject) type).getBaseType())) {
            return BitsTypeDefinition.class.isAssignableFrom(
                    ((GeneratedTransferObject) type).getBaseType().getClass());
        } else {
            try {
                final String qualifiedName = type.getFullyQualifiedName();
                return qualifiedName.equals("byte[]") || String.class.isAssignableFrom(Class.forName(qualifiedName));
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    private boolean isInstanceIdentifier(final Type type) {
        try {
            return InstanceIdentifier.class.isAssignableFrom(Class.forName(type.getFullyQualifiedName()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
