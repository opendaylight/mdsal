/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

/**
 * Marker interface for MD-SAL services which are available for users of MD-SAL.
 *
 * <p>BindingService is marker interface for infrastructure services provided by the MD-SAL. These services may be
 * session-specific, and wrapped by custom delegator patterns in order to introduce additional semantics / checks
 * to the system.
 *
 * <p>This interface is a natural fit for extension, so that services defined outside of this package may be transported
 * via {@link MountPoint} and may be otherwise interoperated with.
 */
public interface BindingService {
    // Nothing else
}
