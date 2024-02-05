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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@VisibleForTesting
public class BindingDOMRpcProviderServiceAdapter extends AbstractBindingAdapter<DOMRpcProviderService>
        implements RpcProviderService {
    private static final ImmutableSet<YangInstanceIdentifier> GLOBAL = ImmutableSet.of(YangInstanceIdentifier.of());

    public BindingDOMRpcProviderServiceAdapter(final AdapterContext adapterContext,
            final DOMRpcProviderService domRpcRegistry) {
        super(adapterContext, domRpcRegistry);
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
    public Registration registerRpcImplementations(final Collection<Rpc<?, ?>> implementations) {
        return register(currentSerializer(), implementations, GLOBAL);
    }

    @Override
    public Registration registerRpcImplementations(final Collection<Rpc<?, ?>> implementations,
            final Set<InstanceIdentifier<?>> paths) {
        final var serializer = currentSerializer();
        return register(serializer, implementations, toYangInstanceIdentifiers(serializer, paths));
    }

    @Override
    @Deprecated(since = "13.0.1")
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations) {
        return registerRpcImplementations(implementations.values());
    }

    @Override
    @Deprecated(since = "13.0.1")
    public Registration registerRpcImplementations(final ClassToInstanceMap<Rpc<?, ?>> implementations,
            final Set<InstanceIdentifier<?>> paths) {
        return registerRpcImplementations(implementations.values(), paths);
    }

    private <T extends Rpc<?, ?>> @NonNull Registration register(final CurrentAdapterSerializer serializer,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        return register(serializer, List.of(implementation), rpcContextPaths);
    }

    private @NonNull Registration register(final CurrentAdapterSerializer serializer,
            final Collection<Rpc<?, ?>> implementations,
            // Note: unique items are implied
            final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final var context = serializer.getRuntimeContext();

        return register(implementations, rpcContextPaths, impl -> {
            final var type = impl.implementedInterface();
            final var def = context.getRpcDefinition(type);
            if (def == null) {
                throw new IllegalArgumentException("Cannot resolve YANG definition of " + type);
            }
            final var rpcName = def.statement().argument();
            return new Impl(rpcName, new BindingDOMRpcImplementationAdapter(adapterContext(), rpcName, impl));
        });
    }

    private <T> @NonNull Registration register(final Collection<T> impls,
            final Collection<YangInstanceIdentifier> paths, final Function<T, Impl> implFactory) {
        final var builder = ImmutableMap.<DOMRpcIdentifier, DOMRpcImplementation>builderWithExpectedSize(impls.size());
        for (var impl : impls) {
            final var proxyImpl = implFactory.apply(impl);
            paths.forEach(path -> builder.put(DOMRpcIdentifier.create(proxyImpl.qname, path), proxyImpl.impl));
        }
        return getDelegate().registerRpcImplementations(builder.build());
    }

    private static Collection<YangInstanceIdentifier> toYangInstanceIdentifiers(
            final CurrentAdapterSerializer serializer, final Set<InstanceIdentifier<?>> identifiers) {
        final var ret = new ArrayList<YangInstanceIdentifier>(identifiers.size());
        for (var binding : identifiers) {
            ret.add(serializer.toCachedYangInstanceIdentifier(binding));
        }
        return ret;
    }

    private record Impl(@NonNull QName qname, @NonNull DOMRpcImplementation impl) {
        // Utility DTO for method return type
    }
}
