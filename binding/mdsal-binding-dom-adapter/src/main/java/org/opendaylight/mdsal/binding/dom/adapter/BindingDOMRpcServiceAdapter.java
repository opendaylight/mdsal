/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;

public class BindingDOMRpcServiceAdapter extends AbstractBindingAdapter<@NonNull DOMRpcService>
        implements RpcConsumerRegistry {

    protected static final Factory<RpcConsumerRegistry> BUILDER_FACTORY = Builder::new;

    private final LoadingCache<Class<? extends RpcService>, RpcServiceAdapter> proxies = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<Class<? extends RpcService>, RpcServiceAdapter>() {
                @Override
                public RpcServiceAdapter load(final Class<? extends RpcService> key) throws Exception {
                    Preconditions.checkArgument(BindingReflections.isBindingClass(key));
                    Preconditions.checkArgument(key.isInterface(), "Supplied RPC service type must be interface.");
                    return new RpcServiceAdapter(key, getCodec(), getDelegate());
                }
            });

    public BindingDOMRpcServiceAdapter(final DOMRpcService domService, final BindingToNormalizedNodeCodec codec) {
        super(codec, domService);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RpcService> T getRpcService(final Class<T> rpcService) {
        Preconditions.checkArgument(rpcService != null, "Rpc Service needs to be specied.");
        return (T) proxies.getUnchecked(rpcService).getProxy();
    }

    private static final class Builder extends BindingDOMAdapterBuilder<RpcConsumerRegistry> {
        @Override
        protected RpcConsumerRegistry createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMRpcService domRpc  = delegates.getInstance(DOMRpcService.class);
            return new BindingDOMRpcServiceAdapter(domRpc  , codec);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMRpcService.class);
        }
    }
}
