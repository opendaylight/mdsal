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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@Beta
public class EnumerationBuilderImpl extends AbstractBaseType implements EnumBuilder {

    private List<Pair> values = ImmutableList.of();
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();
    private String description;
    private String reference;
    private String moduleName;
    private List<QName> schemaPath;

    public EnumerationBuilderImpl(final String packageName, final String name, ModuleContext context) {
        super(packageName, name, context);
    }

    public EnumerationBuilderImpl(final String packageName, final String name, final boolean isPkNameNormalized,
            final boolean isTypeNormalized, ModuleContext context) {
        super(packageName, name, isPkNameNormalized, isTypeNormalized, context);
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    public void setSchemaPath(final List<QName> schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
        if (!this.annotationBuilders.contains(builder)) {
            this.annotationBuilders = LazyCollections.lazyAdd(this.annotationBuilders, builder);
        }
        return builder;
    }

    @Override
    public void addValue(final String name, final int value, final String description, final String reference, final Status status) {
        final EnumPairImpl p = new EnumPairImpl(name, value, description, reference, status, this.values);
        this.values = LazyCollections.lazyAdd(this.values, p);
    }

    @Override
    public Enumeration toInstance(final Type definingType) {
        return new EnumerationImpl(definingType, this.annotationBuilders, this.packageName, this.name, this.values, this.description,
                this.reference, this.moduleName, this.schemaPath);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EnumerationBuilderImpl [packageName=");
        builder.append(this.packageName);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", values=");
        builder.append(this.values);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef) {
        final List<EnumPair> enums = enumTypeDef.getValues();
        if (enums != null) {
            enums.stream().filter(enumPair -> enumPair != null).forEach(enumPair -> this.addValue(enumPair.getName(),
                    enumPair.getValue(), enumPair.getDescription(), enumPair.getReference(), enumPair.getStatus()));
        }
    }

    private static final class EnumPairImpl implements Enumeration.Pair {

        private final String name;
        private final String mappedName;
        private final int value;
        private final String description;
        private final String reference;
        private final Status status;

        public EnumPairImpl(final String name, final int value, final String description,
                final String reference, final Status status, final List<Pair> values) {

            this.name = name;
            this.mappedName = JavaIdentifierNormalizer.normalizeEnumValueIdentifier(name, values);
            this.value = value;
            this.description = description;
            this.reference = reference;
            this.status = status;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getMappedName() {
            return this.mappedName;
        }

        @Override
        public int getValue() {
            return this.value;
        }

        @Nullable
        @Override
        public String getDescription() {
            return this.description;
        }

        @Nullable
        @Override
        public String getReference() {
            return this.reference;
        }

        @Nonnull
        @Override
        public Status getStatus() {
           return this.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.value);
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

            if (!(obj instanceof EnumPairImpl)) {
                return false;
            }

            final EnumPairImpl other = (EnumPairImpl) obj;

            return Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("EnumPair [name=");
            builder.append(this.name);
            builder.append(", mappedName=");
            builder.append(getMappedName());
            builder.append(", value=");
            builder.append(this.value);
            builder.append(']');
            return builder.toString();
        }
    }

    private static final class EnumerationImpl extends AbstractBaseType implements Enumeration {

        private final Type definingType;
        private final String description;
        private final String reference;
        private final String moduleName;
        private final List<QName> schemaPath;
        private final List<Pair> values;
        private final List<AnnotationType> annotations;

        public EnumerationImpl(final Type definingType, final List<AnnotationTypeBuilder> annotationBuilders,
                               final String packageName, final String name, final List<Pair> values, final String description,
                final String reference, final String moduleName, final List<QName> schemaPath) {
            super(packageName, name, true, null);
            this.definingType = definingType;
            this.values = values;
            this.description = description;
            this.reference = reference;
            this.moduleName = moduleName;
            this.schemaPath = schemaPath;

            final List<AnnotationType> a = annotationBuilders.stream().map(AnnotationTypeBuilder::toInstance)
                    .collect(Collectors.toList());
            this.annotations = ImmutableList.copyOf(a);
        }

        @Override
        public List<AnnotationType> getAnnotations() {
            return this.annotations;
        }

        @Override
        public Type getParentType() {
            return this.definingType;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(this.description);
        }

        @Override
        public List<Pair> getValues() {
            return this.values;
        }

        @Override
        public String toFormattedString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("public enum");
            builder.append(' ');
            builder.append(getName());
            builder.append(" {");
            builder.append("\n");

            int i = 0;
            for (final Enumeration.Pair valPair : this.values) {
                builder.append("\t");
                builder.append(' ');
                builder.append(valPair.getMappedName());
                builder.append(" (");
                builder.append(valPair.getValue());

                if (i == (this.values.size() - 1)) {
                    builder.append(" );");
                } else {
                    builder.append(" ),");
                }
                ++i;
            }
            builder.append("\n}");
            return builder.toString();
        }

        @Override
        public Optional<String> getReference() {
            return Optional.ofNullable(this.reference);
        }

        @Override
        public List<QName> getSchemaPath() {
            return this.schemaPath;
        }

        @Override
        public String getModuleName() {
            return this.moduleName;
        }

        @Override
        public String getComment() {
            //noop
            return null;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public List<Type> getImplements() {
            return ImmutableList.of();
        }

        @Override
        public List<GeneratedType> getEnclosedTypes() {
            return ImmutableList.of();
        }

        @Override
        public List<Enumeration> getEnumerations() {
            return ImmutableList.of();
        }

        @Override
        public List<Constant> getConstantDefinitions() {
            return ImmutableList.of();
        }

        @Override
        public List<MethodSignature> getMethodDefinitions() {
            return ImmutableList.of();
        }

        @Override
        public List<GeneratedProperty> getProperties() {
            return ImmutableList.of();
        }

        @Override
        public Type getParentTypeForBuilder() {
            return null;
        }
    }
}
