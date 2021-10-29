/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 */
@Beta
public abstract class AbstractBindingRuntimeContext implements BindingRuntimeContext {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBindingRuntimeContext.class);

    private final LoadingCache<QName, Class<?>> identityClasses = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<QName, Class<?>>() {
            @Override
            public Class<?> load(final QName key) {
                final Optional<Type> identityType = getTypes().findIdentity(key);
                checkArgument(identityType.isPresent(), "Supplied QName %s is not a valid identity", key);
                try {
                    return loadClass(identityType.get());
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + identityType + "was not found.", e);
                }
            }
        });

    @Override
    public final <T extends Augmentation<?>> AugmentRuntimeType getAugmentationDefinition(final Class<T> augClass) {
        return getTypes().findAugmentation(Type.of(augClass)).orElse(null);
    }

    @Override
    public final CompositeRuntimeType getSchemaDefinition(final Class<?> cls) {
        checkArgument(!Augmentation.class.isAssignableFrom(cls), "Supplied class must not be an augmentation (%s is)",
            cls);
        checkArgument(!Action.class.isAssignableFrom(cls), "Supplied class must not be an action (%s is)", cls);
        return (CompositeRuntimeType) getTypes().findSchema(Type.of(cls)).orElse(null);
    }

    @Override
    public final ActionRuntimeType getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return (ActionRuntimeType) getTypes().findSchema(Type.of(cls)).orElse(null);
    }

    @Override
    public final Entry<GeneratedType, WithStatus> getTypeWithSchema(final Class<?> type) {
        return getTypeWithSchema(getTypes(), Type.of(type));
    }

    private static @NonNull Entry<GeneratedType, WithStatus> getTypeWithSchema(final BindingRuntimeTypes types,
            final Type referencedType) {
        final WithStatus schema = types.findSchema(referencedType).orElseThrow(
            () -> new NullPointerException("Failed to find schema for type " + referencedType));
        final Type definedType = types.findType(schema).orElseThrow(
            () -> new NullPointerException("Failed to find defined type for " + referencedType + " schema " + schema));

        if (definedType instanceof GeneratedTypeBuilder) {
            return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).build(), schema);
        }
        checkArgument(definedType instanceof GeneratedType, "Type %s is not a GeneratedType", referencedType);
        return new SimpleEntry<>((GeneratedType) definedType, schema);
    }

    @Override
    public final Set<Class<?>> getCases(final Class<?> choice) {
        final Collection<Type> cazes = getTypes().findCases(Type.of(choice));
        final Set<Class<?>> ret = new HashSet<>(cazes.size());
        for (final Type caze : cazes) {
            try {
                ret.add(loadClass(caze));
            } catch (final ClassNotFoundException e) {
                LOG.warn("Failed to load class for case {}, ignoring it", caze, e);
            }
        }
        return ret;
    }

    @Override
    public final Class<?> getClassForSchema(final Absolute schema) {
        final var child = getTypes().schemaTreeChild(schema);
        checkArgument(child != null, "Failed to find binding type for %s", schema);

        try {
            return loadClass(child.javaType());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final Class<?> getIdentityClass(final QName input) {
        return identityClasses.getUnchecked(input);
    }
}
