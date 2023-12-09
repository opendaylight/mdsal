/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.opendaylight.mdsal.singleton.dom.impl.EOSClusterSingletonServiceProvider.CLOSE_SERVICE_ENTITY_TYPE;
import static org.opendaylight.mdsal.singleton.dom.impl.EOSClusterSingletonServiceProvider.SERVICE_ENTITY_TYPE;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

abstract class AbstractTest {
    static final @NonNull String SERVICE_NAME = "testServiceName";
    static final @NonNull ServiceGroupIdentifier SERVICE_ID = ServiceGroupIdentifier.create(SERVICE_NAME);
    static final @NonNull DOMEntity MAIN_ENTITY = new DOMEntity(SERVICE_ENTITY_TYPE, SERVICE_NAME);
    static final @NonNull DOMEntity CLOSE_ENTITY = new DOMEntity(CLOSE_SERVICE_ENTITY_TYPE, SERVICE_NAME);
}
