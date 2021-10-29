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
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
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
    private final ImmutableSortedMap<String, ModuleRuntimeType> modulesByPackage;
    private final ImmutableMap<QName, IdentityRuntimeType> identities;
    private final ImmutableMap<QName, OutputRuntimeType> rpcOutputs;
    private final ImmutableMap<QName, InputRuntimeType> rpcInputs;
    private final ImmutableMap<JavaTypeName, RuntimeType> types;

    public DefaultBindingRuntimeTypes(final EffectiveModelContext context,
            final Map<QNameModule, ModuleRuntimeType> modules, final Map<JavaTypeName, RuntimeType> types,
            final Map<QName, IdentityRuntimeType> identities, final Map<QName, InputRuntimeType> rpcInputs,
            final Map<QName, OutputRuntimeType> rpcOutputs) {
        this.context = requireNonNull(context);
        this.identities = ImmutableMap.copyOf(identities);
        this.types = ImmutableMap.copyOf(types);
        this.rpcInputs = ImmutableMap.copyOf(rpcInputs);
        this.rpcOutputs = ImmutableMap.copyOf(rpcOutputs);

        modulesByNamespace = ImmutableMap.copyOf(modules);
        modulesByPackage = ImmutableSortedMap.copyOf(Maps.uniqueIndex(modules.values(),
            module -> module.getIdentifier().packageName()));
    }

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
        // The type can actually specify a sub-package, hence we to perform an inexact lookup
        final var entry = modulesByPackage.floorEntry(typeName.packageName());
        return entry == null ? null : entry.getValue().bindingChild(typeName);
    }

    @Override
    public RuntimeType schemaTreeChild(final QName qname) {
        final var module = modulesByNamespace.get(qname.getModule());
        return module == null ? null : module.schemaTreeChild(qname);
    }

    @Override
    public Optional<InputRuntimeType> findRpcInput(final QName rpcName) {
        return Optional.ofNullable(rpcInputs.get(requireNonNull(rpcName)));
    }

    @Override
    public Optional<OutputRuntimeType> findRpcOutput(final QName rpcName) {
        return Optional.ofNullable(rpcOutputs.get(requireNonNull(rpcName)));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("identities", identities)
            .add("types", types)
            .toString();
    }
}
