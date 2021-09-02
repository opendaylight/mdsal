/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
@Beta
public final class BindingRuntimeTypes implements EffectiveModelContextProvider, Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeTypes.class);

    private final @NonNull EffectiveModelContext schemaContext;
    private final ImmutableMap<Type, AugmentationSchemaNode> typeToAugmentation;
    private final ImmutableMap<Type, WithStatus> typeToSchema;
    private final ImmutableMultimap<Type, Type> choiceToCases;
    // FIXME: given we have an EffectiveModelContext, we should be able to use SchemaTreeInference instead of Absolute
    private final ImmutableBiMap<Type, Absolute> actions;
    private final ImmutableMap<QName, Type> identities;
    // Not Immutable as we use two different implementations
    private final Map<WithStatus, Type> schemaToType;

    public BindingRuntimeTypes(final EffectiveModelContext schemaContext,
            final Map<Type, AugmentationSchemaNode> typeToAugmentation,
            final Map<Type, WithStatus> typeToSchema, final Map<WithStatus, Type> schemaToType,
            final BiMap<Absolute, Type> actions, final Map<QName, Type> identities) {
        this.schemaContext = requireNonNull(schemaContext);
        this.typeToAugmentation = ImmutableMap.copyOf(typeToAugmentation);
        this.typeToSchema = ImmutableMap.copyOf(typeToSchema);
        this.actions = ImmutableBiMap.copyOf(actions).inverse();
        this.identities = ImmutableMap.copyOf(identities);

        // Careful to use identity for SchemaNodes, but only if needed
        // FIXME: 8.0.0: YT should be switching to identity for equals(), so this should become unnecessary
        Map<WithStatus, Type> copy;
        try {
            copy = ImmutableMap.copyOf(schemaToType);
        } catch (IllegalArgumentException e) {
            LOG.debug("Equality-duplicates found in {}", schemaToType.keySet());
            copy = new IdentityHashMap<>(schemaToType);
        }

        this.schemaToType = copy;

        // Two-phase indexing of choice/case nodes. First we load all choices. Note we are using typeToSchema argument,
        // not field, so as not to instantiate its entrySet.
        final Set<GeneratedType> choiceTypes = typeToSchema.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof ChoiceEffectiveStatement)
            .map(entry -> {
                final Type key = entry.getKey();
                verify(key instanceof GeneratedType, "Unexpected choice type %s", key);
                return (GeneratedType) key;
            })
            .collect(Collectors.toUnmodifiableSet());

        final Multimap<Type, Type> builder = MultimapBuilder.hashKeys(choiceTypes.size()).arrayListValues().build();
        for (Entry<Type, WithStatus> entry : typeToSchema.entrySet()) {
            if (entry.getValue() instanceof CaseEffectiveStatement) {
                final Type type = entry.getKey();
                verify(type instanceof GeneratedType, "Unexpected case type %s", type);
                builder.put(verifyNotNull(implementedChoiceType(((GeneratedType) type).getImplements(), choiceTypes),
                    "Cannot determine choice type for %s", type), type);
            }
        }

        choiceToCases = ImmutableMultimap.copyOf(builder);
    }

    private static GeneratedType implementedChoiceType(final List<Type> impls, final Set<GeneratedType> choiceTypes) {
        for (Type impl : impls) {
            if (impl instanceof GeneratedType && choiceTypes.contains(impl)) {
                return (GeneratedType) impl;
            }
        }
        return null;
    }

    public BindingRuntimeTypes(final EffectiveModelContext schemaContext,
            final Map<Type, AugmentationSchemaNode> typeToAugmentation,
            final BiMap<Type, WithStatus> typeToDefiningSchema, final BiMap<Absolute, Type> actions,
            final Map<QName, Type> identities) {
        this(schemaContext, typeToAugmentation, typeToDefiningSchema, typeToDefiningSchema.inverse(), actions,
            identities);
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return schemaContext;
    }

    public Optional<AugmentationSchemaNode> findAugmentation(final Type type) {
        return Optional.ofNullable(typeToAugmentation.get(type));
    }

    public Optional<Type> findIdentity(final QName qname) {
        return Optional.ofNullable(identities.get(qname));
    }

    public Optional<WithStatus> findSchema(final Type type) {
        return Optional.ofNullable(typeToSchema.get(type));
    }

    public Optional<Absolute> findActionIdentifier(final Type action) {
        return Optional.ofNullable(actions.get(requireNonNull(action)));
    }

    public Optional<Type> findType(final WithStatus schema) {
        return Optional.ofNullable(schemaToType.get(schema));
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return choiceToCases;
    }

    public Collection<Type> findCases(final Type choiceType) {
        return choiceToCases.get(choiceType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("typeToAugmentation", typeToAugmentation)
                .add("typeToSchema", typeToSchema)
                .add("choiceToCases", choiceToCases)
                .add("identities", identities)
                .toString();
    }
}
