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
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
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
            // Note: unique items are implied
            final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final var context = serializer.getRuntimeContext();

        return register(implementations, rpcContextPaths, (type, impl) -> {
            final var def = context.getRpcDefinition(type);
            if (def == null) {
                throw new IllegalArgumentException("Cannot resolve YANG definition of " + type);
            }
            final var rpcName = def.statement().argument();
            return new Impl(rpcName, new BindingDOMRpcImplementationAdapter(adapterContext(), rpcName, impl));
        });
    }

    private <K, V> @NonNull Registration register(final Map<K, V> map, final Collection<YangInstanceIdentifier> paths,
            final BiFunction<K, V, Impl> implFactory) {
        final var builder = ImmutableMap.<DOMRpcIdentifier, DOMRpcImplementation>builderWithExpectedSize(map.size());
        for (var entry : map.entrySet()) {
            final var impl = implFactory.apply(entry.getKey(), entry.getValue());
            paths.forEach(path -> builder.put(DOMRpcIdentifier.create(impl.qname, path), impl.impl));
        }
        return getDelegate().registerRpcImplementations(builder.build());
    }

    private static Collection<YangInstanceIdentifier> toYangInstanceIdentifiers(
            final CurrentAdapterSerializer serializer, final Set<InstanceIdentifier<?>> identifiers) {
        final var ret = new ArrayList<YangInstanceIdentifier>(identifiers.size());
        for (final InstanceIdentifier<?> binding : identifiers) {
            ret.add(serializer.toCachedYangInstanceIdentifier(binding));
        }
        return ret;
    }

    private record Impl(@NonNull QName qname, @NonNull DOMRpcImplementation impl) {
        // Utility DTO for method return type
    }
}
