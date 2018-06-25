/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class BindingDOMRpcProviderServiceAdapter implements RpcProviderService {
    private static final Set<YangInstanceIdentifier> GLOBAL = ImmutableSet.of(YangInstanceIdentifier.builder().build());

    private final BindingToNormalizedNodeCodec codec;
    private final DOMRpcProviderService domRpcRegistry;

    public BindingDOMRpcProviderServiceAdapter(final DOMRpcProviderService domRpcRegistry,
            final BindingToNormalizedNodeCodec codec) {
        this.codec = codec;
        this.domRpcRegistry = domRpcRegistry;
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
        final Map<SchemaPath, Method> rpcs = codec.getRpcMethodToSchemaPath(type).inverse();

        final BindingDOMRpcImplementationAdapter adapter = new BindingDOMRpcImplementationAdapter(
                codec.getCodecRegistry(), type, rpcs, implementation);
        final Set<DOMRpcIdentifier> domRpcs = createDomRpcIdentifiers(rpcs.keySet(), rpcContextPaths);
        final DOMRpcImplementationRegistration<?> domReg = domRpcRegistry.registerRpcImplementation(adapter, domRpcs);
        return new BindingRpcAdapterRegistration<>(implementation, domReg);
    }

    private static Set<DOMRpcIdentifier> createDomRpcIdentifiers(final Set<SchemaPath> rpcs,
            final Collection<YangInstanceIdentifier> paths) {
        final Set<DOMRpcIdentifier> ret = new HashSet<>();
        for (final YangInstanceIdentifier path : paths) {
            for (final SchemaPath rpc : rpcs) {
                ret.add(DOMRpcIdentifier.create(rpc, path));
            }
        }
        return ret;
    }

    private Collection<YangInstanceIdentifier> toYangInstanceIdentifiers(final Set<InstanceIdentifier<?>> identifiers) {
        final Collection<YangInstanceIdentifier> ret = new ArrayList<>(identifiers.size());
        for (final InstanceIdentifier<?> binding : identifiers) {
            ret.add(codec.toYangInstanceIdentifierCached(binding));
        }
        return ret;
    }
}
