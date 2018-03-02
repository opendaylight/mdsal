/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.yang.types.RuntimeTypeProvider;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

final class RuntimeTypeGenerator extends AbstractTypeGenerator {
    RuntimeTypeGenerator(final SchemaContext context) {
        super(context, new RuntimeTypeProvider(context));
    }

    BindingRuntimeTypes toTypeMapping() {
        final Map<Type, AugmentationSchemaNode> augmentationToSchema = new HashMap<>();
        final BiMap<Type, WithStatus> typeToDefiningSchema = HashBiMap.create();
        final Multimap<Type, Type> choiceToCases = HashMultimap.create();
        final Map<QName, Type> identities = new HashMap<>();

        for (final ModuleContext ctx : moduleContexts()) {
            augmentationToSchema.putAll(ctx.getTypeToAugmentation());
            typeToDefiningSchema.putAll(ctx.getTypeToSchema());

            choiceToCases.putAll(ctx.getChoiceToCases());
            identities.putAll(ctx.getIdentities());
        }

        return new BindingRuntimeTypes(augmentationToSchema, typeToDefiningSchema, choiceToCases, identities);
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genType, final Module module) {
        // No-op
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genType, final Module module, final SchemaNode node) {
        // No-op
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilder interfaceBuilder, final Module module, final String string,
            final Set<? extends SchemaNode> nodes) {
        // No-op
    }
}
