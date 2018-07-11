/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;

public class BindingDOMRpcServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMRpcService, Class<? extends RpcService>, RpcServiceAdapter>
        implements RpcConsumerRegistry {

    protected static final Factory<RpcConsumerRegistry> BUILDER_FACTORY = Builder::new;

    public BindingDOMRpcServiceAdapter(final DOMRpcService domService, final BindingToNormalizedNodeCodec codec) {
        super(codec, domService);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RpcService> T getRpcService(final Class<T> rpcService) {
        checkArgument(rpcService != null, "Rpc Service needs to be specied.");
        return (T) getAdapter(rpcService).getProxy();
    }

    @Override
    RpcServiceAdapter loadAdapter(final Class<? extends RpcService> key) {
        checkArgument(BindingReflections.isBindingClass(key));
        checkArgument(key.isInterface(), "Supplied RPC service type must be interface.");
        return new RpcServiceAdapter(key, getCodec(), getDelegate());
    }

    private static final class Builder extends BindingDOMAdapterBuilder<RpcConsumerRegistry> {
        @Override
        protected RpcConsumerRegistry createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMRpcService domRpc = delegates.getInstance(DOMRpcService.class);
            return new BindingDOMRpcServiceAdapter(domRpc  , codec);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMRpcService.class);
        }
    }
}
