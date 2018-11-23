/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

final class MethodSignatureBuilderImpl extends AbstractTypeMemberBuilder<MethodSignatureBuilder>
        implements MethodSignatureBuilder {

    private List<MethodSignature.Parameter> parameters = Collections.emptyList();
    private List<MethodSignature.Parameter> unmodifiableParams = Collections.emptyList();
    private boolean isAbstract;
    private boolean isDefault;

    MethodSignatureBuilderImpl(final String name) {
        super(name);
    }

    @Override
    public MethodSignatureBuilder setAbstract(final boolean newIsAbstract) {
        this.isAbstract = newIsAbstract;
        return this;
    }

    @Override
    public MethodSignatureBuilder setDefault(final boolean newIsDefault) {
        this.isDefault = newIsDefault;
        return this;
    }

    @Override
    public MethodSignatureBuilder addParameter(final Type type, final String name) {
        this.parameters = LazyCollections.lazyAdd(this.parameters, new MethodParameterImpl(name, type));
        this.unmodifiableParams = Collections.unmodifiableList(this.parameters);
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
                getReturnType(), this.unmodifiableParams, isFinal(), this.isAbstract, isStatic(), isDefault);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(this.parameters);
        result = prime * result + Objects.hashCode(getReturnType());
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
        final MethodSignatureBuilderImpl other = (MethodSignatureBuilderImpl) obj;
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(getReturnType(), other.getReturnType())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MethodSignatureBuilderImpl [name=");
        builder.append(getName());
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append(", annotationBuilders=");
        builder.append(getAnnotationBuilders());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append("]");
        return builder.toString();
    }
}
