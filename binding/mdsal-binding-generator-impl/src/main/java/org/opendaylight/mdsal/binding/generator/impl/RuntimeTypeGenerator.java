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
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.yang.types.RuntimeTypeProvider;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
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

        /*
         * Fun parts are here. ModuleContext maps have Builders in them, we want plan types. We may encounter each
         * builder multiple times, hence we keep a builder->instance cache.
         */
        final Map<Type, Type> builderToType = new IdentityHashMap<>();
        for (final ModuleContext ctx : moduleContexts()) {
            for (Entry<Type, AugmentationSchemaNode> e : ctx.getTypeToAugmentation().entrySet()) {
                augmentationToSchema.put(builtType(builderToType, e.getKey()), e.getValue());
            }
            for (Entry<Type, WithStatus> e : ctx.getTypeToSchema().entrySet()) {
                typeToDefiningSchema.put(builtType(builderToType, e.getKey()), e.getValue());
            }
            for (Entry<Type, Type> e : ctx.getChoiceToCases().entries()) {
                choiceToCases.put(builtType(builderToType, e.getKey()), builtType(builderToType, e.getValue()));
            }
            for (Entry<QName, GeneratedTypeBuilder> e : ctx.getIdentities().entrySet()) {
                identities.put(e.getKey(), builtType(builderToType, e.getValue()));
            }
        }

        return new BindingRuntimeTypes(augmentationToSchema, typeToDefiningSchema, choiceToCases, identities);
    }

    private static Type builtType(final Map<Type, Type> knownTypes, final Type type) {
        if (type instanceof Builder) {
            final Type existing = knownTypes.get(type);
            if (existing != null) {
                return existing;
            }

            final Type built = (Type) ((Builder<?>)type).build();
            knownTypes.put(type, built);
            return built;
        }
        return type;
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
            final Collection<? extends SchemaNode> nodes) {
        // No-op
    }

    @Override
    void addComment(final TypeMemberBuilder<?> genType, final DocumentedNode node) {
        // No-op
    }
}
