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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
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
    public final <T extends Augmentation<?>> AugmentationSchemaNode getAugmentationDefinition(final Class<T> augClass) {
        return getTypes().findAugmentation(Type.of(augClass)).orElse(null);
    }

    @Override
    public final DataSchemaNode getSchemaDefinition(final Class<?> cls) {
        checkArgument(!Augmentation.class.isAssignableFrom(cls), "Supplied class must not be an augmentation (%s is)",
            cls);
        checkArgument(!Action.class.isAssignableFrom(cls), "Supplied class must not be an action (%s is)", cls);
        return (DataSchemaNode) getTypes().findSchema(Type.of(cls)).orElse(null);
    }

    @Override
    public final ActionRuntimeType getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return (ActionRuntimeType) getTypes().findSchema(Type.of(cls)).orElse(null);
    }

    @Override
    public final Entry<AugmentationIdentifier, AugmentationSchemaNode> getResolvedAugmentationSchema(
            final DataNodeContainer target, final Class<? extends Augmentation<?>> aug) {
        final AugmentationSchemaNode origSchema = getAugmentationDefinition(aug);
        checkArgument(origSchema != null, "Augmentation %s is not known in current schema context", aug);
        /*
         * FIXME: Validate augmentation schema lookup
         *
         * Currently this algorithm, does not verify if instantiated child nodes
         * are real one derived from augmentation schema. The problem with
         * full validation is, if user used copy builders, he may use
         * augmentation which was generated for different place.
         *
         * If this augmentations have same definition, we emit same identifier
         * with data and it is up to underlying user to validate data.
         *
         */
        final Set<QName> childNames = new HashSet<>();
        final Set<DataSchemaNode> realChilds = new HashSet<>();
        for (final DataSchemaNode child : origSchema.getChildNodes()) {
            final DataSchemaNode dataChildQNname = target.dataChildByName(child.getQName());
            final String childLocalName = child.getQName().getLocalName();
            if (dataChildQNname == null) {
                for (DataSchemaNode dataSchemaNode : target.getChildNodes()) {
                    if (childLocalName.equals(dataSchemaNode.getQName().getLocalName())) {
                        realChilds.add(dataSchemaNode);
                        childNames.add(dataSchemaNode.getQName());
                    }
                }
            } else {
                realChilds.add(dataChildQNname);
                childNames.add(child.getQName());
            }
        }

        final AugmentationIdentifier identifier = AugmentationIdentifier.create(childNames);
        final AugmentationSchemaNode proxy = new EffectiveAugmentationSchema(origSchema, realChilds);
        return new SimpleEntry<>(identifier, proxy);
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
    public final ImmutableMap<AugmentationIdentifier, Type> getAvailableAugmentationTypes(
            final DataNodeContainer container) {
        if (container instanceof AugmentationTarget) {
            final var augmentations = ((AugmentationTarget) container).getAvailableAugmentations();
            if (!augmentations.isEmpty()) {
                final var identifierToType = new HashMap<AugmentationIdentifier, Type>();
                final var types = getTypes();
                for (var augment : augmentations) {
                    types.findOriginalAugmentationType(augment).ifPresent(augType -> {
                        identifierToType.put(getAugmentationIdentifier(augment), augType);
                    });
                }
                return ImmutableMap.copyOf(identifierToType);
            }
        }
        return ImmutableMap.of();
    }

    @Override
    public final Class<?> getIdentityClass(final QName input) {
        return identityClasses.getUnchecked(input);
    }

    private static AugmentationIdentifier getAugmentationIdentifier(final AugmentationSchemaNode augment) {
        // FIXME: use DataSchemaContextNode.augmentationIdentifierFrom() once it does caching
        return AugmentationIdentifier.create(augment.getChildNodes().stream().map(DataSchemaNode::getQName)
            .collect(ImmutableSet.toImmutableSet()));
    }
}
