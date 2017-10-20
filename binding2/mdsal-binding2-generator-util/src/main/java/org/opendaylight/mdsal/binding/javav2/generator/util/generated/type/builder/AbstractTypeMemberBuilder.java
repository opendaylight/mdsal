/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

@Beta
abstract class AbstractTypeMemberBuilder<T extends TypeMemberBuilder<T>> implements TypeMemberBuilder<T> {

    private final String name;
    private String comment = "";
    private boolean isFinal;
    private boolean isStatic;
    private Type returnType;
    private AccessModifier accessModifier;
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();

    AbstractTypeMemberBuilder(final String strName) {
        this.name = strName;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String strName) {
        Preconditions.checkArgument(packageName != null, "Annotation Type cannot have package name null!");
        Preconditions.checkArgument(strName != null, "Annotation Type cannot have name as null!");

        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, strName);
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    protected abstract T thisInstance();

    protected Iterable<AnnotationTypeBuilder> getAnnotationBuilders() {
        return annotationBuilders;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public String getName() {
        return name;
    }

    protected String getComment() {
        return comment;
    }

    protected boolean isFinal() {
        return isFinal;
    }

    protected boolean isStatic() {
        return isStatic;
    }

    protected Type getReturnType() {
        return returnType;
    }

    @Override
    public T setReturnType(final Type retType) {
        Preconditions.checkArgument(retType != null, "Return Type of member cannot be null!");
        this.returnType = retType;
        return thisInstance();
    }

    @Override
    public T setAccessModifier(final AccessModifier modifier) {
        Preconditions.checkArgument(modifier != null, "Access Modifier for member type cannot be null!");
        this.accessModifier = modifier;
        return thisInstance();
    }

    @Override
    public T setComment(final String strComment) {
        if (strComment == null) {
            this.comment = "";
        }
        this.comment = strComment;
        return thisInstance();
    }

    @Override
    public T setFinal(final boolean beFinal) {
        this.isFinal = beFinal;
        return thisInstance();
    }

    @Override
    public T setStatic(final boolean beStatic) {
        this.isStatic = beStatic;
        return thisInstance();
    }

    protected List<AnnotationType> toAnnotationTypes() {
        final List<AnnotationType> annotations = new ArrayList<>();
        for (final AnnotationTypeBuilder annotBuilder : getAnnotationBuilders()) {
            if (annotBuilder != null) {
                annotations.add(annotBuilder.toInstance());
            }
        }

        return ImmutableList.copyOf(annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getReturnType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AbstractTypeMemberBuilder)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        AbstractTypeMemberBuilder<?> other = (AbstractTypeMemberBuilder<?>) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedPropertyImpl [name=");
        builder.append(getName());
        builder.append(", annotations=");
        builder.append(getAnnotationBuilders());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", isFinal=");
        builder.append(isFinal());
        builder.append(", modifier=");
        builder.append(getAccessModifier());
        builder.append("]");
        return builder.toString();
    }
}
