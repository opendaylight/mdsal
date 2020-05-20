/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;

final class GeneratedPropertyImpl extends AbstractTypeMember implements GeneratedProperty {
    private final String value;
    private final boolean readOnly;

    GeneratedPropertyImpl(final Type definingType, final String name, final List<AnnotationType> annotations,
            final TypeMemberComment comment, final AccessModifier accessModifier, final Type returnType,
            final boolean isFinal, final boolean isStatic, final boolean isReadOnly, final String value) {
        super(definingType, name, annotations, comment, accessModifier, returnType, isFinal, isStatic);
        this.value = value;
        this.readOnly = isReadOnly;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder()
            .append("GeneratedPropertyImpl [name=").append(getName())
            .append(", annotations=").append(getAnnotations())
            .append(", comment=").append(getComment().orElse(null))
            .append(", parent=");
        if (getDefiningType() != null) {
            builder.append(getDefiningType().getPackageName()).append(".").append(getDefiningType().getName());
        } else {
            builder.append("null");
        }
        return builder.append(", returnType=").append(getReturnType())
            .append(", isFinal=").append(isFinal())
            .append(", isReadOnly=").append(readOnly)
            .append(", modifier=").append(getAccessModifier())
            .append(']').toString();
    }
}
