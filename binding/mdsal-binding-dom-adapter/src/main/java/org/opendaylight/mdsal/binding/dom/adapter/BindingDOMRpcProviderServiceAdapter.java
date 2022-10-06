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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
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
    public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(final Class<S> type,
            final T implementation) {
        return register(type, implementation, GLOBAL);
    }

    @Override
    public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(final Class<S> type,
            final T implementation, final Set<InstanceIdentifier<?>> paths) {
        return register(type, implementation, toYangInstanceIdentifiers(paths));
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation) {
        return register(implementation, GLOBAL);
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation,
            final Set<InstanceIdentifier<?>> paths) {
        return register(implementation, toYangInstanceIdentifiers(paths));
    }

    private Registration register(final Rpc<?, ?> implementation,
            final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final var type = implementation.implementedInterface();
        final var def = currentSerializer().getRuntimeContext().getRpcDefinition(type);
        if (def == null) {
            throw new IllegalArgumentException("Cannot resolve YANG definition of " + type);
        }
        final var name = def.statement().argument();

        return getDelegate().registerRpcImplementation(
            new BindingDOMRpcImplementationAdapter(adapterContext(), implementation, name),
            createDomRpcIdentifiers(Set.of(name), rpcContextPaths));
    }

    private <S extends RpcService, T extends S> ObjectRegistration<T> register(final Class<S> type,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final var rpcs = currentSerializer().getRpcMethodToQName(type).inverse();

        return new BindingRpcAdapterRegistration<>(implementation, getDelegate().registerRpcImplementation(
            new LegacyDOMRpcImplementationAdapter(adapterContext(), type, rpcs, implementation),
            createDomRpcIdentifiers(rpcs.keySet(), rpcContextPaths)));
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
