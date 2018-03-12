/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.TypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.AbstractBaseType;
import org.opendaylight.yangtools.util.LazyCollections;

final class AnnotationTypeBuilderImpl extends AbstractBaseType implements AnnotationTypeBuilder {

    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private List<AnnotationType.Parameter> parameters = Collections.emptyList();

    AnnotationTypeBuilderImpl(final TypeName identifier) {
        super(identifier);
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        if (packageName != null && name != null) {
            final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(getIdentifier());
            if (!this.annotationBuilders.contains(builder)) {
                this.annotationBuilders = LazyCollections.lazyAdd(this.annotationBuilders, builder);
                return builder;
            }
        }
        return null;
    }

    private boolean addParameter(final ParameterImpl param) {
        if (parameters.contains(param)) {
            return false;
        }
        parameters = LazyCollections.lazyAdd(parameters, param);
        return true;
    }

    @Override
    public boolean addParameter(final String paramName, final String value) {
        if (paramName != null && value != null) {
            final ParameterImpl param = new ParameterImpl(paramName, value);
            return addParameter(param);
        }
        return false;
    }

    @Override
    public boolean addParameters(final String paramName, final List<String> values) {
        if (paramName != null && values != null) {
            final ParameterImpl param = new ParameterImpl(paramName, values);
            return addParameter(param);
        }
        return false;
    }

    @Override
    public AnnotationType build() {
        return new AnnotationTypeImpl(getIdentifier(), this.annotationBuilders, this.parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return getIdentifier().equals(((AnnotationTypeBuilderImpl) obj).getIdentifier());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AnnotationTypeBuilder [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append(", annotationBuilders=");
        builder.append(this.annotationBuilders);
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append("]");
        return builder.toString();
    }

    private static final class AnnotationTypeImpl implements AnnotationType {
        private final TypeName identifier;
        private final List<AnnotationType> annotations;
        private final List<AnnotationType.Parameter> parameters;
        private final List<String> paramNames;

        AnnotationTypeImpl(final TypeName identifier, final List<AnnotationTypeBuilder> annotationBuilders,
                final List<AnnotationType.Parameter> parameters) {
            this.identifier = requireNonNull(identifier);

            final List<AnnotationType> a = new ArrayList<>();
            for (final AnnotationTypeBuilder builder : annotationBuilders) {
                a.add(builder.build());
            }
            this.annotations = ImmutableList.copyOf(a);

            final List<String> p = new ArrayList<>();
            for (final AnnotationType.Parameter parameter : parameters) {
                p.add(parameter.getName());
            }
            this.paramNames = ImmutableList.copyOf(p);

            this.parameters = parameters.isEmpty() ? Collections.<AnnotationType.Parameter>emptyList()
                    : Collections.unmodifiableList(parameters);
        }

        @Override
        public TypeName getIdentifier() {
            return identifier;
        }

        @Override
        public List<AnnotationType> getAnnotations() {
            return this.annotations;
        }

        @Override
        public Parameter getParameter(final String paramName) {
            if (paramName != null) {
                for (final AnnotationType.Parameter parameter : this.parameters) {
                    if (parameter.getName().equals(paramName)) {
                        return parameter;
                    }
                }
            }
            return null;
        }

        @Override
        public List<Parameter> getParameters() {
            return this.parameters;
        }

        @Override
        public List<String> getParameterNames() {
            return this.paramNames;
        }

        @Override
        public boolean containsParameters() {
            return !this.parameters.isEmpty();
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final AnnotationTypeImpl other = (AnnotationTypeImpl) obj;
            return identifier.equals(other.identifier);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("AnnotationType [packageName=");
            builder.append(getPackageName());
            builder.append(", name=");
            builder.append(getName());
            builder.append(", annotations=");
            builder.append(this.annotations);
            builder.append(", parameters=");
            builder.append(this.parameters);
            builder.append("]");
            return builder.toString();
        }
    }

    private static final class ParameterImpl implements AnnotationType.Parameter {

        private final String name;
        private final String value;
        private final List<String> values;

        public ParameterImpl(final String name, final String value) {
            this.name = name;
            this.value = value;
            this.values = Collections.emptyList();
        }

        public ParameterImpl(final String name, final List<String> values) {
            this.name = name;
            this.values = values;
            this.value = null;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public List<String> getValues() {
            return this.values;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(this.name);
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
            final ParameterImpl other = (ParameterImpl) obj;
            return Objects.equals(this.name, other.name);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("ParameterImpl [name=");
            builder.append(this.name);
            builder.append(", value=");
            builder.append(this.value);
            builder.append(", values=");
            builder.append(this.values);
            builder.append("]");
            return builder.toString();
        }
    }
}
