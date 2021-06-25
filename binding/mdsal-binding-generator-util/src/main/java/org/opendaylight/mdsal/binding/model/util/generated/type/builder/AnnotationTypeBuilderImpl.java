/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.AbstractType;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

final class AnnotationTypeBuilderImpl extends AbstractType implements AnnotationTypeBuilder {

    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private List<AnnotationType.Parameter> parameters = Collections.emptyList();

    AnnotationTypeBuilderImpl(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        if (packageName != null && name != null) {
            final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(JavaTypeName.create(packageName, name));
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
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper)
            .omitNullValues()
            .add("annotationBuilders", annotationBuilders)
            .add("parameters", parameters);
    }

    private static final class AnnotationTypeImpl extends AbstractType implements AnnotationType {
        private final List<AnnotationType> annotations;
        private final List<AnnotationType.Parameter> parameters;
        private final List<String> paramNames;

        AnnotationTypeImpl(final JavaTypeName identifier, final List<AnnotationTypeBuilder> annotationBuilders,
                final List<AnnotationType.Parameter> parameters) {
            super(identifier);

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

            this.parameters = parameters.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(parameters);
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
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper)
                .omitNullValues()
                .add("annotations", annotations)
                .add("parameters", parameters);
        }
    }

    private static final class ParameterImpl implements AnnotationType.Parameter {

        private final String name;
        private final String value;
        private final List<String> values;

        ParameterImpl(final String name, final String value) {
            this.name = name;
            this.value = value;
            this.values = Collections.emptyList();
        }

        ParameterImpl(final String name, final List<String> values) {
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
