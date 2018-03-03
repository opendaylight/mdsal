/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.util.AbstractBaseType;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

// FIXME: public because EnumBuilder does not have setters we are exposing
public abstract class AbstractEnumerationBuilder extends AbstractBaseType implements EnumBuilder {
    private List<Enumeration.Pair> values = ImmutableList.of();
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();

    AbstractEnumerationBuilder(final String packageName, final String name) {
        super(packageName, name);
    }

    @Override
    public final AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        if (packageName != null && name != null) {
            final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
            if (!annotationBuilders.contains(builder)) {
                annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
                return builder;
            }
        }
        return null;
    }

    @Override
    public final void addValue(final String name, final int value, final String description) {
        values = LazyCollections.lazyAdd(values, createEnumPair(name, value, description));
    }

    public abstract void setReference(String reference);

    public abstract void setModuleName(String moduleName);

    public abstract void setSchemaPath(Iterable<QName> schemaPath);

    abstract AbstractPair createEnumPair(String name, int value, String description);

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EnumerationBuilderImpl [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append(", values=");
        builder.append(values);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public final void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef) {
        final List<EnumPair> enums = enumTypeDef.getValues();
        if (enums != null) {
            for (EnumPair enumPair : enums) {
                if (enumPair != null) {
                    addValue(enumPair.getName(), enumPair.getValue(), enumPair.getDescription().orElse(null));
                }
            }
        }
    }

    abstract static class AbstractPair implements Enumeration.Pair {
        private final String name;
        private final String mappedName;
        private final int value;

        AbstractPair(final String name, final int value) {
            this.name = name;
            this.mappedName = BindingMapping.getClassName(name);
            this.value = value;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(this.name);
            result = prime * result + Objects.hashCode(this.value);
            return result;
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractPair)) {
                return false;
            }
            final AbstractPair other = (AbstractPair) obj;
            return Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
        }

        @Override
        public final String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("EnumPair [name=");
            builder.append(this.name);
            builder.append(", mappedName=");
            builder.append(getMappedName());
            builder.append(", value=");
            builder.append(this.value);
            builder.append("]");
            return builder.toString();
        }

        @Override
        public final Status getStatus() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    abstract static class AbstractEnumeration extends AbstractBaseType implements Enumeration {

        private final Type definingType;
        private final List<Pair> values;
        private final List<AnnotationType> annotations;

        public AbstractEnumeration(final AbstractEnumerationBuilder builder, final Type definingType) {
            super(builder.getPackageName(), builder.getName());
            this.definingType = definingType;
            this.values = ImmutableList.copyOf(builder.values);

            final ArrayList<AnnotationType> a = new ArrayList<>();
            for (final AnnotationTypeBuilder b : builder.annotationBuilders) {
                a.add(b.toInstance());
            }
            this.annotations = ImmutableList.copyOf(a);
        }

        @Override
        public final Type getParentType() {
            return this.definingType;
        }

        @Override
        public final List<Pair> getValues() {
            return this.values;
        }

        @Override
        public final List<AnnotationType> getAnnotations() {
            return this.annotations;
        }

        @Override
        public final String toFormattedString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("public enum");
            builder.append(" ");
            builder.append(getName());
            builder.append(" {");
            builder.append("\n");

            int i = 0;
            for (final Enumeration.Pair valPair : this.values) {
                builder.append("\t");
                builder.append(" ");
                builder.append(valPair.getMappedName());
                builder.append(" (");
                builder.append(valPair.getValue());

                if (i == this.values.size() - 1) {
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
        public final String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Enumeration [packageName=");
            builder.append(getPackageName());
            if (this.definingType != null) {
                builder.append(", definingType=");
                builder.append(this.definingType.getPackageName());
                builder.append(".");
                builder.append(this.definingType.getName());
            } else {
                builder.append(", definingType= null");
            }
            builder.append(", name=");
            builder.append(getName());
            builder.append(", values=");
            builder.append(this.values);
            builder.append("]");
            return builder.toString();
        }

        @Override
        public final boolean isAbstract() {
            return false;
        }

        @Override
        public final List<Type> getImplements() {
            return Collections.emptyList();
        }

        @Override
        public final List<GeneratedType> getEnclosedTypes() {
            return Collections.emptyList();
        }

        @Override
        public final List<Enumeration> getEnumerations() {
            return Collections.emptyList();
        }

        @Override
        public final List<Constant> getConstantDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public final List<MethodSignature> getMethodDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public final List<GeneratedProperty> getProperties() {
            return Collections.emptyList();
        }
    }
}
