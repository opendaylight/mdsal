/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.ServiceLoader;

/**
 *
 * Provider of YangModuleInfo for specified package / model.
 *
 * Implementation of this interface should be discoverable
 * via {@link ServiceLoader}
 *
 * @deprecated Use {@link org.opendaylight.mdsal.yang.binding.YangModelBindingProvider} instead
 */
@Deprecated
public interface YangModelBindingProvider {

    /**
     * YangModuleInfo associated to package
     *
     * @return
     */
    YangModuleInfo getModuleInfo();

}
