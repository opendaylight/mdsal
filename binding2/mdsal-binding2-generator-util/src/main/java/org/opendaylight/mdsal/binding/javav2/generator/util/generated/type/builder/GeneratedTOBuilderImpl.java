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
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
public final class GeneratedTOBuilderImpl extends AbstractGeneratedTypeBuilder<GeneratedTOBuilder> implements
        GeneratedTOBuilder {

    private GeneratedTransferObject extendsType;
    private List<GeneratedPropertyBuilder> equalsProperties = ImmutableList.of();
    private List<GeneratedPropertyBuilder> hashProperties = ImmutableList.of();
    private List<GeneratedPropertyBuilder> toStringProperties = ImmutableList.of();
    private boolean isTypedef = false;
    private boolean isUnionType = false;
    private boolean isUnionTypeBuilder = false;
    private TypeDefinition<?> baseType = null;
    private Restrictions restrictions;
    private GeneratedPropertyBuilder suid;
    private String reference;
    private String description;
    private String moduleName;
    private List<QName> schemaPath;

    public GeneratedTOBuilderImpl(final String packageName, final String name, ModuleContext context) {
        super(packageName, name, context);
        setAbstract(false);
    }

    public GeneratedTOBuilderImpl(final String packageName, final String name, final boolean isNormalized) {
        super(packageName, name, true, null);
    }

    public GeneratedTOBuilderImpl(final String packageName, final String name, final boolean isPkNameNormalized,
            final boolean isTypeNormalized, ModuleContext context) {
        super(packageName, name, isPkNameNormalized, isTypeNormalized, context);
        setAbstract(false);
    }

    @Override
    public GeneratedTOBuilder setExtendsType(GeneratedTransferObject genTransObj) {
        Preconditions.checkArgument(genTransObj != null, "Generated Transfer Object cannot be null!");
        extendsType = genTransObj;
        return this;
    }

    /**
     * Add new MethodSignature definition for GeneratedTypeBuilder and
     * returns MethodSignatureBuilder for specifying all Method parameters. <br>
     * Name of Method cannot be <code>null</code>, if it is <code>null</code>
     * the method SHOULD throw {@link IllegalArgumentException} <br>
     * By <i>Default</i> the MethodSignatureBuilder SHOULD be pre-set as
     * {@link MethodSignatureBuilder#setAbstract(boolean)},
     * {TypeMemberBuilder#setFinal(boolean)} and
     * {TypeMemberBuilder#setAccessModifier(boolean)}
     *
     * @param name
     *            Name of Method
     * @return <code>new</code> instance of Method Signature Builder.
     */
    @Override
    public MethodSignatureBuilder addMethod(final String name) {
        final MethodSignatureBuilder builder = super.addMethod(name);
        builder.setAbstract(false);
        return builder;
    }

    @Override
    public GeneratedTOBuilder addEqualsIdentity(GeneratedPropertyBuilder property) {
        equalsProperties = LazyCollections.lazyAdd(equalsProperties, property);
        return this;
    }

    @Override
    public GeneratedTOBuilder addHashIdentity(GeneratedPropertyBuilder property) {
        hashProperties = LazyCollections.lazyAdd(hashProperties, property);
        return this;
    }

    @Override
    public GeneratedTOBuilder addToStringProperty(GeneratedPropertyBuilder property) {
        toStringProperties = LazyCollections.lazyAdd(toStringProperties, property);
        return this;
    }

    @Override
    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public GeneratedTransferObject toInstance() {
        return new GeneratedTransferObjectImpl(this);
    }

    @Override
    public void setTypedef(boolean isTypedef) {
        this.isTypedef = isTypedef;
    }

    @Override
    public void setBaseType(TypeDefinition<?> typeDef) {
        this.baseType = typeDef;
    }

    @Override
    public void setIsUnion(boolean isUnion) {
        this.isUnionType = isUnion;
    }

    @Override
    public void setIsUnionBuilder(boolean isUnionTypeBuilder) {
        this.isUnionTypeBuilder = isUnionTypeBuilder;
    }

    @Override
    public void setSUID(GeneratedPropertyBuilder suid) {
        this.suid = suid;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setSchemaPath(List<QName> schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    protected GeneratedTOBuilderImpl thisInstance() {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedTransferObject [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append(", constants=");
        builder.append(getConstants());
        builder.append(", enumerations=");
        builder.append(getEnumerations());
        builder.append(", equalsProperties=");
        builder.append(equalsProperties);
        builder.append(", hashCodeProperties=");
        builder.append(hashProperties);
        builder.append(", stringProperties=");
        builder.append(toStringProperties);
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append(", methods=");
        builder.append(getMethodDefinitions());
        builder.append("]");
        return builder.toString();
    }

    private static final class GeneratedTransferObjectImpl extends AbstractGeneratedType implements GeneratedTransferObject {

        private final List<GeneratedProperty> equalsProperties;
        private final List<GeneratedProperty> hashCodeProperties;
        private final List<GeneratedProperty> stringProperties;
        private final GeneratedTransferObject extendsType;
        private final boolean isTypedef;
        private final TypeDefinition<?> baseType;
        private final boolean isUnionType;
        private final boolean isUnionTypeBuilder;
        private final Restrictions restrictions;
        private final GeneratedProperty innerSuid;
        private final String reference;
        private final String description;
        private final String moduleName;
        private final List<QName> schemaPath;

        public GeneratedTransferObjectImpl(final GeneratedTOBuilderImpl builder) {
            super(builder);

            this.extendsType = builder.extendsType;
            this.equalsProperties = toUnmodifiableProperties(builder.equalsProperties);
            this.hashCodeProperties = toUnmodifiableProperties(builder.hashProperties);
            this.stringProperties = toUnmodifiableProperties(builder.toStringProperties);

            this.isTypedef = builder.isTypedef;
            this.baseType = builder.baseType;
            this.isUnionType = builder.isUnionType;
            this.isUnionTypeBuilder = builder.isUnionTypeBuilder;
            this.restrictions = builder.restrictions;
            this.reference = builder.reference;
            this.description = builder.description;
            this.moduleName = builder.moduleName;
            this.schemaPath = builder.schemaPath;

            if (builder.suid == null) {
                this.innerSuid = null;
            } else {
                this.innerSuid = builder.suid.toInstance(GeneratedTransferObjectImpl.this);
            }
        }

        @Override
        public GeneratedProperty getSUID() {
            return innerSuid;
        }

        @Override
        public GeneratedTransferObject getSuperType() {
            return extendsType;
        }

        @Override
        public List<GeneratedProperty> getEqualsIdentifiers() {
            return equalsProperties;
        }

        @Override
        public List<GeneratedProperty> getHashCodeIdentifiers() {
            return hashCodeProperties;
        }

        @Override
        public List<GeneratedProperty> getToStringIdentifiers() {
            return stringProperties;
        }

        @Override
        public boolean isTypedef() {
            return isTypedef;
        }

        @Override
        public TypeDefinition<?> getBaseType() {
            return baseType;
        }

        @Override
        public boolean isUnionType() {
            return isUnionType;
        }

        @Override
        public boolean isUnionTypeBuilder() {
            return isUnionTypeBuilder;
        }

        @Override
        public Restrictions getRestrictions() {
            return restrictions;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(this.description);
        }

        @Override
        public Optional<String> getReference() {
            return Optional.ofNullable(this.reference);
        }

        @Override
        public List<QName> getSchemaPath() {
            return schemaPath;
        }

        @Override
        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String toString() {
            if (isTypedef) {
                return serializeTypedef(this);
            }
            StringBuilder builder = new StringBuilder();
            builder.append("GeneratedTransferObject [packageName=");
            builder.append(getPackageName());
            builder.append(", name=");
            builder.append(getName());
            builder.append(", comment=");
            builder.append(", annotations=");
            builder.append(getAnnotations());
            builder.append(getComment());
            builder.append(", extends=");
            builder.append(getSuperType());
            builder.append(", implements=");
            builder.append(getImplements());
            builder.append(", enclosedTypes=");
            builder.append(getEnclosedTypes());
            builder.append(", constants=");
            builder.append(getConstantDefinitions());
            builder.append(", enumerations=");
            builder.append(getEnumerations());
            builder.append(", properties=");
            builder.append(getProperties());
            builder.append(", equalsProperties=");
            builder.append(equalsProperties);
            builder.append(", hashCodeProperties=");
            builder.append(hashCodeProperties);
            builder.append(", stringProperties=");
            builder.append(stringProperties);
            builder.append(", methods=");
            builder.append(getMethodDefinitions());
            builder.append("]");
            return builder.toString();
        }

        private String serializeTypedef(final Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                StringBuilder sb = new StringBuilder();
                sb.append(parameterizedType.getRawType().getFullyQualifiedName());
                sb.append('<');
                boolean first = true;
                for (Type parameter : parameterizedType.getActualTypeArguments()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(',');
                    }
                    sb.append(serializeTypedef(parameter));
                }
                sb.append('>');
                return sb.toString();
            } else {
                return type.getFullyQualifiedName();
            }
        }
    }
}
