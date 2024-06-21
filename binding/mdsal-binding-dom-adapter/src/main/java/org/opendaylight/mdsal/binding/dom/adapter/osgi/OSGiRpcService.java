/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.yangtools.binding.Rpc;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@NonNullByDefault
@Component(factory = OSGiRpcService.FACTORY_NAME)
public final class OSGiRpcService extends AbstractAdaptedService<RpcService> implements RpcService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiRpcConsumerRegistry";

    @Activate
    public OSGiRpcService(final Map<String, ?> properties) {
        super(RpcService.class, properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }

    @Override
    public <T extends Rpc<?, ?>> T getRpc(final Class<T> rpcInterface) {
        return delegate.getRpc(rpcInterface);
    }
}
