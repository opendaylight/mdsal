/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util;

import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

/**
 *
 * Wraps combination of <code>packageName</code> and <code>name</code> to the
 * object representation
 *
 */
public final class ReferencedTypeImpl extends AbstractBaseType {

    /**
     * Creates instance of this class with concrete package name and type name
     *
     * @param packageName
     *            string with the package name
     * @param name
     *            string with the name for referenced type
     */
    public ReferencedTypeImpl(String packageName, String name, ModuleContext context) {
        super(packageName, name, context);
    }

    /**
     * Creates instance of this class with concrete package name and type name
     * for already normalized identifier
     *
     * @param packageName
     *            string with the package name
     * @param name
     *            string with the name for referenced type
     * @param isNormalized
     *            indicates if identifier name is normalized
     */
    public ReferencedTypeImpl(String packageName, String name, boolean isNormalized, ModuleContext context) {
        super(packageName, name, isNormalized, context);
    }

    public ReferencedTypeImpl(String packageName, String name, boolean isPkgNormalized,
            boolean isTypeNormalized, ModuleContext context) {
        super(packageName, name, isPkgNormalized, isTypeNormalized, context);
    }

    //FIXME: It's better not to pass 'ModuleContext' just aiming to use a map to resolve confilctions
    //of types that should be fixed by MDSAL-271, here just eliminates the argument to make the change
    // be workable first, once this resolution is accept, we could reimplement it.
    public ReferencedTypeImpl(String packageName, String name, boolean isPkgNormalized,
            boolean isTypeNormalized) {
        super(packageName, name, isPkgNormalized, isTypeNormalized);
    }
    /**
     * Creates instance of this class with Generated type
     *
     * @param type
     *            Generated type
     */
    public ReferencedTypeImpl(final Type type) {
        super(type.getPackageName(), type.getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReferencedTypeImpl [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append(']');
        return builder.toString();
    }
}
