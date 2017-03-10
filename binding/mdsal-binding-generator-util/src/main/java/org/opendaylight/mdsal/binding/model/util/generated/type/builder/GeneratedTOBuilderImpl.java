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
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public final class GeneratedTOBuilderImpl extends AbstractGeneratedTypeBuilder<GeneratedTOBuilder> implements GeneratedTOBuilder {

    private GeneratedTransferObject extendsType;
    private List<GeneratedPropertyBuilder> equalsProperties = Collections.emptyList();
    private List<GeneratedPropertyBuilder> hashProperties = Collections.emptyList();
    private List<GeneratedPropertyBuilder> toStringProperties = Collections.emptyList();
    private boolean isTypedef = false;
    private boolean isUnionType = false;
    private boolean isUnionTypeBuilder = false;
    private TypeDefinition<?> baseType = null;
    private Restrictions restrictions;
    private GeneratedPropertyBuilder SUID;
    private String reference;
    private String description;
    private String moduleName;
    private Iterable<QName> schemaPath;

    public GeneratedTOBuilderImpl(final String packageName, final String name) {
        super(packageName, name);
        setAbstract(false);
    }

    @Override
    public GeneratedTOBuilder setExtendsType(final GeneratedTransferObject genTransObj) {
        Preconditions.checkArgument(genTransObj != null, "Generated Transfer Object cannot be null!");
        this.extendsType = genTransObj;
        return this;
    }

    /**
     * Add new Method Signature definition for Generated Type Builder and
     * returns Method Signature Builder for specifying all Method parameters. <br>
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
    public GeneratedTOBuilder addEqualsIdentity(final GeneratedPropertyBuilder property) {
        this.equalsProperties = LazyCollections.lazyAdd(this.equalsProperties, property);
        return this;
    }

    @Override
    public GeneratedTOBuilder addHashIdentity(final GeneratedPropertyBuilder property) {
        this.hashProperties = LazyCollections.lazyAdd(this.hashProperties, property);
        return this;
    }

    @Override
    public GeneratedTOBuilder addToStringProperty(final GeneratedPropertyBuilder property) {
        this.toStringProperties = LazyCollections.lazyAdd(this.toStringProperties, property);
        return this;
    }

    @Override
    protected GeneratedTOBuilder thisInstance() {
        return this;
    }

    @Override
    public void setRestrictions(final Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public void setSUID(final GeneratedPropertyBuilder suid) {
        this.SUID = suid;
    }

    @Override
    public GeneratedTransferObject toInstance() {
        return new GeneratedTransferObjectImpl(this);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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
        builder.append(this.equalsProperties);
        builder.append(", hashCodeProperties=");
        builder.append(this.hashProperties);
        builder.append(", stringProperties=");
        builder.append(this.toStringProperties);
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append(", methods=");
        builder.append(getMethodDefinitions());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void setTypedef(final boolean isTypedef) {
        this.isTypedef = isTypedef;
    }

    @Override
    public void setBaseType(final TypeDefinition<?> typeDef) {
        this.baseType = typeDef;
    }

    @Override
    public void setIsUnion(final boolean isUnion) {
        this.isUnionType = isUnion;
    }

    @Override
    public void setIsUnionBuilder(final boolean isUnionTypeBuilder) {
        this.isUnionTypeBuilder = isUnionTypeBuilder;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setSchemaPath(final Iterable<QName> schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    private static final class GeneratedTransferObjectImpl extends AbstractGeneratedType implements
    GeneratedTransferObject {

        private final List<GeneratedProperty> equalsProperties;
        private final List<GeneratedProperty> hashCodeProperties;
        private final List<GeneratedProperty> stringProperties;
        private final GeneratedTransferObject extendsType;
        private final boolean isTypedef;
        private final TypeDefinition<?> baseType;
        private final boolean isUnionType;
        private final boolean isUnionTypeBuilder;
        private final Restrictions restrictions;
        private final GeneratedProperty SUID;
        private final String reference;
        private final String description;
        private final String moduleName;
        private final Iterable<QName> schemaPath;

        public GeneratedTransferObjectImpl(final GeneratedTOBuilderImpl builder) {
            super(builder);
            this.extendsType = builder.extendsType;

            // FIXME: if these fields were guaranteed to be constant, we could perhaps
            //        cache and reuse them between instances...
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

            if (builder.SUID == null) {
                this.SUID = null;
            } else {
                this.SUID = builder.SUID.toInstance(GeneratedTransferObjectImpl.this);
            }
        }

        @Override
        public boolean isTypedef() {
            return this.isTypedef;
        }

        @Override
        public TypeDefinition<?> getBaseType() {
            return this.baseType;
        }

        @Override
        public boolean isUnionType() {
            return this.isUnionType;
        }

        @Override
        public boolean isUnionTypeBuilder() {
            return this.isUnionTypeBuilder;
        }

        @Override
        public GeneratedTransferObject getSuperType() {
            return this.extendsType;
        }

        @Override
        public List<GeneratedProperty> getEqualsIdentifiers() {
            return this.equalsProperties;
        }

        @Override
        public List<GeneratedProperty> getHashCodeIdentifiers() {
            return this.hashCodeProperties;
        }

        @Override
        public List<GeneratedProperty> getToStringIdentifiers() {
            return this.stringProperties;
        }

        @Override
        public Restrictions getRestrictions() {
            return this.restrictions;
        }

        @Override
        public GeneratedProperty getSUID() {
            return this.SUID;
        }

        @Override
        public String toString() {
            if (this.isTypedef) {
                return serializeTypedef(this);
            }
            final StringBuilder builder = new StringBuilder();
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
            builder.append(this.equalsProperties);
            builder.append(", hashCodeProperties=");
            builder.append(this.hashCodeProperties);
            builder.append(", stringProperties=");
            builder.append(this.stringProperties);
            builder.append(", methods=");
            builder.append(getMethodDefinitions());
            builder.append("]");
            return builder.toString();
        }

        public String serializeTypedef(final Type type) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) type;
                final StringBuilder sb = new StringBuilder();
                sb.append(parameterizedType.getRawType().getFullyQualifiedName());
                sb.append('<');
                boolean first = true;
                for (final Type parameter : parameterizedType.getActualTypeArguments()) {
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

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getReference() {
            return this.reference;
        }

        @Override
        public Iterable<QName> getSchemaPath() {
            return this.schemaPath;
        }

        @Override
        public String getModuleName() {
            return this.moduleName;
        }
    }
}
