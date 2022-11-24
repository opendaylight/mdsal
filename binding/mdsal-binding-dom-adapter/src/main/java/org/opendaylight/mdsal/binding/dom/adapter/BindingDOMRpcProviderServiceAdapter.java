/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
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
    @Deprecated
    public <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(final Class<R> type,
            final I implementation) {
        return register(currentSerializer(), type, implementation, GLOBAL);
    }

    @Override
    @Deprecated
    public <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(final Class<R> type,
            final I implementation, final Set<InstanceIdentifier<?>> paths) {
        final var serializer = currentSerializer();
        return register(serializer, type, implementation, toYangInstanceIdentifiers(serializer, paths));
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation) {
        return register(currentSerializer(), implementation, GLOBAL);
    }

    @Override
    public Registration registerRpcImplementation(final Rpc<?, ?> implementation,
            final Set<InstanceIdentifier<?>> paths) {
        final var serializer = currentSerializer();
        return register(serializer, implementation, toYangInstanceIdentifiers(serializer, paths));
    }

    @Override
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations) {
        return register(currentSerializer(), implementations, GLOBAL);
    }

    @Override
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations,
            final Set<InstanceIdentifier<?>> paths) {
        final var serializer = currentSerializer();
        return register(serializer, implementations, toYangInstanceIdentifiers(serializer, paths));
    }

    private <T extends Rpc<?, ?>> Registration register(final CurrentAdapterSerializer serializer,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        @SuppressWarnings("unchecked")
        final var type = (Class<T>) implementation.implementedInterface();
        return register(serializer, ImmutableClassToInstanceMap.of(type, implementation), rpcContextPaths);
    }

    private Registration register(final CurrentAdapterSerializer serializer,
            final ClassToInstanceMap<Rpc<?, ?>> implementations,
            final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final var context = serializer.getRuntimeContext();
        final var builder = ImmutableMap.<DOMRpcIdentifier, DOMRpcImplementation>builderWithExpectedSize(
            implementations.size());
        for (var entry : implementations.entrySet()) {
            final var type = entry.getKey();
            final var def = context.getRpcDefinition(type);
            if (def == null) {
                throw new IllegalArgumentException("Cannot resolve YANG definition of " + type);
            }
            final var rpcName  = def.statement().argument();
            final var impl = new BindingDOMRpcImplementationAdapter(adapterContext(), rpcName, entry.getValue());

            for (var id : createDomRpcIdentifiers(Set.of(rpcName), rpcContextPaths)) {
                builder.put(id, impl);
            }
        }

        return getDelegate().registerRpcImplementations(builder.build());
    }

    @Deprecated
    private <S extends RpcService, T extends S> ObjectRegistration<T> register(
            final CurrentAdapterSerializer serializer, final Class<S> type, final T implementation,
            final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final var methodHandles = currentSerializer().getRpcMethods(type);
        final var builder = ImmutableMap.<DOMRpcIdentifier, DOMRpcImplementation>builderWithExpectedSize(
            methodHandles.size());
        for (var entry : methodHandles.entrySet()) {
            final var rpcName = entry.getKey();
            final var impl = new LegacyDOMRpcImplementationAdapter(adapterContext(), rpcName,
                entry.getValue().bindTo(implementation));
            for (var id : createDomRpcIdentifiers(Set.of(rpcName), rpcContextPaths)) {
                builder.put(id, impl);
            }
        }

        return new BindingRpcAdapterRegistration<>(implementation,
            getDelegate().registerRpcImplementations(builder.build()));
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

    private static Collection<YangInstanceIdentifier> toYangInstanceIdentifiers(
            final CurrentAdapterSerializer serializer, final Set<InstanceIdentifier<?>> identifiers) {
        final var ret = new ArrayList<YangInstanceIdentifier>(identifiers.size());
        for (final InstanceIdentifier<?> binding : identifiers) {
            ret.add(serializer.toCachedYangInstanceIdentifier(binding));
        }
        return ret;
    }
}
