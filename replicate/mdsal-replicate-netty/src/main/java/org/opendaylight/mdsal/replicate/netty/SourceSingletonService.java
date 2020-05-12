/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

final class SourceSingletonService implements ClusterSingletonService {
    private static final ServiceGroupIdentifier SGID =
            ServiceGroupIdentifier.create(SourceSingletonService.class.getName());

    SourceSingletonService(final DOMDataBroker dataBroker, final int listenPort) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SGID;
    }

    @Override
    public void instantiateServiceInstance() {
        // TODO Auto-generated method stub

    }

    @Override
    public ListenableFuture<?> closeServiceInstance() {
        // TODO Auto-generated method stub
        return null;
    }
}
