/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.TypeComments;
import org.opendaylight.mdsal.binding.yang.types.CodegenTypeProvider;
import org.opendaylight.yangtools.concepts.Builder;
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
        Optional<String> comment = buildComment(node);
        comment.ifPresent(genType::setComment);
    }

    @Override
    void addRpcMethodComment(TypeMemberBuilder<?> genType, RpcDefinition node) {
        final String rpcName = node.getQName().getLocalName();

        final String inputDesc = "input of {@code " + rpcName + "}";
        final String outputDesc = "output of {@code " + rpcName + "}";
        final StringBuilder bodyBuilder =  new StringBuilder("Invoke {@code ").append(rpcName).append("} RPC. ");

        final Optional<String> encodedDesc = buildComment(node);
        encodedDesc.ifPresent(bodyBuilder::append);

        final String comment = new MethodCommentBuilder()
                .addBody(bodyBuilder.toString())
                .addParam("input", inputDesc)
                .addReturn(outputDesc)
                .build();
        genType.setComment(comment);
    }

    private static Optional<String> buildComment(final DocumentedNode node) {
        final Optional<String> optDesc = node.getDescription();
        return optDesc.map(BindingGeneratorUtil::encodeAngleBrackets);
    }

    private static final class MethodCommentBuilder implements Builder<String> {
        private String body;
        private final LinkedHashMap<String, String> paramMap = new LinkedHashMap<>();
        private final LinkedHashMap<Class<? extends Exception>, String> throwsMap = new LinkedHashMap<>();
        private String returnValue;

        @Override
        public String build() {
            final StringBuilder comment = new StringBuilder();

            buildBody(comment);
            buildParam(comment);
            buildThrows(comment);
            buildReturn(comment);

            return comment.toString();
        }

        private void buildBody(final StringBuilder comment) {
            if (!Strings.isNullOrEmpty(body)) {
                comment.append(body).append(' ');
            }
        }

        private void buildParam(final StringBuilder comment) {
            for (Map.Entry<String, String> param : paramMap.entrySet()) {
                comment.append("@param ").append(param.getKey()).append(' ').append(param.getValue()).append(' ');
            }
        }

        private void buildThrows(final StringBuilder comment) {
            for (Map.Entry<Class<? extends Exception>, String> throwz : throwsMap.entrySet()) {
                final String exceptionName = throwz.getKey().getSimpleName();
                comment.append("@throws ").append(exceptionName).append(' ').append(throwz.getValue()).append(' ');
            }
        }

        private void buildReturn(final StringBuilder comment) {
            if (!Strings.isNullOrEmpty(returnValue)) {
                comment.append("@return ").append(returnValue);
            }
        }

        MethodCommentBuilder addParam(final String name, final String desc) {
            paramMap.put(name, desc);
            return this;
        }

        MethodCommentBuilder addThrows(final Class<? extends Exception> clazz, final String desc) {
            throwsMap.put(clazz, desc);
            return this;
        }

        MethodCommentBuilder addBody(final String newBody) {
            this.body = newBody;
            return this;
        }

        MethodCommentBuilder addReturn(final String newReturn) {
            this.returnValue = newReturn;
            return this;
        }
    }
}
