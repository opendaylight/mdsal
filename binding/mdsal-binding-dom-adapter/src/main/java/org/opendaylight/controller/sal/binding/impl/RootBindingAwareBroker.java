/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.sal.binding.impl;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableClassToInstanceMap;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.util.BindingContextUtils;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootBindingAwareBroker implements Identifiable<String>, BindingAwareBroker, AutoCloseable,
        RpcProviderRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(RootBindingAwareBroker.class);


    private final String identifier;


    private final ImmutableClassToInstanceMap<BindingAwareService> supportedConsumerServices;
    private final ImmutableClassToInstanceMap<BindingAwareService> supportedProviderServices;

    public RootBindingAwareBroker(final String instanceName,
            final ImmutableClassToInstanceMap<BindingAwareService> consumerServices,
            final ImmutableClassToInstanceMap<BindingAwareService> providerServices) {
        this.identifier = instanceName;
        supportedConsumerServices = consumerServices;
        supportedProviderServices = providerServices;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }


    public RpcProviderRegistry getRpcProviderRegistry() {
        return supportedProviderServices.getInstance(RpcProviderRegistry.class);
    }

    public RpcProviderRegistry getRpcBroker() {
        return getRpcBroker();
    }

    public MountPointService getMountService() {
        return supportedConsumerServices.getInstance(MountPointService.class);
    }

    @Override
    public ConsumerContext registerConsumer(final BindingAwareConsumer consumer) {
        checkState(supportedConsumerServices != null, "Broker is not initialized.");
        return BindingContextUtils.createConsumerContextAndInitialize(consumer, supportedConsumerServices);
    }

    @Override
    public ProviderContext registerProvider(final BindingAwareProvider provider) {
        checkState(supportedProviderServices != null, "Broker is not initialized.");
        return BindingContextUtils.createProviderContextAndInitialize(provider, supportedProviderServices);
    }

    @Override
    public void close() throws Exception {
        // FIXME: Close all sessions
    }

    @Override
    public <T extends RpcService> RoutedRpcRegistration<T> addRoutedRpcImplementation(final Class<T> type,
            final T implementation) throws IllegalStateException {
        return getRpcProviderRegistry().addRoutedRpcImplementation(type, implementation);
    }

    @Override
    public <T extends RpcService> RpcRegistration<T> addRpcImplementation(final Class<T> type, final T implementation)
            throws IllegalStateException {
        return getRpcProviderRegistry().addRpcImplementation(type, implementation);
    }

    @Override
    public <T extends RpcService> T getRpcService(final Class<T> module) {
        return getRpcBroker().getRpcService(module);
    }
}
