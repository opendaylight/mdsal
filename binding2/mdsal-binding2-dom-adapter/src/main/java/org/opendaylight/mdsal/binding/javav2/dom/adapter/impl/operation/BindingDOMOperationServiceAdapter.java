/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import static java.util.Objects.requireNonNull;

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
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Adapter for operation service.
 */
@Beta
public class BindingDOMOperationServiceAdapter implements RpcActionConsumerRegistry {

    public static final Factory<RpcActionConsumerRegistry> BUILDER_FACTORY = Builder::new;

    private final DOMRpcService domRpcService;
    private final DOMActionService domActionService;
    private final BindingToNormalizedNodeCodec codec;
    private final LoadingCache<Class<? extends Rpc<?, ?>>, RpcServiceAdapter> rpcProxies = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<Class<? extends Rpc<?, ?>>, RpcServiceAdapter>() {
                @Nonnull
                @Override
                public RpcServiceAdapter load(@Nonnull final Class<? extends Rpc<?, ?>> key) {
                    Preconditions.checkArgument(BindingReflections.isBindingClass(key));
                    Preconditions.checkArgument(key.isInterface(),
                            "Supplied Operation service type must be interface.");
                    if (Rpc.class.isAssignableFrom(key)) {
                        return new RpcServiceAdapter(key, codec, domRpcService);
                    }

                    throw new UnsupportedOperationException();
                }
            });

    private final LoadingCache<Class<? extends Action<? extends TreeNode, ?, ?, ?>>, ActionServiceAdapter>
            actionProxies = CacheBuilder.newBuilder().weakKeys().build(
                new CacheLoader<Class<? extends Action<? extends TreeNode, ?, ?, ?>>, ActionServiceAdapter>() {
                        @Nonnull
                        @Override
                        public ActionServiceAdapter load(@Nonnull
                                final Class<? extends Action<? extends TreeNode, ?, ?, ?>> key) {
                            Preconditions.checkArgument(BindingReflections.isBindingClass(key));
                            Preconditions.checkArgument(key.isInterface(),
                                "Supplied Operation service type must be interface.");
                            if (Action.class.isAssignableFrom(key)) {
                                return new ActionServiceAdapter(key, codec, domActionService);
                            }

                            throw new UnsupportedOperationException();
                        }
                    });

    public BindingDOMOperationServiceAdapter(final DOMRpcService domRpcService, final DOMActionService domActionService,
             final BindingToNormalizedNodeCodec codec) {
        this.domRpcService = requireNonNull(domRpcService);
        this.domActionService = requireNonNull(domActionService);
        this.codec = requireNonNull(codec);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Rpc<?, ?>> T getRpcService(final Class<T> rpc) {
        return (T) rpcProxies.getUnchecked(requireNonNull(rpc)).getProxy();
    }

    private static final class Builder extends BindingDOMAdapterBuilder<RpcActionConsumerRegistry> {

        @Override
        protected RpcActionConsumerRegistry createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMRpcService domRpcService = delegates.getInstance(DOMRpcService.class);
            final DOMActionService domActionService = delegates.getInstance(DOMActionService.class);
            return new BindingDOMOperationServiceAdapter(domRpcService, domActionService, codec);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMRpcService.class, DOMActionService.class);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Action<? extends TreeNode, ?, ?, ?>> T getActionService(final Class<T> serviceInterface) {
        return (T) actionProxies.getUnchecked(requireNonNull(serviceInterface)).getProxy();
    }

    @Override
    public <T extends ListAction<? extends TreeNode, ?, ?, ?>> T getListActionService(final Class<T> serviceInterface) {
        return getActionService(serviceInterface);
    }
}
