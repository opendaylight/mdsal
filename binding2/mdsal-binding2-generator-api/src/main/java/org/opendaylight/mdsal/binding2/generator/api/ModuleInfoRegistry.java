/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.spec.YangModuleInfo;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Provides mechanism for YangModuleInfo registration
 */
@Beta
public interface ModuleInfoRegistry {

    /**
     *
     * @param yangModuleInfo YANG module info instance
     * @return reference to registered YANG module info
     */
    ObjectRegistration<YangModuleInfo> registerModuleInfo(YangModuleInfo yangModuleInfo);
}
