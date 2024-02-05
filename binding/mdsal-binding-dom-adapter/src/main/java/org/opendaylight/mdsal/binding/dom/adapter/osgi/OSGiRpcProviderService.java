/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.collect.ClassToInstanceMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(factory = OSGiRpcProviderService.FACTORY_NAME)
public final class OSGiRpcProviderService extends AbstractAdaptedService<RpcProviderService>
        implements RpcProviderService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiRpcProviderService";

    @Activate
    public OSGiRpcProviderService(final Map<String, ?> properties) {
        super(RpcProviderService.class, properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation) {
        return delegate.registerRpcImplementation(implementation);
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation,
            final Set<InstanceIdentifier<?>> paths) {
        return delegate.registerRpcImplementation(implementation, paths);
    }

    @Override
    public Registration registerRpcImplementations(final Collection<Rpc<?, ?>> implementations) {
        return delegate.registerRpcImplementations(implementations);
    }

    @Override
    public Registration registerRpcImplementations(final Collection<Rpc<?, ?>> implementations,
            final Set<InstanceIdentifier<?>> paths) {
        return delegate.registerRpcImplementations(implementations, paths);
    }

    @Override
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations) {
        return delegate.registerRpcImplementations(implementations);
    }

    @Override
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations,
            final Set<InstanceIdentifier<?>> paths) {
        return delegate.registerRpcImplementations(implementations, paths);
    }
}
