/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding2.generator.util.Binding2Mapping;
import org.opendaylight.mdsal.binding2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@Beta
public class EnumerationBuilderImpl extends AbstractBaseType implements EnumBuilder {

    private final String packageName;
    private final String name;
    private List<Pair> values = ImmutableList.of();
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();
    private List<Pair> unmodifiableValues  = ImmutableList.of();
    private String description;
    private String reference;
    private Status status;
    private String moduleName;
    private List<QName> schemaPath;

    public EnumerationBuilderImpl(String packageName, String name) {
        super(packageName, name);
        this.packageName = packageName;
        this.name = name;
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

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(String packageName, String name) {
        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
        if (!annotationBuilders.contains(builder)) {
            annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        }
        return builder;
    }

    @Override
    public void addValue(String name, int value, String description, String reference, Status status) {
        final EnumPairImpl p = new EnumPairImpl(name, value, description, reference, status);
        values = LazyCollections.lazyAdd(values, p);
        unmodifiableValues = Collections.unmodifiableList(values);
    }

    @Override
    public Enumeration toInstance(Type definingType) {
        return new EnumerationImpl(definingType, annotationBuilders, packageName, name, values, description,
                reference, status, moduleName, schemaPath);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnumerationBuilderImpl [packageName=");
        builder.append(packageName);
        builder.append(", name=");
        builder.append(name);
        builder.append(", values=");
        builder.append(values);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void updateEnumPairsFromEnumTypeDef(EnumTypeDefinition enumTypeDef) {
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
                            final String reference, final Status status) {

            this.name = name;
            this.mappedName = Binding2Mapping.getClassName(name);
            this.value = value;
            this.description = description;
            this.reference = reference;
            this.status = status;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getMappedName() {
            return mappedName;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Nullable
        @Override
        public String getDescription() {
            return description;
        }

        @Nullable
        @Override
        public String getReference() {
            return reference;
        }

        @Nonnull
        @Override
        public Status getStatus() {
           return status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public boolean equals(Object obj) {
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

            EnumPairImpl other = (EnumPairImpl) obj;

            return Objects.equals(name, other.name) && Objects.equals(value, other.value);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("EnumPair [name=");
            builder.append(name);
            builder.append(", mappedName=");
            builder.append(getMappedName());
            builder.append(", value=");
            builder.append(value);
            builder.append("]");
            return builder.toString();
        }
    }

    private static final class EnumerationImpl extends AbstractBaseType implements Enumeration {

        private final Type definingType;
        private final String description;
        private final String reference;
        private final Status status;
        private final String moduleName;
        private final List<QName> schemaPath;
        private final List<Pair> values;
        private final List<AnnotationType> annotations;

        public EnumerationImpl(final Type definingType, final List<AnnotationTypeBuilder> annotationBuilders,
                               final String packageName, final String name, final List<Pair> values, final String description,
                               final String reference, final Status status, final String moduleName, final
                               List<QName> schemaPath) {
            super(packageName, name);
            this.definingType = definingType;
            this.values = values;
            this.description = description;
            this.reference = reference;
            this.status = status;
            this.moduleName = moduleName;
            this.schemaPath = schemaPath;

            final List<AnnotationType> a = annotationBuilders.stream().map(AnnotationTypeBuilder::toInstance)
                    .collect(Collectors.toList());
            this.annotations = ImmutableList.copyOf(a);
        }

        @Override
        public List<AnnotationType> getAnnotations() {
            return annotations;
        }

        @Override
        public Type getParentType() {
            return definingType;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.of(description);
        }

        @Override
        public List<Pair> getValues() {
            return values;
        }

        @Override
        public String toFormattedString() {
            StringBuilder builder = new StringBuilder();
            builder.append("public enum");
            builder.append(" ");
            builder.append(getName());
            builder.append(" {");
            builder.append("\n");

            int i = 0;
            for (final Enumeration.Pair valPair : values) {
                builder.append("\t");
                builder.append(" ");
                builder.append(valPair.getMappedName());
                builder.append(" (");
                builder.append(valPair.getValue());

                if (i == (values.size() - 1)) {
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
            return Optional.of(reference);
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

        public Status getStatus() {
            return status;
        }
    }
}
