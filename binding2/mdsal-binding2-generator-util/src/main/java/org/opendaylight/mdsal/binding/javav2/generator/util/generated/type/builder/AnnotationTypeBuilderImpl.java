/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

@Beta
final class AnnotationTypeBuilderImpl extends AbstractBaseType implements AnnotationTypeBuilder {

    private final String packageName;
    private final String name;
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();
    private List<AnnotationType.Parameter> parameters = ImmutableList.of();

    public AnnotationTypeBuilderImpl(final String packageName, final String name) {
        super(packageName, name, true, null);
        this.packageName = packageName;
        this.name = name;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        if (packageName != null && name != null) {
            final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
            if (!annotationBuilders.contains(builder)) {
                annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
                return builder;
            }
        }
        return null;
    }

    private boolean addParameter(final ParameterImpl param) {
        if (!parameters.contains(param)) {
            parameters = LazyCollections.lazyAdd(parameters, param);
            return true;
        } else {
            return false;
        }
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
    public AnnotationType toInstance() {
        return new AnnotationTypeImpl(packageName, name, annotationBuilders, parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, packageName);
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
        if (!(obj instanceof AnnotationTypeBuilderImpl)) {
            return false;
        }

        AnnotationTypeBuilderImpl other = (AnnotationTypeBuilderImpl) obj;
        return Objects.equals(name, other.name) && Objects.equals(packageName, other.packageName);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AnnotationTypeBuilder [packageName=");
        builder.append(packageName);
        builder.append(", name=");
        builder.append(name);
        builder.append(", annotationBuilders=");
        builder.append(annotationBuilders);
        builder.append(", parameters=");
        builder.append(parameters);
        builder.append("]");
        return builder.toString();
    }

    private static final class AnnotationTypeImpl implements AnnotationType {

        private final String packageName;
        private final String name;
        private final List<AnnotationType> annotations;
        private final List<AnnotationType.Parameter> parameters;
        private final List<String> paramNames;

        public AnnotationTypeImpl(final String packageName, final String name,
                final List<AnnotationTypeBuilder> annotationBuilders,
                final List<AnnotationType.Parameter> parameters) {
            super();
            this.packageName = packageName;
            this.name = name;

            final List<AnnotationType> a = new ArrayList<>();
            for (final AnnotationTypeBuilder builder : annotationBuilders) {
                a.add(builder.toInstance());
            }
            this.annotations = ImmutableList.copyOf(a);

            final List<String> p = new ArrayList<>();
            for (final AnnotationType.Parameter parameter : parameters) {
                p.add(parameter.getName());
            }
            this.paramNames = ImmutableList.copyOf(p);

            this.parameters = parameters.isEmpty() ? ImmutableList.of()
                    : Collections.unmodifiableList(parameters);
        }

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFullyQualifiedName() {
            return packageName + "." + name;
        }

        @Override
        public List<AnnotationType> getAnnotations() {
            return annotations;
        }

        @Override
        public Parameter getParameter(final String paramName) {
            if (paramName != null) {
                for (final AnnotationType.Parameter parameter : parameters) {
                    if (parameter.getName().equals(paramName)) {
                        return parameter;
                    }
                }
            }
            return null;
        }

        @Override
        public List<Parameter> getParameters() {
            return parameters;
        }

        @Override
        public List<String> getParameterNames() {
            return paramNames;
        }

        @Override
        public boolean containsParameters() {
            return !parameters.isEmpty();
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, packageName);
        }

        @Override
        public int compareTo(AnnotationType other) {
            return ComparisonChain.start()
                    .compare(this.name, other.getName())
                    .compare(this.packageName, other.getPackageName())
                    //FIXME: what is natural ordering for AnnotationType?
                    .compare(this.annotations, other.getAnnotations(), Ordering.<AnnotationType>natural().lexicographical())
                    .compare(this.paramNames, other.getParameterNames(), Ordering.<String>natural().lexicographical())
                    .result();
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
            if (!(obj instanceof AnnotationTypeImpl)) {
                return false;
            }

            AnnotationTypeImpl other = (AnnotationTypeImpl) obj;
            return Objects.equals(name, other.name) && Objects.equals(packageName, other.packageName);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AnnotationType [packageName=");
            builder.append(packageName);
            builder.append(", name=");
            builder.append(name);
            builder.append(", annotations=");
            builder.append(annotations);
            builder.append(", parameters=");
            builder.append(parameters);
            builder.append("]");
            return builder.toString();
        }
    }

    private static final class ParameterImpl implements AnnotationType.Parameter {

        private final String name;
        private final String value;
        private final List<String> values;

        public ParameterImpl(final String name, final String value) {
            super();
            this.name = name;
            this.value = value;
            this.values = ImmutableList.of();
        }

        public ParameterImpl(final String name, final List<String> values) {
            super();
            this.name = name;
            this.values = values;
            this.value = null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSingleValue() {
            return value;
        }

        @Override
        public List<String> getValues() {
            return values;
        }

        @Override
        public int hashCode() {
           return Objects.hash(name);
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
            if (!(obj instanceof ParameterImpl)) {
                return false;
            }
            ParameterImpl other = (ParameterImpl) obj;
            return Objects.equals(name, other.name);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ParameterImpl [name=");
            builder.append(name);
            builder.append(", value=");
            builder.append(value);
            builder.append(", values=");
            builder.append(values);
            builder.append("]");
            return builder.toString();
        }
    }
}
