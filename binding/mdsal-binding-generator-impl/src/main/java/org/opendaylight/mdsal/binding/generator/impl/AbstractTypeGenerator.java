/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.yang.types.AbstractTypeProvider;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

abstract class AbstractTypeGenerator {
    private final Map<QNameModule, ModuleContext> genCtx = new HashMap<>();

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private final AbstractTypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented element when creating augmentation builder.
     */
    private final @NonNull EffectiveModelContext schemaContext;

    AbstractTypeGenerator(final EffectiveModelContext context, final AbstractTypeProvider typeProvider,
            final Map<SchemaNode, JavaTypeName> renames) {
        this.schemaContext = requireNonNull(context);
        this.typeProvider = requireNonNull(typeProvider);
    }

    final @NonNull EffectiveModelContext schemaContext() {
        return schemaContext;
    }

    final Collection<ModuleContext> moduleContexts() {
        return genCtx.values();
    }

    final ModuleContext moduleContext(final QNameModule module) {
        return requireNonNull(genCtx.get(module), () -> "Module context not found for module " + module);
    }

    final AbstractTypeProvider typeProvider() {
        return typeProvider;
    }

    abstract void addCodegenInformation(GeneratedTypeBuilderBase<?> genType, Module module);

    abstract void addCodegenInformation(GeneratedTypeBuilderBase<?> genType, Module module, SchemaNode node);

    abstract void addCodegenInformation(GeneratedTypeBuilder interfaceBuilder, Module module, String description,
            Collection<? extends SchemaNode> nodes);

    abstract void addComment(TypeMemberBuilder<?> genType, DocumentedNode node);

    abstract void addRpcMethodComment(TypeMemberBuilder<?> genType, RpcDefinition node);
}
