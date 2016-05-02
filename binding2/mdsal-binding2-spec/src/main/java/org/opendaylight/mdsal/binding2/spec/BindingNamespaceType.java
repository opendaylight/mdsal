/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.spec;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

/**
 * Binding Namespace is structure of Java packages designed to prevent conflicts in YANG Java
 * Binding, since Java does have only one namespace.
 *
 *
 */
@Beta
public enum BindingNamespaceType {

    /**
     *
     * Namespace containing all derived types, defined from grouping and data namespaces
     *
     */
    Typedef("type"), Identity("ident"), Key("key"), Data("data"), Grouping("grp"), Builder("dto"),;

    private final String packagePrefix;

    private BindingNamespaceType(String packagePrefix) {
        this.packagePrefix = Preconditions.checkNotNull(packagePrefix);
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }
}
