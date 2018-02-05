/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;

/**
 * Java interface for builders to get package name and so on.
 */
@Beta
public interface GeneratedTypeForBuilder {
    /**
     * Returns name of the package that builder class belongs to.
     *
     * @return name of the package that  builder class belongs to
     */
    String getPackageNameForBuilder();

    /**
     * Returns the binding namespace, in which generated type was specified.
     *
     * @return the binding namespace, in which generated type was specified.
     */
    BindingNamespaceType getNamespace();
}
