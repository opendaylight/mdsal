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
import com.google.common.base.VerifyException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.BindingContract;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
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

    private final ConcurrentMap<JavaTypeName, ContextReferenceExtractor> extractors = new ConcurrentHashMap<>();
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
        return DOMDataTreeIdentifier.of(path.datastore(), toYangInstanceIdentifier(path.path()));
    }

    Collection<DOMDataTreeIdentifier> toDOMDataTreeIdentifiers(final Collection<DataTreeIdentifier<?>> subtrees) {
        return subtrees.stream().map(this::toDOMDataTreeIdentifier).collect(Collectors.toSet());
    }

    @NonNull Absolute getActionPath(final @NonNull ActionSpec<?, ?> spec) {
        return getSchemaNodeIdentifier(spec.path(), spec.type(), ActionRuntimeType.class,
            ActionEffectiveStatement.class);
    }

    @NonNull Absolute getNotificationPath(final @NonNull InstanceNotificationSpec<?, ?> spec) {
        return getSchemaNodeIdentifier(spec.path(), spec.type(), NotificationRuntimeType.class,
            NotificationEffectiveStatement.class);
    }

    private <T extends RuntimeType> @NonNull Absolute getSchemaNodeIdentifier(final @NonNull InstanceIdentifier<?> path,
            final @NonNull Class<? extends BindingContract<?>> type, final @NonNull Class<T> expectedRuntime,
            final @NonNull Class<? extends SchemaTreeEffectiveStatement<?>> expectedStatement) {
        final var typeName = JavaTypeName.create(type);
        final var runtimeType = getRuntimeContext().getTypes().findSchema(typeName)
            .orElseThrow(() -> new IllegalArgumentException(typeName + " is not known"));
        final T casted;
        try {
            casted = expectedRuntime.cast(runtimeType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(typeName + " resolved to unexpected " + runtimeType, e);
        }
        final var qname = expectedStatement.cast(casted.statement()).argument();

        final var entry = resolvePath(path);
        final var stack = entry.getKey();
        final var stmt = stack.enterSchemaTree(qname.bindTo(entry.getValue()));
        if (expectedStatement.isInstance(stmt)) {
            return stack.toSchemaNodeIdentifier();
        }
        throw new VerifyException(path + " child " + typeName + " resolved to unexpected statement" + stmt);
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

    private @NonNull Entry<SchemaInferenceStack, QNameModule> resolvePath(final @NonNull InstanceIdentifier<?> path) {
        final var stack = SchemaInferenceStack.of(getRuntimeContext().modelContext());
        final var it = toYangInstanceIdentifier(path).getPathArguments().iterator();
        verify(it.hasNext(), "Unexpected empty instance identifier for %s", path);

        QNameModule lastNamespace;
        do {
            final var arg = it.next();
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
