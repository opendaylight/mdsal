/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiRpcConsumerRegistry.FACTORY_NAME)
public final class OSGiRpcConsumerRegistry extends AbstractAdaptedService<RpcConsumerRegistry>
        implements RpcConsumerRegistry {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiRpcConsumerRegistry";

    public OSGiRpcConsumerRegistry() {
        super(RpcConsumerRegistry.class);
    }

    @Override
    public <T extends Rpc<?, ?>> T getRpc(final Class<T> rpcInterface) {
        return delegate().getRpc(rpcInterface);
    }

    @Override
    @Deprecated
    public <T extends RpcService> T getRpcService(final Class<T> serviceInterface) {
        return delegate().getRpcService(serviceInterface);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        start(properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }
}
