/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

final class SinkSingletonService implements ClusterSingletonService {
    private static final ServiceGroupIdentifier SGID =
            ServiceGroupIdentifier.create(SinkSingletonService.class.getName());

    private final DOMDataBroker dataBroker;
    private final InetSocketAddress sourceAddress;
    private final Duration reconnectDelay;

    SinkSingletonService(final DOMDataBroker dataBroker, final InetSocketAddress sourceAddress,
            final Duration reconnectDelay) {
        this.dataBroker = requireNonNull(dataBroker);
        this.sourceAddress = requireNonNull(sourceAddress);
        this.reconnectDelay = requireNonNull(reconnectDelay);
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
