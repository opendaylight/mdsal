/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.api.RpcActionProviderService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.registration.BindingDOMOperationAdapterRegistration;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.KeyedInstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Operation service provider adapter.
 */
@Beta
public class BindingDOMOperationProviderServiceAdapter implements RpcActionProviderService {

    private static final Set<YangInstanceIdentifier> GLOBAL = ImmutableSet.of(YangInstanceIdentifier.builder().build());
    private final BindingToNormalizedNodeCodec codec;
    private final DOMOperationProviderService domOperationRegistry;

    public BindingDOMOperationProviderServiceAdapter(final DOMOperationProviderService domOperationRegistry,
            final BindingToNormalizedNodeCodec codec) {
        this.codec = codec;
        this.domOperationRegistry = domOperationRegistry;
    }

    @Override
    public <S extends Rpc<?, ?>, T extends S> ObjectRegistration<T> registerRpcImplementation(final Class<S> type,
            final T implementation) {
        return register(type, implementation, GLOBAL);
    }

    @Override
    public <S extends Rpc<?, ?>, T extends S> ObjectRegistration<T> registerRpcImplementation(final Class<S> type,
            final T implementation, final Set<InstanceIdentifier<?>> paths) {
        return register(type, implementation, toYangInstanceIdentifiers(paths));
    }

    private <S extends Operation, T extends S> ObjectRegistration<T> register(final Class<S> type,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final Map<SchemaPath, Method> operations = codec.getOprMethodToSchemaPath(type).inverse();

        final BindingDOMOperationImplementationAdapter adapter =
            new BindingDOMOperationImplementationAdapter(codec.getCodecRegistry(), type, operations, implementation);
        final Set<DOMRpcIdentifier> domRpcs = createDomRpcIdentifiers(operations.keySet(), rpcContextPaths);
        final DOMOperationImplementationRegistration<?> domReg =
            domOperationRegistry.registerOperationImplementation(adapter, domRpcs);
        return new BindingDOMOperationAdapterRegistration<>(implementation, domReg);
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

    @Override
    public <S extends Action<? extends TreeNode, ?, ?>, T extends S, P extends TreeNode> ObjectRegistration<T>
            registerActionImplementation(final Class<S> type, final InstanceIdentifier<P> parent,
                    final T implementation) {
        return register(type, implementation, toYangInstanceIdentifiers(ImmutableSet.of(parent)));
    }

    @Override
    public <S extends ListAction<? extends TreeNode, ?, ?>, T extends S, P extends TreeNode, K> ObjectRegistration<T>
            registerListActionImplementation(final Class<S> type, final KeyedInstanceIdentifier<P, K> parent,
                    final T implementation) {
        return register(type, implementation, toYangInstanceIdentifiers(ImmutableSet.of(parent)));
    }
}
