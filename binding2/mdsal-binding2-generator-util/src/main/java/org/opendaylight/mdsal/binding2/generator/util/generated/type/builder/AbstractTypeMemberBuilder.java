/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

@Beta
abstract class AbstractTypeMemberBuilder<T extends TypeMemberBuilder<T>> implements TypeMemberBuilder<T> {

    private final String name;
    private String comment = "";
    private boolean isFinal;
    private boolean isStatic;
    private Type returnType;
    private AccessModifier accessModifier;
    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();

    public AbstractTypeMemberBuilder(final String name) {
        this.name = name;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        Preconditions.checkArgument(packageName != null, "Annotation Type cannot have package name null!");
        Preconditions.checkArgument(name != null, "Annotation Type cannot have name as null!");

        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    protected abstract T thisInstance();

    protected Iterable<AnnotationTypeBuilder> getAnnotationBuilders() {
        return annotationBuilders;
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
    public T setReturnType(final Type returnType) {
        Preconditions.checkArgument(returnType != null, "Return Type of member cannot be null!");
        this.returnType = returnType;
        return thisInstance();
    }

    @Override
    public T setAccessModifier(final AccessModifier modifier) {
        Preconditions.checkArgument(modifier != null, "Access Modifier for member type cannot be null!");
        this.accessModifier = modifier;
        return thisInstance();
    }

    @Override
    public T setComment(final String comment) {
        if (comment == null) {
            this.comment = "";
        }
        this.comment = comment;
        return thisInstance();
    }

    @Override
    public T setFinal(final boolean isFinal) {
        this.isFinal = isFinal;
        return thisInstance();
    }

    @Override
    public T setStatic(final boolean isStatic) {
        this.isStatic = isStatic;
        return thisInstance();
    }

    //TODO: implement methods
}
