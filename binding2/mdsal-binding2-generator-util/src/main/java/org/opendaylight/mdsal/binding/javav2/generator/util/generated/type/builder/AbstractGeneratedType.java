/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.javav2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;

@Beta
abstract class AbstractGeneratedType extends AbstractBaseType implements GeneratedType {

    private final Type parent;
    private final Type parentTypeForBuilder;
    private final String comment;
    private final List<AnnotationType> annotations;
    private final List<Type> implementsTypes;
    private final List<Enumeration> enumerations;
    private final List<Constant> constants;
    private final List<MethodSignature> methodSignatures;
    private final List<GeneratedType> enclosedTypes;
    private final List<GeneratedProperty> properties;
    private final boolean isAbstract;

    public AbstractGeneratedType(final AbstractGeneratedTypeBuilder<?> builder) {
        super(builder.getPackageName(), builder.getName(), true, null);
        this.parent = builder.getParent();
        this.parentTypeForBuilder = builder.getParentTypeForBuilder();
        this.comment = builder.getComment();
        this.annotations = toUnmodifiableAnnotations(builder.getAnnotations());
        this.implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
        this.constants = makeUnmodifiable(builder.getConstants());
        this.enumerations = toUnmodifiableEnumerations(builder.getEnumerations());
        this.methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
        this.enclosedTypes = toUnmodifiableEnclosedTypes(builder.getEnclosedTypes(),
                builder.getEnclosedTransferObjects());
        this.properties = toUnmodifiableProperties(builder.getProperties());
        this.isAbstract = builder.isAbstract();
    }

    public AbstractGeneratedType(final Type parent, final String packageName, final String name, final String comment,
                                 final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
                                 final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
                                 final List<GeneratedTOBuilder> enclosedGenTOBuilders, final List<EnumBuilder> enumBuilders,
                                 final List<Constant> constants, final List<MethodSignatureBuilder> methodBuilders,
                                 final List<GeneratedPropertyBuilder> propertyBuilders, final Type parentTypeForBuilder) {
        //TODO: not called by actual codebase, fix this up (provide context) if needed - 07/20/2017
        super(packageName, name, null);
        this.parent = parent;
        this.parentTypeForBuilder = parentTypeForBuilder;
        this.comment = comment;
        this.annotations = toUnmodifiableAnnotations(annotationBuilders);
        this.implementsTypes = makeUnmodifiable(implementsTypes);
        this.constants = makeUnmodifiable(constants);
        this.enumerations = toUnmodifiableEnumerations(enumBuilders);
        this.methodSignatures = toUnmodifiableMethods(methodBuilders);
        this.enclosedTypes = toUnmodifiableEnclosedTypes(enclosedGenTypeBuilders, enclosedGenTOBuilders);
        this.properties = toUnmodifiableProperties(propertyBuilders);
        this.isAbstract = isAbstract;
    }

    protected static final <T> List<T> makeUnmodifiable(final List<T> list) {
        switch (list.size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                return Collections.singletonList(list.get(0));
            default:
                return Collections.unmodifiableList(list);
        }
    }

    private static List<GeneratedType> toUnmodifiableEnclosedTypes(final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
                                                                   final List<GeneratedTOBuilder> enclosedGenTOBuilders) {
        final ArrayList<GeneratedType> enclosedTypesList = new ArrayList<>(enclosedGenTypeBuilders.size() + enclosedGenTOBuilders.size());
        enclosedTypesList.addAll(enclosedGenTypeBuilders.stream().filter(builder -> builder != null).map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));

        enclosedTypesList.addAll(enclosedGenTOBuilders.stream().filter(builder -> builder != null).map(GeneratedTOBuilder::toInstance).collect(Collectors.toList()));

        return makeUnmodifiable(enclosedTypesList);
    }

    protected static final List<AnnotationType> toUnmodifiableAnnotations(final List<AnnotationTypeBuilder> annotationBuilders) {
        final List<AnnotationType> annotationList = new ArrayList<>(annotationBuilders.size());
        annotationList.addAll(annotationBuilders.stream().map(AnnotationTypeBuilder::toInstance).collect(Collectors.toList()));
        return makeUnmodifiable(annotationList);
    }

    protected final List<MethodSignature> toUnmodifiableMethods(final List<MethodSignatureBuilder> methodBuilders) {
        final List<MethodSignature> methods = new ArrayList<>(methodBuilders.size());
        methods.addAll(methodBuilders.stream().map(methodBuilder -> methodBuilder.toInstance(this)).collect(Collectors.toList()));
        return makeUnmodifiable(methods);
    }

    protected final List<Enumeration> toUnmodifiableEnumerations(final List<EnumBuilder> enumBuilders) {
        final List<Enumeration> enums = new ArrayList<>(enumBuilders.size());
        enums.addAll(enumBuilders.stream().map(enumBuilder -> enumBuilder.toInstance(this)).collect(Collectors.toList()));
        return makeUnmodifiable(enums);
    }

    protected final List<GeneratedProperty> toUnmodifiableProperties(final List<GeneratedPropertyBuilder> methodBuilders) {
        final List<GeneratedProperty> methods = new ArrayList<>(methodBuilders.size());
        methods.addAll(methodBuilders.stream().map(methodBuilder -> methodBuilder.toInstance(this)).collect(Collectors.toList()));
        return makeUnmodifiable(methods);
    }

    @Override
    public Type getParentType() {
        return this.parent;
    }

    @Override
    public Type getParentTypeForBuilder() {
        return this.parentTypeForBuilder;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return this.annotations;
    }

    @Override
    public boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public List<Type> getImplements() {
        return this.implementsTypes;
    }

    @Override
    public List<GeneratedType> getEnclosedTypes() {
        return this.enclosedTypes;
    }

    @Override
    public List<Enumeration> getEnumerations() {
        return this.enumerations;
    }

    @Override
    public List<Constant> getConstantDefinitions() {
        return this.constants;
    }

    @Override
    public List<MethodSignature> getMethodDefinitions() {
        return this.methodSignatures;
    }

    @Override
    public List<GeneratedProperty> getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("GeneratedType [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        if (this.parent != null) {
            builder.append(", parent=");
            builder.append(this.parent.getFullyQualifiedName());
        } else {
            builder.append(", parent=null");
        }
        builder.append(", comment=");
        builder.append(this.comment);
        builder.append(", annotations=");
        builder.append(this.annotations);
        builder.append(", enclosedTypes=");
        builder.append(this.enclosedTypes);
        builder.append(", enumerations=");
        builder.append(this.enumerations);
        builder.append(", constants=");
        builder.append(this.constants);
        builder.append(", methodSignatures=");
        builder.append(this.methodSignatures);
        builder.append("]");
        return builder.toString();
    }
}
