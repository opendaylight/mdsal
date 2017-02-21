/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util.generated.type.builder;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.Type;

final class GeneratedPropertyImpl extends AbstractTypeMember implements GeneratedProperty {
    private final String value;
    private final boolean isReadOnly;

    public GeneratedPropertyImpl(final Type definingType, final String name, final List<AnnotationType> annotations, final String comment,
                                 final AccessModifier accessModifier, final Type returnType, final boolean isFinal, final boolean isStatic, final boolean isReadOnly, final String value) {
        super(definingType, name, annotations, comment, accessModifier, returnType, isFinal, isStatic);
        this.value = value;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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
        builder.append(this.isReadOnly);
        builder.append(", modifier=");
        builder.append(getAccessModifier());
        builder.append("]");
        return builder.toString();
    }
}
