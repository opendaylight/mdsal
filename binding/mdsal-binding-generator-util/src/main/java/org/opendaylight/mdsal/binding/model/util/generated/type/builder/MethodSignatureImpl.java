/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;

class MethodSignatureImpl extends AbstractTypeMember implements MethodSignature {

    private final List<Parameter> params;
    private final boolean isAbstract;
    private final boolean isDefault;

    MethodSignatureImpl(final Type definingType, final String name, final List<AnnotationType> annotations,
        final String comment, final AccessModifier accessModifier, final Type returnType,
        final List<Parameter> params, final boolean isFinal, final boolean isAbstract, final boolean isStatic) {
        this(definingType, name, annotations, comment, accessModifier, returnType, params, isFinal, isAbstract,
            isStatic, false);
    }

    MethodSignatureImpl(final Type definingType, final String name, final List<AnnotationType> annotations,
            final String comment, final AccessModifier accessModifier, final Type returnType,
            final List<Parameter> params, final boolean isFinal, final boolean isAbstract, final boolean isStatic,
            final boolean isDefault) {
        super(definingType, name, annotations, comment, accessModifier, returnType, isFinal, isStatic);
        this.params = params;
        this.isAbstract = isAbstract;
        this.isDefault = isDefault;
    }

    @Override
    public boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public List<Parameter> getParameters() {
        return this.params;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(this.params);
        result = prime * result + Objects.hashCode(getReturnType());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodSignatureImpl other = (MethodSignatureImpl) obj;
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(this.params, other.params)) {
            return false;
        }
        if (!Objects.equals(getReturnType(), other.getReturnType())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MethodSignatureImpl [name=");
        builder.append(getName());
        builder.append(", comment=");
        builder.append(getComment());
        if (getDefiningType() != null) {
            builder.append(", definingType=");
            builder.append(getDefiningType().getPackageName());
            builder.append(".");
            builder.append(getDefiningType().getName());
        } else {
            builder.append(", definingType= null");
        }
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", params=");
        builder.append(this.params);
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append("]");
        return builder.toString();
    }
}
