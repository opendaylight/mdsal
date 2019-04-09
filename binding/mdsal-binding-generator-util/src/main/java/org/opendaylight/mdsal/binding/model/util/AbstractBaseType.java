/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;

/**
 * It is used only as ancestor for other <code>Type</code>s.
 */
public class AbstractBaseType implements Type {
    /**
     * Name of this <code>Type</code>.
     */
    private final @NonNull JavaTypeName identifier;

    /**
     * Constructs the instance of this class with a JavaTypeName.
     *
     * @param identifier for this <code>Type</code>
     */
    protected AbstractBaseType(final JavaTypeName identifier) {
        this.identifier = requireNonNull(identifier);
    }

    @Override
    public final JavaTypeName getIdentifier() {
        return this.identifier;
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Type)) {
            return false;
        }
        final Type other = (Type) obj;
        return identifier.equals(other.getIdentifier());
    }

    @Override
    public String toString() {
        return "Type (" + getFullyQualifiedName() + ")";
    }
}
