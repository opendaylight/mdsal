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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

@Beta
final class MethodSignatureBuilderImpl extends AbstractTypeMemberBuilder<MethodSignatureBuilder>
        implements MethodSignatureBuilder {

    private List<MethodSignature.Parameter> parameters = ImmutableList.of();
    private List<MethodSignature.Parameter> unmodifiableParams  = ImmutableList.of();
    private boolean isAbstract;

    MethodSignatureBuilderImpl(final String name) {
        super(name);
    }

    @Override
    public MethodSignatureBuilder setAbstract(final boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    @Override
    public MethodSignatureBuilder addParameter(final Type type, final String name) {
        parameters = LazyCollections.lazyAdd(parameters, new MethodParameterImpl(name, type));
        unmodifiableParams = Collections.unmodifiableList(parameters);
        return this;
    }

    @Override
    protected MethodSignatureBuilder thisInstance() {
        return this;
    }

    @Override
    public MethodSignature toInstance(final Type definingType) {
        final List<AnnotationType> annotations = toAnnotationTypes();
        return new MethodSignatureImpl(definingType, getName(), annotations, getComment(), getAccessModifier(),
                getReturnType(), unmodifiableParams, isFinal(), isAbstract, isStatic());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), parameters, getReturnType());
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
        MethodSignatureBuilderImpl other = (MethodSignatureBuilderImpl) obj;
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(getReturnType(), other.getReturnType())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MethodSignatureBuilderImpl [name=");
        builder.append(getName());
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", parameters=");
        builder.append(parameters);
        builder.append(", annotationBuilders=");
        builder.append(getAnnotationBuilders());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append("]");
        return builder.toString();
    }
}
