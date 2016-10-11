/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import java.util.List;

import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

/**
 * @deprecated Use {@link org.opendaylight.mdsal.binding.generator.util.generated.type.builder.GeneratedPropertyImpl} instead.
 */
@Deprecated
final class GeneratedPropertyImpl extends AbstractTypeMember implements GeneratedProperty {
    private String value;
    private boolean isReadOnly;

    public GeneratedPropertyImpl(Type definingType, String name, List<AnnotationType> annotations, String comment,
                                 AccessModifier accessModifier, Type returnType, boolean isFinal, boolean isStatic, boolean isReadOnly, String value) {
        super(definingType, name, annotations, comment, accessModifier, returnType, isFinal, isStatic);
        this.value = value;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedPropertyImpl [name=");
        builder.append(getName());
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append(", comment=");
        builder.append(getComment());
        if (getDefiningType() != null) {
            builder.append(", parent=");
            builder.append(getDefiningType().getPackageName());
            builder.append(".");
            builder.append(getDefiningType().getName());
        } else {
            builder.append(", parent=null");
        }
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", isFinal=");
        builder.append(isFinal());
        builder.append(", isReadOnly=");
        builder.append(isReadOnly);
        builder.append(", modifier=");
        builder.append(getAccessModifier());
        builder.append("]");
        return builder.toString();
    }
}
