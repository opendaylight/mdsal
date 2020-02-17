/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * It is used only as ancestor for other <code>Type</code>s. Note this forms the equality domain over most types, please
 * consider joining the party.
 */
@Beta
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
    public final int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Type)) {
            return false;
        }
        return identifier.equals(((Type) obj).getIdentifier());
    }

    @Override
    public String toString() {
        return "Type (" + getFullyQualifiedName() + ")";
    }
}
