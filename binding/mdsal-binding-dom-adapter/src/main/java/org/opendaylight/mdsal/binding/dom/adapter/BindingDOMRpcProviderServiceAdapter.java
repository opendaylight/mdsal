/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@VisibleForTesting
public class BindingDOMRpcProviderServiceAdapter extends AbstractBindingAdapter<DOMRpcProviderService>
        implements RpcProviderService {
    private static final ImmutableSet<YangInstanceIdentifier> GLOBAL = ImmutableSet.of(YangInstanceIdentifier.empty());

    public BindingDOMRpcProviderServiceAdapter(final AdapterContext adapterContext,
            final DOMRpcProviderService domRpcRegistry) {
        super(adapterContext, domRpcRegistry);
    }

    @Override
    public <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(final Class<R> type,
            final I implementation) {
        return register(type, implementation, GLOBAL);
    }

    @Override
    public <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(final Class<R> type,
            final I implementation, final Set<InstanceIdentifier<?>> paths) {
        return register(type, implementation, toYangInstanceIdentifiers(paths));
    }

    private <S extends RpcService, T extends S> ObjectRegistration<T> register(final Class<S> type,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final Map<QName, Method> rpcs = currentSerializer().getRpcMethodToQName(type).inverse();

        final BindingDOMRpcImplementationAdapter adapter = new BindingDOMRpcImplementationAdapter(adapterContext(),
            type, rpcs, implementation);
        final Set<DOMRpcIdentifier> domRpcs = createDomRpcIdentifiers(rpcs.keySet(), rpcContextPaths);
        final DOMRpcImplementationRegistration<?> domReg = getDelegate().registerRpcImplementation(adapter, domRpcs);
        return new BindingRpcAdapterRegistration<>(implementation, domReg);
    }

    private static Set<DOMRpcIdentifier> createDomRpcIdentifiers(final Set<QName> rpcs,
            final Collection<YangInstanceIdentifier> paths) {
        final Set<DOMRpcIdentifier> ret = new HashSet<>();
        for (final YangInstanceIdentifier path : paths) {
            for (final QName rpc : rpcs) {
                ret.add(DOMRpcIdentifier.create(rpc, path));
            }
        }
        return ret;
    }

    private Collection<YangInstanceIdentifier> toYangInstanceIdentifiers(final Set<InstanceIdentifier<?>> identifiers) {
        final Collection<YangInstanceIdentifier> ret = new ArrayList<>(identifiers.size());
        final CurrentAdapterSerializer serializer = currentSerializer();
        for (final InstanceIdentifier<?> binding : identifiers) {
            ret.add(serializer.toCachedYangInstanceIdentifier(binding));
        }
        return ret;
    }
}
