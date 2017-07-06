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
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

@Beta
abstract class AbstractGeneratedTypeBuilder<T extends GeneratedTypeBuilderBase<T>> extends AbstractBaseType
        implements GeneratedTypeBuilderBase<T> {

    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();
    private List<Type> implementsTypes = ImmutableList.of();
    private List<EnumBuilder> enumDefinitions = ImmutableList.of();
    private List<Constant> constants = ImmutableList.of();
    private List<MethodSignatureBuilder> methodDefinitions = ImmutableList.of();
    private final List<GeneratedTypeBuilder> enclosedTypes = ImmutableList.of();
    private List<GeneratedTOBuilder> enclosedTransferObjects = ImmutableList.of();
    private List<GeneratedPropertyBuilder> properties = ImmutableList.of();
    private String comment = "";
    private boolean isAbstract;
    private Type parentTypeForBuilder;

    protected AbstractGeneratedTypeBuilder(final String packageName, final String name, ModuleContext
            context) {
        super(packageName, name, context);
    }

    protected AbstractGeneratedTypeBuilder(final String packageName, final String name, final boolean isNormalized,
                                           JavaIdentifierNormalizer javaIdentifierNormalizer) {
        super(packageName, name, true, null);
    }

    protected AbstractGeneratedTypeBuilder(final String packageName, final String name,
                                           final boolean isPkNameNormalized,
                                           final boolean isTypeNormalized,
                                           ModuleContext context) {
        super(packageName, name, isPkNameNormalized, isTypeNormalized, context);
    }

    protected String getComment() {
        return comment;
    }

    protected List<AnnotationTypeBuilder> getAnnotations() {
        return annotationBuilders;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public List<Type> getImplementsTypes() {
        return implementsTypes;
    }

    protected List<EnumBuilder> getEnumerations() {
        return enumDefinitions;
    }

    protected List<Constant> getConstants() {
        return constants;
    }

    @Override
    public List<MethodSignatureBuilder> getMethodDefinitions() {
        return methodDefinitions;
    }

    protected List<GeneratedTypeBuilder> getEnclosedTypes() {
        return enclosedTypes;
    }

    protected List<GeneratedTOBuilder> getEnclosedTransferObjects() {
        return enclosedTransferObjects;
    }

    protected abstract T thisInstance();

    @Override
    public GeneratedTOBuilder addEnclosingTransferObject(final String name) {
        Preconditions.checkArgument(name != null, "Name for Enclosing Generated Transfer Object cannot be null!");
        GeneratedTOBuilder builder = new GeneratedTOBuilderImpl(getFullyQualifiedName(), name, true);

        Preconditions.checkArgument(!enclosedTransferObjects.contains(builder), "This generated type already contains equal enclosing transfer object.");
        enclosedTransferObjects = LazyCollections.lazyAdd(enclosedTransferObjects, builder);
        return builder;
    }

    @Override
    public T addEnclosingTransferObject(final GeneratedTOBuilder genTOBuilder) {
        Preconditions.checkArgument(genTOBuilder != null, "Parameter genTOBuilder cannot be null!");
        Preconditions.checkArgument(!enclosedTransferObjects.contains(genTOBuilder), "This generated type already contains equal enclosing transfer object.");
        enclosedTransferObjects = LazyCollections.lazyAdd(enclosedTransferObjects, genTOBuilder);
        return thisInstance();
    }

    @Override
    public T addComment(final String comment) {
        this.comment = comment;
        return thisInstance();
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        Preconditions.checkArgument(packageName != null, "Package Name for Annotation Type cannot be null!");
        Preconditions.checkArgument(name != null, "Name of Annotation Type cannot be null!");

        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);

        Preconditions.checkArgument(!annotationBuilders.contains(builder), "This generated type already contains equal annotation.");
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    @Override
    public T setAbstract(final boolean isAbstract) {
        this.isAbstract = isAbstract;
        return thisInstance();
    }

    @Override
    public T addImplementsType(final Type genType) {
        Preconditions.checkArgument(genType != null, "Type cannot be null");
        Preconditions.checkArgument(!implementsTypes.contains(genType), "This generated type already contains equal implements type.");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public Constant addConstant(final Type type, final String name, final Object value) {
        Preconditions.checkArgument(type != null, "Returning Type for Constant cannot be null!");
        Preconditions.checkArgument(name != null, "Name of constant cannot be null!");
        Preconditions.checkArgument(!containsConstant(name), "This generated type already contains constant with the same name.");

        final Constant constant = new ConstantImpl(this, type, name, value);
        constants = LazyCollections.lazyAdd(constants, constant);
        return constant;
    }

    @Override
    public Type setParentTypeForBuilder(Type type) {
        return this.parentTypeForBuilder = type;
    }

    public boolean containsConstant(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (Constant constant : constants) {
            if (name.equals(constant.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumBuilder addEnumeration(final String name, ModuleContext context) {
        Preconditions.checkArgument(name != null, "Name of enumeration cannot be null!");
        final EnumBuilder builder = new EnumerationBuilderImpl(getFullyQualifiedName(), name, true, false,
                context);

        Preconditions.checkArgument(!enumDefinitions.contains(builder), "This generated type already contains equal enumeration.");
        enumDefinitions = LazyCollections.lazyAdd(enumDefinitions, builder);
        return builder;
    }

    @Override
    public MethodSignatureBuilder addMethod(final String name) {
        Preconditions.checkArgument(name != null, "Name of method cannot be null!");
        final MethodSignatureBuilder builder = new MethodSignatureBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        builder.setAbstract(true);
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, builder);
        return builder;
    }

    @Override
    public boolean containsMethod(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (MethodSignatureBuilder methodDefinition : methodDefinitions) {
            if (name.equals(methodDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GeneratedPropertyBuilder addProperty(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        Preconditions.checkArgument(!containsProperty(name), "This generated type already contains property with the same name.");

        final GeneratedPropertyBuilder builder = new GeneratedPropertyBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        properties = LazyCollections.lazyAdd(properties, builder);
        return builder;
    }

    @Override
    public boolean containsProperty(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (GeneratedPropertyBuilder property : properties) {
            if (name.equals(property.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPackageName());
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

        if (!(obj instanceof AbstractGeneratedTypeBuilder)) {
            return false;
        }

        AbstractGeneratedTypeBuilder<?> other = (AbstractGeneratedTypeBuilder<?>) obj;
        return Objects.equals(getFullyQualifiedName(), other.getFullyQualifiedName());
    }

    public Type getParent() {
        return null;
    }

    public Type getParentTypeForBuilder() {
        return parentTypeForBuilder;
    }

    @Override
    public List<GeneratedPropertyBuilder> getProperties() {
        return properties;
    }

}
