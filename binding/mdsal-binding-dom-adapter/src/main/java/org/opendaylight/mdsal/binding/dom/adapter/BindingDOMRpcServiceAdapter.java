/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.RpcService;

@VisibleForTesting
public final class BindingDOMRpcServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMRpcService, Class<? extends RpcService>, RpcServiceAdapter>
        implements RpcConsumerRegistry {
    static final Factory<RpcConsumerRegistry> BUILDER_FACTORY = Builder::new;

    public BindingDOMRpcServiceAdapter(final AdapterContext adapterContext, final DOMRpcService domService) {
        super(adapterContext, domService);
    }

    @Override
    public <T extends RpcService> T getRpcService(final Class<T> rpcService) {
        return rpcService.cast(getAdapter(requireNonNull(rpcService)).facade());
    }

    @Override
    RpcServiceAdapter loadAdapter(final Class<? extends RpcService> key) {
        checkArgument(BindingReflections.isBindingClass(key));
        checkArgument(key.isInterface(), "Supplied RPC service type must be interface.");
        return new RpcServiceAdapter(key, adapterContext(), getDelegate());
    }

    private static final class Builder extends BindingDOMAdapterBuilder<RpcConsumerRegistry> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMRpcService.class);
        }

        @Override
        protected RpcConsumerRegistry createInstance(final ClassToInstanceMap<DOMService> delegates) {
            return new BindingDOMRpcServiceAdapter(adapterContext(), delegates.getInstance(DOMRpcService.class));
        }
    }
}
