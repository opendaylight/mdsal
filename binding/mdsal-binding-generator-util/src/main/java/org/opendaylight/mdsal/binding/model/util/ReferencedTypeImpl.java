/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import org.opendaylight.mdsal.binding.model.api.AbstractBaseType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

/**
 * Wraps combination of <code>packageName</code> and <code>name</code> to the object representation.
 */
public final class ReferencedTypeImpl extends AbstractBaseType {
    /**
     * Creates instance of this class with concrete package name and type name.
     *
     * @param identifier JavaTypeName of the referenced type
     */
    public ReferencedTypeImpl(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ReferencedTypeImpl [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append("]");
        return builder.toString();
    }
}
