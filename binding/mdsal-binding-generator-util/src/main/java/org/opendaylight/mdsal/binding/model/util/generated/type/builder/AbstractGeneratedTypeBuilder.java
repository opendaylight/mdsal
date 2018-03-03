/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.AbstractBaseType;
import org.opendaylight.yangtools.util.LazyCollections;

abstract class AbstractGeneratedTypeBuilder<T extends GeneratedTypeBuilderBase<T>> extends AbstractBaseType
        implements GeneratedTypeBuilderBase<T> {

    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private List<Type> implementsTypes = Collections.emptyList();
    private List<EnumBuilder> enumDefinitions = Collections.emptyList();
    private List<Constant> constants = Collections.emptyList();
    private List<MethodSignatureBuilder> methodDefinitions = Collections.emptyList();
    private final List<GeneratedTypeBuilder> enclosedTypes = Collections.emptyList();
    private List<GeneratedTOBuilder> enclosedTransferObjects = Collections.emptyList();
    private List<GeneratedPropertyBuilder> properties = Collections.emptyList();
    private TypeComment comment;
    private boolean isAbstract;
    private YangSourceDefinition yangSourceDefinition;

    protected AbstractGeneratedTypeBuilder(final String packageName, final String name) {
        super(packageName, name);
    }

    protected TypeComment getComment() {
        return this.comment;
    }

    protected List<AnnotationTypeBuilder> getAnnotations() {
        return this.annotationBuilders;
    }

    @Override
    public boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public List<Type> getImplementsTypes() {
        return this.implementsTypes;
    }

    protected List<EnumBuilder> getEnumerations() {
        return this.enumDefinitions;
    }

    protected List<Constant> getConstants() {
        return this.constants;
    }

    @Override
    public List<MethodSignatureBuilder> getMethodDefinitions() {
        return this.methodDefinitions;
    }

    protected List<GeneratedTypeBuilder> getEnclosedTypes() {
        return this.enclosedTypes;
    }

    protected List<GeneratedTOBuilder> getEnclosedTransferObjects() {
        return this.enclosedTransferObjects;
    }

    protected abstract T thisInstance();

    abstract AbstractEnumerationBuilder newEnumerationBuilder(String packageName, String name);

    @Override
    public GeneratedTOBuilder addEnclosingTransferObject(final String name) {
        Preconditions.checkArgument(name != null, "Name for Enclosing Generated Transfer Object cannot be null!");
        final GeneratedTOBuilder builder = new CodegenGeneratedTOBuilder(getFullyQualifiedName(), name);

        Preconditions.checkArgument(!this.enclosedTransferObjects.contains(builder),
            "This generated type already contains equal enclosing transfer object.");
        this.enclosedTransferObjects = LazyCollections.lazyAdd(this.enclosedTransferObjects, builder);
        return builder;
    }

    @Override
    public T addEnclosingTransferObject(final GeneratedTOBuilder genTOBuilder) {
        Preconditions.checkArgument(genTOBuilder != null, "Parameter genTOBuilder cannot be null!");
        Preconditions.checkArgument(!this.enclosedTransferObjects.contains(genTOBuilder),
            "This generated type already contains equal enclosing transfer object.");
        this.enclosedTransferObjects = LazyCollections.lazyAdd(this.enclosedTransferObjects, genTOBuilder);
        return thisInstance();
    }

    @Override
    public T addComment(final TypeComment comment) {
        this.comment = Preconditions.checkNotNull(comment);
        return thisInstance();
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        Preconditions.checkArgument(packageName != null, "Package Name for Annotation Type cannot be null!");
        Preconditions.checkArgument(name != null, "Name of Annotation Type cannot be null!");

        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);

        Preconditions.checkArgument(!this.annotationBuilders.contains(builder),
            "This generated type already contains equal annotation.");
        this.annotationBuilders = LazyCollections.lazyAdd(this.annotationBuilders, builder);
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
        Preconditions.checkArgument(!this.implementsTypes.contains(genType),
            "This generated type already contains equal implements type.");
        this.implementsTypes = LazyCollections.lazyAdd(this.implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public Constant addConstant(final Type type, final String name, final Object value) {
        Preconditions.checkArgument(type != null, "Returning Type for Constant cannot be null!");
        Preconditions.checkArgument(name != null, "Name of constant cannot be null!");
        Preconditions.checkArgument(!containsConstant(name),
            "This generated type already contains constant with the same name.");

        final Constant constant = new ConstantImpl(this, type, name, value);
        this.constants = LazyCollections.lazyAdd(this.constants, constant);
        return constant;
    }

    public boolean containsConstant(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (final Constant constant : this.constants) {
            if (name.equals(constant.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumBuilder addEnumeration(final String name) {
        Preconditions.checkArgument(name != null, "Name of enumeration cannot be null!");
        // FIXME:
        final EnumBuilder builder = new CodegenEnumerationBuilder(getFullyQualifiedName(), name);

        Preconditions.checkArgument(!this.enumDefinitions.contains(builder),
            "This generated type already contains equal enumeration.");
        this.enumDefinitions = LazyCollections.lazyAdd(this.enumDefinitions, builder);
        return builder;
    }

    @Override
    public MethodSignatureBuilder addMethod(final String name) {
        Preconditions.checkArgument(name != null, "Name of method cannot be null!");
        final MethodSignatureBuilder builder = new MethodSignatureBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        builder.setAbstract(true);
        this.methodDefinitions = LazyCollections.lazyAdd(this.methodDefinitions, builder);
        return builder;
    }

    @Override
    public boolean containsMethod(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (final MethodSignatureBuilder methodDefinition : this.methodDefinitions) {
            if (name.equals(methodDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GeneratedPropertyBuilder addProperty(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        Preconditions.checkArgument(!containsProperty(name),
            "This generated type already contains property with the same name.");

        final GeneratedPropertyBuilder builder = new GeneratedPropertyBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        this.properties = LazyCollections.lazyAdd(this.properties, builder);
        return builder;
    }

    @Override
    public boolean containsProperty(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (final GeneratedPropertyBuilder property : this.properties) {
            if (name.equals(property.getName())) {
                return true;
            }
        }
        return false;
    }

    public Type getParent() {
        return null;
    }

    @Override
    public List<GeneratedPropertyBuilder> getProperties() {
        return this.properties;
    }

    @Override
    public Optional<YangSourceDefinition> getYangSourceDefinition() {
        return Optional.ofNullable(yangSourceDefinition);
    }


    @Override
    public void setYangSourceDefinition(final YangSourceDefinition definition) {
        yangSourceDefinition = Preconditions.checkNotNull(definition);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getPackageName());
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
        final AbstractGeneratedTypeBuilder<?> other = (AbstractGeneratedTypeBuilder<?>) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getPackageName(), other.getPackageName());
    }
}
