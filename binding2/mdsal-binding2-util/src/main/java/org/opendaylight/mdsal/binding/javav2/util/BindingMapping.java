/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.util;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.common.BindingMappingBase;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Standard Util class that provides generated Java related functionality
 */
@Beta
public final class BindingMapping extends BindingMappingBase {

    /**
     * Package prefix for Binding v2 generated Java code structures
     */
    public static final String PACKAGE_PREFIX = "org.opendaylight.mdsal.gen.javav2";

    private BindingMapping() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getRootPackageName(final Module module) {
        return getRawRootPackageName(module.getQNameModule(), module.getSemanticVersion(), PACKAGE_PREFIX);
    }
}
