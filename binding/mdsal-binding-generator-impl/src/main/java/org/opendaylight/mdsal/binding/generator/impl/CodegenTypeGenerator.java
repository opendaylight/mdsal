/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.TypeComments;
import org.opendaylight.mdsal.binding.yang.types.CodegenTypeProvider;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

final class CodegenTypeGenerator extends AbstractTypeGenerator {
    CodegenTypeGenerator(final EffectiveModelContext context, final Map<SchemaNode, JavaTypeName> renames) {
        super(context, new CodegenTypeProvider(context, renames), renames);
    }

    List<Type> toTypes(final Collection<? extends Module> modules) {
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
        node.getDescription().map(TypeMemberComment::referenceOf).ifPresent(genType::setComment);
    }

    @Override
    void addRpcMethodComment(final TypeMemberBuilder<?> genType, final RpcDefinition node) {
        final String rpcName = node.getQName().getLocalName();
        genType.setComment(TypeMemberComment.builder()
            .contractDescription("Invoke {@code " + rpcName + "} RPC.")
            .referenceDescription(node.getDescription()
//              sb.append("\n<p><pre>\n")
                .map(BindingGeneratorUtil::encodeAngleBrackets)
//              .append("\n</pre>\n")
                )
//            sb.append('\n')
            .contractDescription("@param input of {@code " + rpcName + "}\n"
                + "@return output of {@code " + rpcName + '}')
            .build());
    }
}
