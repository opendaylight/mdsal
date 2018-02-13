/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding.generator.spi.YangTextSnippetProvider;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

final class VerboseCommentGenerator {
    private final YangTextSnippetProvider provider;

    VerboseCommentGenerator(final YangTextSnippetProvider provider) {
        this.provider = requireNonNull(provider);
    }

    void appendModuleDescription(final StringBuilder sb, final Module module) {
        sb.append("<p>")
        .append("This class represents the following YANG schema fragment defined in module <b>")
        .append(module.getName())
        .append("</b>\n")
        .append("<pre>\n")
        .append(encodeAngleBrackets(provider.generateYangSnippet(module)))
        .append("</pre>");
    }

    void appendYangSnippet(final StringBuilder sb, final Set<? extends SchemaNode> schemaNodes) {
        if (!schemaNodes.isEmpty()) {
            sb.append("<pre>\n");
            for (SchemaNode schemaNode : schemaNodes) {
                sb.append(encodeAngleBrackets(provider.generateYangSnippet(requireNonNull(schemaNode))));
            }
            sb.append("</pre>\n");
        }
    }

    void appendYangSnippet(final StringBuilder sb, final Module module, final SchemaNode schemaNode,
            final String className) {

        sb.append("<p>\n")
        .append("This class represents the following YANG schema fragment defined in module <b>")
        .append(module.getName())
        .append("</b>\n")
        .append("<pre>\n")
        .append(encodeAngleBrackets(provider.generateYangSnippet(schemaNode)))
        .append("</pre>\n")
        .append("The schema path to identify an instance is\n")
        .append("<i>")
        .append(formatSchemaPath(module.getName(), schemaNode.getPath().getPathFromRoot()))
        .append("</i>\n");

        if (hasBuilderClass(schemaNode)) {
            final String builderClassName = className + "Builder";

            sb.append("\n<p>To create instances of this class use {@link ").append(builderClassName).append("}.\n")
            .append("@see ").append(builderClassName).append('\n');
            if (schemaNode instanceof ListSchemaNode) {
                final List<QName> keyDef = ((ListSchemaNode)schemaNode).getKeyDefinition();
                if (keyDef != null && !keyDef.isEmpty()) {
                    sb.append("@see ").append(className).append("Key");
                }
                sb.append('\n');
            }
        }
    }

    private static boolean hasBuilderClass(final SchemaNode schemaNode) {
        return schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode
                || schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition;
    }

    private static String formatSchemaPath(final String moduleName, final Iterable<QName> schemaPath) {
        final StringBuilder sb = new StringBuilder();
        sb.append(moduleName);

        QName currentElement = Iterables.getFirst(schemaPath, null);
        for (QName pathElement : schemaPath) {
            sb.append('/');
            if (!currentElement.getNamespace().equals(pathElement.getNamespace())) {
                currentElement = pathElement;
                sb.append(pathElement);
            } else {
                sb.append(pathElement.getLocalName());
            }
        }
        return sb.toString();
    }
}
