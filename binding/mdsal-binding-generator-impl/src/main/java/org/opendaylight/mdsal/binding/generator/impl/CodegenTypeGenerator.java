/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.model.util.TypeComments;
import org.opendaylight.mdsal.binding.yang.types.CodegenTypeProvider;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

final class CodegenTypeGenerator extends AbstractTypeGenerator {
    CodegenTypeGenerator(final SchemaContext context) {
        super(context, new CodegenTypeProvider(context));
    }

    List<Type> toTypes(final Set<Module> modules) {
        final List<Type> filteredGenTypes = new ArrayList<>();
        for (final Module m : modules) {
            filteredGenTypes.addAll(moduleContext(m.getQNameModule()).getGeneratedTypes());
            final Set<Type> additionalTypes = typeProvider().getAdditionalTypes().get(m);
            if (additionalTypes != null) {
                filteredGenTypes.addAll(additionalTypes);
            }
        }
        return filteredGenTypes;
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genType, final Module module,
            final SchemaNode node) {
        YangSourceDefinition.of(module, node).ifPresent(genType::setYangSourceDefinition);
        TypeComments.description(node).ifPresent(genType::addComment);
        node.getDescription().ifPresent(genType::setDescription);
        node.getReference().ifPresent(genType::setReference);
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genType, final Module module) {
        YangSourceDefinition.of(module).ifPresent(genType::setYangSourceDefinition);
        TypeComments.description(module).ifPresent(genType::addComment);
        module.getDescription().ifPresent(genType::setDescription);
        module.getReference().ifPresent(genType::setReference);
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilder interfaceBuilder, final Module module,
            final String description, final Collection<?  extends SchemaNode> nodes) {
        interfaceBuilder.addComment(TypeComments.javadoc("Interface for implementing the following YANG " + description
            + " defined in module <b>" + module.getName() + "</b>").get());
        YangSourceDefinition.of(module, nodes).ifPresent(interfaceBuilder::setYangSourceDefinition);
    }

    @Override
    void addComment(final TypeMemberBuilder<?> genType, final DocumentedNode node) {
        final Optional<String> optDesc = node.getDescription();
        if (optDesc.isPresent()) {
            genType.setComment(encodeAngleBrackets(optDesc.get()));
        }
    }
}
