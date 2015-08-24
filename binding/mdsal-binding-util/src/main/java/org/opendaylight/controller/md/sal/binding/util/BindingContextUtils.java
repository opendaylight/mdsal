/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.md.sal.binding.util;

import static com.google.common.base.Preconditions.checkNotNull;

import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class BindingContextUtils {

    public static ConsumerContext createConsumerContext(final BindingAwareConsumer consumer,
            final ClassToInstanceMap<BindingAwareService> serviceProvider) {
        checkNotNull(consumer,"Consumer should not be null");
        checkNotNull(serviceProvider,"Service map should not be null");
        return new SingleConsumerContextImpl(serviceProvider);
    }

    public static ProviderContext createProviderContext(final BindingAwareProvider provider,
            final ClassToInstanceMap<BindingAwareService> serviceProvider) {
        checkNotNull(provider,"Provider should not be null");
        checkNotNull(serviceProvider,"Service map should not be null");
        return new SingleProviderContextImpl(serviceProvider);
    }

    public static ConsumerContext createConsumerContextAndInitialize(final BindingAwareConsumer consumer,
            final ClassToInstanceMap<BindingAwareService> serviceProvider) {
        final ConsumerContext context = createConsumerContext(consumer, serviceProvider);
        consumer.onSessionInitialized(context);
        return context;
    }

    public static ProviderContext createProviderContextAndInitialize(final BindingAwareProvider provider,
            final ClassToInstanceMap<BindingAwareService> serviceProvider) {
        final ProviderContext context = createProviderContext(provider, serviceProvider);
        provider.onSessionInitiated(context);
        return context;
    }

    public static <T extends BindingAwareService> T createContextProxyOrReturnService(final Class<T> service, final T instance) {
        // FIXME: Create Proxy
        return instance;
    }

    private static class SingleConsumerContextImpl implements ConsumerContext, AutoCloseable {

        private ClassToInstanceMap<BindingAwareService> alreadyRetrievedServices;
        private ClassToInstanceMap<BindingAwareService> serviceProvider;

        public SingleConsumerContextImpl(final ClassToInstanceMap<BindingAwareService> serviceProvider) {
            this.alreadyRetrievedServices = MutableClassToInstanceMap.create();
            this.serviceProvider = serviceProvider;
        }

        @Override
        public final <T extends RpcService> T getRpcService(final Class<T> module) {
            return getSALService(RpcConsumerRegistry.class).getRpcService(module);
        }

        @Override
        public final <T extends BindingAwareService> T getSALService(final Class<T> service) {
            checkNotNull(service,"Service class should not be null.");
            final T potential = alreadyRetrievedServices.getInstance(service);
            if(potential != null) {
                return potential;
            }
            return tryToRetrieveSalService(service);
        }

        private synchronized <T extends BindingAwareService> T tryToRetrieveSalService(final Class<T> service) {
            final T potential = alreadyRetrievedServices.getInstance(service);
            if(potential != null) {
                return potential;
            }
            final T requested = serviceProvider.getInstance(service);
            if(requested == null) {
                throw new IllegalArgumentException("Requested service "+service.getName() +" is not available.");
            }
            final T retrieved = BindingContextUtils.createContextProxyOrReturnService(service,requested);
            alreadyRetrievedServices.put(service, retrieved);
            return retrieved;
        }

        @Override
        public final void close() throws Exception {
            alreadyRetrievedServices = null;
            serviceProvider = null;
        }
    }

    private static class SingleProviderContextImpl extends SingleConsumerContextImpl implements ProviderContext {

        public SingleProviderContextImpl(final ClassToInstanceMap<BindingAwareService> serviceProvider) {
            super(serviceProvider);
        }

        @Override
        public <T extends RpcService> RoutedRpcRegistration<T> addRoutedRpcImplementation(final Class<T> type,
                final T implementation) throws IllegalStateException {
            return getSALService(RpcProviderRegistry.class).addRoutedRpcImplementation(type, implementation);
        }

        @Override
        public <T extends RpcService> RpcRegistration<T> addRpcImplementation(final Class<T> type, final T implementation)
                throws IllegalStateException {
            return getSALService(RpcProviderRegistry.class).addRpcImplementation(type, implementation);
        }
    }
}
