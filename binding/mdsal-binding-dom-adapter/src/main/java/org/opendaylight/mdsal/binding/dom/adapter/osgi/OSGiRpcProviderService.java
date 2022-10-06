/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiRpcProviderService.FACTORY_NAME)
public final class OSGiRpcProviderService extends AbstractAdaptedService<RpcProviderService>
        implements RpcProviderService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiRpcProviderService";

    public OSGiRpcProviderService() {
        super(RpcProviderService.class);
    }

    @Override
    public <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(final Class<R> type,
            final I implementation) {
        return delegate().registerRpcImplementation(type, implementation);
    }

    @Override
    public <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(final Class<R> type,
            final I implementation, final Set<InstanceIdentifier<?>> paths) {
        return delegate().registerRpcImplementation(type, implementation, paths);
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation) {
        return delegate().registerRpcImplementation(implementation);
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation,
            final Set<InstanceIdentifier<?>> paths) {
        return delegate().registerRpcImplementation(implementation, paths);
    }

    @Override
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations) {
        return delegate().registerRpcImplementations(implementations);
    }

    @Override
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations,
            final Set<InstanceIdentifier<?>> paths) {
        return delegate().registerRpcImplementations(implementations, paths);
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
