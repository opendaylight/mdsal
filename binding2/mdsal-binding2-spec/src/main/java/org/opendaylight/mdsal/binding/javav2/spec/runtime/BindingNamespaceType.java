/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * Binding Namespace is structure of Java packages designed to prevent conflicts in YANG Java
 * Binding, since Java does have only one namespace.
 */
@Beta
public enum BindingNamespaceType {

    /**
     *
     * Namespace containing all derived types, defined from grouping and data namespaces
     *
     */
    Typedef("type"), Identity("ident"), Key("key"), Data("data"), Notification("data"), Operation("data"),
    Grouping("grp"), Builder("dto");

    private final String packagePrefix;

    BindingNamespaceType(String packagePrefix) {
        this.packagePrefix = requireNonNull(packagePrefix);
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public static Boolean isData(final BindingNamespaceType type) {
        return isTreeData(type) || isNotificationData(type) || isOperationData(type);
    }

    public static Boolean isTreeData(final BindingNamespaceType type) {
        return Data.equals(type);
    }

    public static Boolean isNotificationData(final BindingNamespaceType type) {
        return Notification.equals(type);
    }

    public static Boolean isOperationData(final BindingNamespaceType type) {
        return Operation.equals(type);
    }

    public static Boolean isGrouping(final BindingNamespaceType type) {
        return Grouping.equals(type);
    }
}
