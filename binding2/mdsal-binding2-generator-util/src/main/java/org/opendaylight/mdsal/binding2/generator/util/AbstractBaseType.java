/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.model.api.Type;

/**
 * This class represents ancestor for other <code>Type</code>s
 */
@Beta
public class AbstractBaseType implements Type {

    /**
     * Name of the package to which this <code>Type</code> belongs.
     */
    private final String packageName;

    /**
     * Name of this <code>Type</code>.
     */
    private final String name;

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

    //TODO: implement hashCode, equals and toString
}
