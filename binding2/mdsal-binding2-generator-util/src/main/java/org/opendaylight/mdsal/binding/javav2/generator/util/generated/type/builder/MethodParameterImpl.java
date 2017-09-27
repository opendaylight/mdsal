/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import java.util.Objects;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature.Parameter;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

@Beta
final class MethodParameterImpl implements Parameter {

    private final String name;
    private final Type type;

    public MethodParameterImpl(final String name, final Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
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
        if (!(obj instanceof MethodParameterImpl)) {
            return false;
        }
        MethodParameterImpl other = (MethodParameterImpl) obj;
        return Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MethodParameter [name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type.getPackageName());
        builder.append(".");
        builder.append(type.getName());
        builder.append("]");
        return builder.toString();
    }
}
