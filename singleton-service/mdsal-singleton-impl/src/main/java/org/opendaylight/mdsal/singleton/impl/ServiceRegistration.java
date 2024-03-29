/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.impl;

import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

abstract class ServiceRegistration extends AbstractObjectRegistration<ClusterSingletonService> {
    ServiceRegistration(final ClusterSingletonService instance) {
        super(instance);
    }
}
