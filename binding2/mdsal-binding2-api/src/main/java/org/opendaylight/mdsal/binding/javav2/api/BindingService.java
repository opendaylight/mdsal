/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;

/**
 * Marker interface for MD-SAL services which are available for users of MD-SAL.
 *
 * <p>
 * BindingService is marker interface for infrastructure services provided by
 * the SAL. These services may be session-specific, and wrapped by custom
 * delegator patterns in order to introduce additional semantics / checks
 * to the system.
 *
 */
@Beta
public interface BindingService {
}
