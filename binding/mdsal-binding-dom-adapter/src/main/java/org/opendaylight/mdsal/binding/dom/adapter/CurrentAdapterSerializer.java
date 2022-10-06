/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verify;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceNotificationSpec;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.ForwardingBindingDOMCodecServices;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
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

    @NonNull Absolute getActionPath(final @NonNull ActionSpec<?, ?> spec) {
        final var entry = resolvePath(spec.path());
        final var stack = entry.getKey();
        final var stmt = stack.enterSchemaTree(BindingReflections.findQName(spec.type()).bindTo(entry.getValue()));
        verify(stmt instanceof ActionEffectiveStatement, "Action %s resolved to unexpected statement %s", spec, stmt);
        return stack.toSchemaNodeIdentifier();
    }

    @NonNull Absolute getNotificationPath(final @NonNull InstanceNotificationSpec<?, ?> spec) {
        final var entry = resolvePath(spec.path());
        final var stack = entry.getKey();
        final var stmt = stack.enterSchemaTree(BindingReflections.findQName(spec.type()).bindTo(entry.getValue()));
        verify(stmt instanceof NotificationEffectiveStatement, "Notification %s resolved to unexpected statement %s",
            spec, stmt);
        return stack.toSchemaNodeIdentifier();
    }

    private @NonNull Entry<SchemaInferenceStack, QNameModule> resolvePath(final @NonNull InstanceIdentifier<?> path) {
        final var stack = SchemaInferenceStack.of(getRuntimeContext().getEffectiveModelContext());
        final var it = toYangInstanceIdentifier(path).getPathArguments().iterator();
        verify(it.hasNext(), "Unexpected empty instance identifier for %s", path);

        QNameModule lastNamespace;
        do {
            final var arg = it.next();
            if (arg instanceof AugmentationIdentifier) {
                final var augChildren = ((AugmentationIdentifier) arg).getPossibleChildNames();
                verify(!augChildren.isEmpty(), "Invalid empty augmentation %s", arg);
                lastNamespace = augChildren.iterator().next().getModule();
                continue;
            }

            final var qname = arg.getNodeType();
            final var stmt = stack.enterDataTree(qname);
            lastNamespace = qname.getModule();
            if (stmt instanceof ListEffectiveStatement) {
                // Lists have two steps
                verify(it.hasNext(), "Unexpected list termination at %s in %s", stmt, path);
                // Verify just to make sure we are doing the right thing
                final var skipped = it.next();
                verify(skipped instanceof NodeIdentifier, "Unexpected skipped list entry item %s in %s", skipped, path);
                verify(stmt.argument().equals(skipped.getNodeType()), "Mismatched list entry item %s in %s", skipped,
                    path);
            }
        } while (it.hasNext());

        return Map.entry(stack, lastNamespace);
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
    ImmutableBiMap<Method, QName> getRpcMethodToQName(final Class<? extends RpcService> key) {
        final Module module = getModule(key);
        final ImmutableBiMap.Builder<Method, QName> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method,rpcDef.getQName());
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    @NonNull Entry<Method, RpcDefinition> getRpcInvokeDescription(final Class<? extends Rpc<?, ?>> type) {
        final var localContext = getRuntimeContext();
        final var runtimeType = localContext.getRpcDefinition(type);
        if (runtimeType == null) {
            throw new IllegalStateException("Failed to find runtime type for " + type);
        }



        // TODO Auto-generated method stub
        return null;
    }

    private Module getModule(final Class<?> modeledClass) {
        final QNameModule moduleName = BindingReflections.getQNameModule(modeledClass);
        final BindingRuntimeContext localRuntimeContext = getRuntimeContext();
        final Module module = localRuntimeContext.getEffectiveModelContext().findModule(moduleName).orElse(null);
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
        final var rpcName = rpcDef.getQName();
        final var inputClz = getRuntimeContext().getRpcInput(rpcName);
        return key.getMethod(BindingMapping.getRpcMethodName(rpcName), inputClz);
    }
}
