/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

@VisibleForTesting
public class BindingDOMRpcProviderServiceAdapter extends AbstractBindingAdapter<DOMRpcProviderService>
        implements RpcProviderService {
    private static final ImmutableSet<YangInstanceIdentifier> GLOBAL = ImmutableSet.of(YangInstanceIdentifier.empty());
    private LoadingCache<Class<? extends RpcService>, RpcServiceClassContext> rpcServiceClasses =
            CacheBuilder.newBuilder().build(
                    new CacheLoader<>() {
                        @Override
                        public RpcServiceClassContext load(final Class<? extends RpcService> key) {
                            final Map<QName, Method> rpcs = currentSerializer().getRpcMethodToQName(key)
                                    .inverse();
                            return new RpcServiceClassContext(adapterContext(), key, rpcs);
                        }
                    });

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

    private <S extends RpcService, T extends S> ObjectRegistration<T> register(final Class<S> type,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final Map<QName, Method> rpcs = currentSerializer().getRpcMethodToQName(type).inverse();
        final Map<QName, Set<DOMRpcIdentifier>> domRpcs = createDomRpcIdentifiers(rpcs.keySet(), rpcContextPaths);
        final RpcServiceClassContext context = rpcServiceClasses.getUnchecked(type);

        final Map<DOMRpcIdentifier, DOMRpcImplementation> implementations = new HashMap<>();
        for (Map.Entry<QName, Set<DOMRpcIdentifier>> entry : domRpcs.entrySet()) {
            final NewBindingDOMRpcImplementationAdapter domImpl =
                    new NewBindingDOMRpcImplementationAdapter(implementation,
                            context.getMethodContexts().get(SchemaNodeIdentifier.Absolute.of(entry.getKey())));

            for (DOMRpcIdentifier domRpcIdentifier : entry.getValue()) {
                implementations.put(domRpcIdentifier, domImpl);
            }
        }

        final Registration domReg = getDelegate().registerRpcImplementations(implementations);
        return new BindingRpcAdapterRegistration<>(implementation, domReg);
    }

    private static Map<QName,  Set<DOMRpcIdentifier>> createDomRpcIdentifiers(final Set<QName> rpcsPaths,
            final Collection<YangInstanceIdentifier> contextsPaths) {
        final Map<QName,  Set<DOMRpcIdentifier>> result = new HashMap<>();
        for (final QName rpcPath : rpcsPaths) {
            final Set<DOMRpcIdentifier> set = new HashSet<>();
            for (final YangInstanceIdentifier contextPath : contextsPaths) {
                set.add(DOMRpcIdentifier.create(rpcPath, contextPath));
            }
            result.put(rpcPath, set);
        }
        return result;
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
