/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.TypeMember;

@Beta
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
        return annotations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public Type getDefiningType() {
        return definingType;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, returnType);
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

        if (!(obj instanceof AbstractTypeMember)) {
            return false;
        }

        AbstractTypeMember other = (AbstractTypeMember) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
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
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append("]");
        return builder.toString();
    }
}
