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
