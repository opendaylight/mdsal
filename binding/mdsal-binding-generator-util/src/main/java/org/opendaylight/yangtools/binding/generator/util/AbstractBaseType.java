/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.Type;

/**
 * It is used only as ancestor for other <code>Type</code>s
 *
 * @deprecated Use {@link org.opendaylight.mdsal.binding.generator.util.AbstractBaseType} instead.
 */
@Deprecated
public class AbstractBaseType implements Type {

    /**
     * Name of the package to which this <code>Type</code> belongs.
     */
    private final String packageName;

    /**
     * Name of this <code>Type</code>.
     */
    private final String name;

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
        if (packageName.isEmpty()) {
            return name;
        } else {
            return packageName + "." + name;
        }
    }

    /**
     * Constructs the instance of this class with the concrete package name type
     * name.
     *
     * @param pkName
     *            string with the package name to which this <code>Type</code>
     *            belongs
     * @param name
     *            string with the name for this <code>Type</code>
     */
    protected AbstractBaseType(final String pkName, final String name) {
        if (pkName == null) {
            throw new IllegalArgumentException("Package Name for Generated Type cannot be null!");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name of Generated Type cannot be null!");
        }
        this.packageName = pkName;
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(packageName);
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
        if (!(obj instanceof Type)) {
            return false;
        }
        Type other = (Type) obj;
        return Objects.equals(name, other.getName()) && Objects.equals(packageName, other.getPackageName());
    }

    @Override
    public String toString() {
        if (packageName.isEmpty()) {
            return "Type (" + name + ")";
        }
        return "Type (" + packageName + "." + name + ")";
    }
}
