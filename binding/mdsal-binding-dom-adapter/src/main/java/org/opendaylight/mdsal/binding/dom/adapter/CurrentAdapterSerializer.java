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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceNotificationSpec;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.ForwardingBindingDOMCodecServices;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
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
    @Deprecated
    private static final MethodType RPC_SERVICE_METHOD_SIGNATURE = MethodType.methodType(ListenableFuture.class,
        RpcService.class, RpcInput.class);

    private final LoadingCache<InstanceIdentifier<?>, YangInstanceIdentifier> cache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<InstanceIdentifier<?>, YangInstanceIdentifier>() {
                @Override
                public YangInstanceIdentifier load(final InstanceIdentifier<?> key) {
                    return toYangInstanceIdentifier(key);
                }
            });

    private final ConcurrentMap<JavaTypeName, ContextReferenceExtractor> extractors = new ConcurrentHashMap<>();
    @Deprecated
    private final ConcurrentMap<Class<? extends RpcService>, ImmutableMap<QName, MethodHandle>> rpcMethods =
        new ConcurrentHashMap<>();
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

    @Nullable ContextReferenceExtractor findExtractor(final @NonNull InputRuntimeType inputType) {
        final var inputName = inputType.getIdentifier();
        final var cached = extractors.get(inputName);
        if (cached != null) {
            return cached;
        }

        // Load the class
        final Class<?> inputClass;
        try {
            inputClass = getRuntimeContext().loadClass(inputName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class for " + inputType, e);
        }

        // Check if there is an extractor at all
        final var created = ContextReferenceExtractor.of(inputClass);
        if (created == null) {
            return null;
        }

        // Reconcile with cache
        final var raced = extractors.putIfAbsent(inputName, created);
        return raced != null ? raced : created;
    }

    @Deprecated
    @NonNull ImmutableMap<QName, MethodHandle> getRpcMethods(final @NonNull Class<? extends RpcService> serviceType) {
        return rpcMethods.computeIfAbsent(serviceType, ignored -> {
            final var lookup = MethodHandles.publicLookup();
            return ImmutableMap.copyOf(Maps.transformValues(createQNameToMethod(serviceType), method -> {
                final MethodHandle raw;
                try {
                    raw = lookup.unreflect(method);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Lookup on public method failed", e);
                }
                return raw.asType(RPC_SERVICE_METHOD_SIGNATURE);
            }));
        });
    }

    @Deprecated
    @VisibleForTesting
    // FIXME: This should be probably part of Binding Runtime context
    ImmutableMap<QName, Method> createQNameToMethod(final Class<? extends RpcService> key) {
        final var moduleName = BindingReflections.getQNameModule(key);
        final var runtimeContext = getRuntimeContext();
        final var module = runtimeContext.getEffectiveModelContext().findModule(moduleName).orElse(null);
        if (module == null) {
            LOG.trace("Schema for {} is not available; expected module name: {}; BindingRuntimeContext: {}",
                key, moduleName, runtimeContext);
            throw new IllegalStateException(String.format("Schema for %s is not available; expected module name: %s;"
                + " full BindingRuntimeContext available in trace log", key, moduleName));
        }

        final var ret = ImmutableBiMap.<QName, Method>builder();
        try {
            for (var rpcDef : module.getRpcs()) {
                final var rpcName = rpcDef.getQName();
                ret.put(rpcName, key.getMethod(Naming.getRpcMethodName(rpcName), runtimeContext.getRpcInput(rpcName)));
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
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
}
