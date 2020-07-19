/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableBiMap;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.ForwardingBindingDOMCodecServices;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@VisibleForTesting
public final class CurrentAdapterSerializer extends ForwardingBindingDOMCodecServices {
    private static final Logger LOG = LoggerFactory.getLogger(CurrentAdapterSerializer.class);

    private final LoadingCache<InstanceIdentifier<?>, YangInstanceIdentifier> cache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<InstanceIdentifier<?>, YangInstanceIdentifier>() {
                @Override
                public YangInstanceIdentifier load(final InstanceIdentifier<?> key) {
                    return toYangInstanceIdentifier(key);
                }
            });

    private final @NonNull BindingDOMCodecServices delegate;

    public CurrentAdapterSerializer(final BindingDOMCodecServices delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected BindingDOMCodecServices delegate() {
        return delegate;
    }

    @NonNull YangInstanceIdentifier toCachedYangInstanceIdentifier(final @NonNull InstanceIdentifier<?> path) {
        return cache.getUnchecked(path);
    }

    <T extends DataObject> @NonNull InstanceIdentifier<T> coerceInstanceIdentifier(final YangInstanceIdentifier dom) {
        return verifyNotNull(fromYangInstanceIdentifier(dom));
    }

    DOMDataTreeIdentifier toDOMDataTreeIdentifier(final DataTreeIdentifier<?> path) {
        return new DOMDataTreeIdentifier(path.getDatastoreType(), toYangInstanceIdentifier(path.getRootIdentifier()));
    }

    Collection<DOMDataTreeIdentifier> toDOMDataTreeIdentifiers(final Collection<DataTreeIdentifier<?>> subtrees) {
        return subtrees.stream().map(this::toDOMDataTreeIdentifier).collect(Collectors.toSet());
    }

    @NonNull SchemaPath getActionPath(final @NonNull Class<? extends Action<?, ?, ?>> type) {
        final ActionDefinition schema = getRuntimeContext().getActionDefinition(type);
        checkArgument(schema != null, "Failed to find schema for %s", type);
        return schema.getPath();
    }

    // FIXME: This should be probably part of Binding Runtime context
    ImmutableBiMap<Method, RpcDefinition> getRpcMethodToSchema(final Class<? extends RpcService> key) {
        final Module module = getModule(key);
        final ImmutableBiMap.Builder<Method, RpcDefinition> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef);
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    // FIXME: This should be probably part of Binding Runtime context
    ImmutableBiMap<Method, SchemaPath> getRpcMethodToSchemaPath(final Class<? extends RpcService> key) {
        final Module module = getModule(key);
        final ImmutableBiMap.Builder<Method, SchemaPath> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef.getPath());
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    private Module getModule(final Class<?> modeledClass) {
        final QNameModule moduleName = BindingReflections.getQNameModule(modeledClass);
        final BindingRuntimeContext localRuntimeContext = getRuntimeContext();
        final Module module = localRuntimeContext.getSchemaContext().findModule(moduleName).orElse(null);
        if (module != null) {
            return module;
        }

        LOG.trace("Schema for {} is not available; expected module name: {}; BindingRuntimeContext: {}",
                modeledClass, moduleName, localRuntimeContext);
        throw new IllegalStateException(String.format("Schema for %s is not available; expected module name: %s; "
                + "full BindingRuntimeContext available in trace log", modeledClass, moduleName));
    }

    private Method findRpcMethod(final Class<? extends RpcService> key, final RpcDefinition rpcDef)
            throws NoSuchMethodException {
        final String methodName = BindingMapping.getRpcMethodName(rpcDef.getQName());
        final Class<?> inputClz = getRuntimeContext().getClassForSchema(rpcDef.getInput());
        return key.getMethod(methodName, inputClz);
    }
}
