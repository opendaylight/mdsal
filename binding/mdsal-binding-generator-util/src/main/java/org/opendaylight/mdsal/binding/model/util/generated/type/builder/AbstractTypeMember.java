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
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMember;

abstract class AbstractTypeMember implements TypeMember {

    private final String name;
    private final String comment;
    private final Type definingType;
    private final Type returnType;
    private final List<AnnotationType> annotations;
    private final boolean isFinal;
    private final boolean isStatic;
    private final AccessModifier accessModifier;

    protected AbstractTypeMember(final Type definingType, final String name,  final List<AnnotationType> annotations,
            final String comment, final AccessModifier accessModifier, final Type returnType,
            final boolean isFinal, final boolean isStatic) {
        this.definingType = definingType;
        this.name = name;
        this.annotations = annotations;
        this.comment = comment;
        this.accessModifier = accessModifier;
        this.returnType = returnType;
        this.isFinal = isFinal;
        this.isStatic = isStatic;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return this.annotations;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public Type getDefiningType() {
        return this.definingType;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return this.accessModifier;
    }

    @Override
    public Type getReturnType() {
        return this.returnType;
    }

    @Override
    public boolean isFinal() {
        return this.isFinal;
    }

    @Override
    public boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Objects.hashCode(getName());
        result = (prime * result) + Objects.hashCode(getReturnType());
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
        final AbstractTypeMember other = (AbstractTypeMember) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AbstractTypeMember [name=");
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
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append("]");
        return builder.toString();
    }
}
