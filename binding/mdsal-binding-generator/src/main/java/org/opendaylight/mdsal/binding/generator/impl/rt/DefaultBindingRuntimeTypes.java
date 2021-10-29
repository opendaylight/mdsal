/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
public final class DefaultBindingRuntimeTypes implements BindingRuntimeTypes {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBindingRuntimeTypes.class);

    private final @NonNull EffectiveModelContext context;
    private final ImmutableMap<QNameModule, ModuleRuntimeType> modulesByNamespace;
    private final ImmutableMap<String, ModuleRuntimeType> modulesByPackage;
    private final ImmutableMap<QName, IdentityRuntimeType> identities;
    private final ImmutableMap<JavaTypeName, RuntimeType> types;


//  private final Map<JavaTypeName, RuntimeType> childByBinding = Map.of();

    public DefaultBindingRuntimeTypes(final EffectiveModelContext context,
            final Map<QNameModule, ModuleRuntimeType> modules, final Map<JavaTypeName, RuntimeType> types,
            final Map<QName, IdentityRuntimeType> identities) {
        this.context = requireNonNull(context);
        modulesByNamespace = ImmutableMap.copyOf(modules);
        this.identities = ImmutableMap.copyOf(identities);
        this.types = ImmutableMap.copyOf(types);

        modulesByPackage = Maps.uniqueIndex(modules.values(), module -> module.getIdentifier().packageName());
    }

//    DefaultBindingRuntimeTypes(final EffectiveModelContext schemaContext,
//            final Map<Type, AugmentRuntimeType> typeToAugmentation,
//            final Map<Type, WithStatus> typeToSchema, final Map<WithStatus, Type> schemaToType,
//            final Map<QName, Type> identities) {
//        this.context = requireNonNull(schemaContext);
//        this.typeToAugmentation = ImmutableMap.copyOf(typeToAugmentation);
//        this.typeToSchema = ImmutableMap.copyOf(typeToSchema);
//        this.identities = ImmutableMap.copyOf(identities);
//
//        // Careful to use identity for SchemaNodes, but only if needed
//        // FIXME: 8.0.0: YT should be switching to identity for equals(), so this should become unnecessary
//        Map<WithStatus, Type> copy;
//        try {
//            copy = ImmutableMap.copyOf(schemaToType);
//        } catch (IllegalArgumentException e) {
//            LOG.debug("Equality-duplicates found in {}", schemaToType.keySet());
//            copy = new IdentityHashMap<>(schemaToType);
//        }
//
//        this.schemaToType = copy;
//
//        // Two-phase indexing of choice/case nodes. First we load all choices. Note we are using typeToSchema
//        // argument, not field, so as not to instantiate its entrySet.
//        final Set<GeneratedType> choiceTypes = typeToSchema.entrySet().stream()
//            .filter(entry -> entry.getValue() instanceof ChoiceEffectiveStatement)
//            .map(entry -> {
//                final Type key = entry.getKey();
//                verify(key instanceof GeneratedType, "Unexpected choice type %s", key);
//                return (GeneratedType) key;
//            })
//            .collect(Collectors.toUnmodifiableSet());
//
//        final Multimap<Type, Type> builder = MultimapBuilder.hashKeys(choiceTypes.size()).arrayListValues().build();
//        for (Entry<Type, WithStatus> entry : typeToSchema.entrySet()) {
//            if (entry.getValue() instanceof CaseEffectiveStatement) {
//                final Type type = entry.getKey();
//                verify(type instanceof GeneratedType, "Unexpected case type %s", type);
//                builder.put(verifyNotNull(implementedChoiceType(((GeneratedType) type).getImplements(), choiceTypes),
//                    "Cannot determine choice type for %s", type), type);
//            }
//        }
//
//        choiceToCases = ImmutableMultimap.copyOf(builder);
//    }
//
//    private static GeneratedType implementedChoiceType(final List<Type> impls, final Set<GeneratedType> choiceTypes) {
//        for (Type impl : impls) {
//            if (impl instanceof GeneratedType && choiceTypes.contains(impl)) {
//                return (GeneratedType) impl;
//            }
//        }
//        return null;
//    }
//
//    DefaultBindingRuntimeTypes(final EffectiveModelContext schemaContext,
//            final Map<Type, AugmentRuntimeType> typeToAugmentation,
//            final BiMap<Type, WithStatus> typeToDefiningSchema, final Map<QName, Type> identities) {
//        this(schemaContext, typeToAugmentation, typeToDefiningSchema, typeToDefiningSchema.inverse(), identities);
//    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return context;
    }

    @Override
    public Optional<IdentityRuntimeType> findIdentity(final QName qname) {
        return Optional.ofNullable(identities.get(requireNonNull(qname)));
    }

    @Override
    public Optional<RuntimeType> findSchema(final JavaTypeName typeName) {
        return Optional.ofNullable(types.get(requireNonNull(typeName)));
    }

//    @Override
//    public Optional<Type> findOriginalAugmentationType(final AugmentationSchemaNode augment) {
//        // If the augment statement does not contain any child nodes, we did not generate an augmentation, as it would
//        // be plain littering.
//        // FIXME: MDSAL-695: this check is rather costly (involves filtering), can we just rely on the not being found
//        //                   in the end? all we are saving is essentially two map lookups after all...
//        if (augment.getChildNodes().isEmpty()) {
//            return Optional.empty();
//        }
//
//        // FIXME: MDSAL-695: We should have enough information from mdsal-binding-generator to receive a (sparse) Map
//        //                   for current -> original lookup. When combined with schemaToType, this amounts to the
//        //                   inverse view of what 'typeToSchema' holds
//        AugmentationSchemaNode current = augment;
//        while (true) {
//            // If this augmentation has been added through 'uses foo { augment bar { ... } }', we need to invert that
//            // walk and arrive at the original declaration site, as that is where we generated 'grouping foo's
//            // augmentation. That site may have a different module, hence the augment namespace may be different.
//            final Optional<AugmentationSchemaNode> original = current.getOriginalDefinition();
//            if (original.isEmpty()) {
//                return findType(current);
//            }
//            current = original.orElseThrow();
//        }
//    }

    @Override
    public GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        final var module = modulesByPackage.get(typeName.packageName());
        return module == null ? null : module.bindingChild(typeName);
    }

    @Override
    public RuntimeType schemaTreeChild(final QName qname) {
        final var module = modulesByNamespace.get(qname.getModule());
        return module == null ? null : module.schemaTreeChild(qname);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("identities", identities)
            .add("types", types)
            .toString();
    }
}
