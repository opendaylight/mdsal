/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Type;

final class ConstantImpl implements Constant {

    private final Type definingType;
    private final Type type;
    private final String name;
    private final Object value;

    public ConstantImpl(final Type definingType, final Type type, final String name, final Object value) {
        this.definingType = definingType;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public Type getDefiningType() {
        return this.definingType;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public String toFormattedString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.type);
        builder.append(" ");
        builder.append(this.name);
        builder.append(" ");
        builder.append(this.value);
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Objects.hashCode(this.name);
        result = (prime * result) + Objects.hashCode(this.type);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        final ConstantImpl other = (ConstantImpl) obj;
        return Objects.equals(this.name, other.name) && Objects.equals(this.type, other.type) && Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Constant [type=");
        builder.append(this.type);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", value=");
        builder.append(this.value);
        if (this.definingType != null) {
            builder.append(", definingType=");
            builder.append(this.definingType.getPackageName());
            builder.append(".");
            builder.append(this.definingType.getName());
        } else {
            builder.append(", definingType= null");
        }
        builder.append("]");
        return builder.toString();
    }
}
