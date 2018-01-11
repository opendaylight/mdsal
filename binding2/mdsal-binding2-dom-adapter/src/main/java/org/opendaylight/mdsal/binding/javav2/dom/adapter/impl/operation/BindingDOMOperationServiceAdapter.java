/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.RpcActionConsumerRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMService;

//FIXME implement after improve DOM part of MD-SAL for support of Yang 1.1
/**
 * Adapter for operation service.
 */
@Beta
public class BindingDOMOperationServiceAdapter implements RpcActionConsumerRegistry {

    public static final Factory<RpcActionConsumerRegistry> BUILDER_FACTORY = Builder::new;

    private final DOMOperationService domService;
    private final BindingToNormalizedNodeCodec codec;
    private final LoadingCache<Class<? extends Operation>, OperationServiceAdapter> proxies = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<Class<? extends Operation>, OperationServiceAdapter>() {

                @SuppressWarnings("unchecked")
                private OperationServiceAdapter createProxy(final Class<? extends Operation> key) {
                    Preconditions.checkArgument(BindingReflections.isBindingClass(key));
                    Preconditions.checkArgument(key.isInterface(),
                            "Supplied Operation service type must be interface.");
                    if (Rpc.class.isAssignableFrom(key)) {
                        return new RpcServiceAdapter((Class<? extends Rpc<?, ?>>) key, codec, domService);
                    }
                    // TODO implement after improve DOM part of MD-SAL for support of Yang 1.1
                    throw new UnsupportedOperationException();
                }

                @Nonnull
                @Override
                public OperationServiceAdapter load(@Nonnull final Class<? extends Operation> key) throws Exception {
                    return createProxy(key);
                }

            });

    public BindingDOMOperationServiceAdapter(final DOMOperationService domService,
        final BindingToNormalizedNodeCodec codec) {
        this.domService = Preconditions.checkNotNull(domService);
        this.codec = Preconditions.checkNotNull(codec);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Rpc<?, ?>> T getRpcService(final Class<T> rpc) {
        Preconditions.checkArgument(rpc != null, "Rpc needs to be specified.");
        return (T) proxies.getUnchecked(rpc).getProxy();
    }

    private static final class Builder extends BindingDOMAdapterBuilder<RpcActionConsumerRegistry> {

        @Override
        protected RpcActionConsumerRegistry createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMOperationService domRpc = delegates.getInstance(DOMOperationService.class);
            return new BindingDOMOperationServiceAdapter(domRpc, codec);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMOperationService.class);
        }
    }

    @Override
    public <T extends Action<? extends TreeNode, ?, ?>> T getActionService(final Class<T> serviceInterface) {
        // TODO implement after improve DOM part of MD-SAL for support of Yang 1.1
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends ListAction<? extends TreeNode, ?, ?>> T getListActionService(final Class<T> serviceInterface) {
        // TODO implement after improve DOM part of MD-SAL for support of Yang 1.1
        throw new UnsupportedOperationException();
    }
}
