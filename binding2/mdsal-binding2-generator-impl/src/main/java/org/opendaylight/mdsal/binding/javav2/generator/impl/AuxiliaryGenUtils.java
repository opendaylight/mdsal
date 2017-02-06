/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.BOOLEAN;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNode;
import org.opendaylight.mdsal.binding.javav2.generator.impl.util.YangTextTemplate;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * Auxiliary util class for {@link GenHelperUtil} class
 */
@Beta
final class AuxiliaryGenUtils {

    private static final Splitter BSDOT_SPLITTER = Splitter.on("\\.");
    private static final char NEW_LINE = '\n';
    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    private AuxiliaryGenUtils() {
        throw new UnsupportedOperationException("Util class");
    }

    static void annotateDeprecatedIfNecessary(final Status status, final GeneratedTypeBuilder builder) {
        if (status == Status.DEPRECATED) {
            builder.addAnnotation("", "Deprecated");
        }
    }

    private static boolean hasBuilderClass(final SchemaNode schemaNode) {
        if (schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode ||
                schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition) {
            return true;
        }
        return false;
    }

    static Constant qNameConstant(final GeneratedTypeBuilderBase<?> toBuilder, final String constantName,
                                  final QName name) {
        return toBuilder.addConstant(Types.typeForClass(QName.class), constantName, name);
    }

    /**
     * Created a method signature builder as part of
     * <code>interfaceBuilder</code>.
     *
     * The method signature builder is created for the getter method of
     * <code>schemaNodeName</code>. Also <code>comment</code> and
     * <code>returnType</code> information are added to the builder.
     *
     * @param interfaceBuilder
     *            generated type builder for which the getter method should be
     *            created
     * @param schemaNodeName
     *            string with schema node name. The name will be the part of the
     *            getter method name.
     * @param comment
     *            string with comment for the getter method
     * @param returnType
     *            type which represents the return type of the getter method
     * @param status
     *            status from yang file, for deprecated annotation
     * @return method signature builder which represents the getter method of
     *         <code>interfaceBuilder</code>
     */
    static MethodSignatureBuilder constructGetter(final GeneratedTypeBuilder interfaceBuilder,
                                                  final String schemaNodeName, final String comment, final Type returnType, final Status status) {

        final MethodSignatureBuilder getMethod = interfaceBuilder
                .addMethod(getterMethodName(schemaNodeName, returnType));
        if (status == Status.DEPRECATED) {
            getMethod.addAnnotation("", "Deprecated");
        }
        getMethod.setComment(encodeAngleBrackets(comment));
        getMethod.setReturnType(returnType);
        return getMethod;
    }

    /**
     * Creates the name of the getter method name from <code>localName</code>.
     *
     * @param localName
     *            string with the name of the getter method
     * @param returnType
     *            return type
     * @return string with the name of the getter method for
     *         <code>methodName</code> in JAVA method format
     */
    private static String getterMethodName(final String localName, final Type returnType) {
        final StringBuilder method = new StringBuilder();
        if (BOOLEAN.equals(returnType)) {
            method.append("is");
        } else {
            method.append("get");
        }
        final String name = BindingMapping.toFirstUpper(BindingMapping.getPropertyName(localName));
        method.append(name);
        return method.toString();
    }

    static String createDescription(final SchemaNode schemaNode, final String fullyQualifiedName,
                                    final SchemaContext schemaContext, final boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();
        final String nodeDescription = encodeAngleBrackets(schemaNode.getDescription());
        final String formattedDescription = YangTextTemplate.formatToParagraph(nodeDescription, 0);

        if (!Strings.isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            final Module module = SchemaContextUtil.findParentModule(schemaContext, schemaNode);
            final StringBuilder linkToBuilderClass = new StringBuilder();
            final String[] namespace = Iterables.toArray(BSDOT_SPLITTER.split(fullyQualifiedName), String.class);
            final String className = namespace[namespace.length - 1];

            if (hasBuilderClass(schemaNode)) {
                linkToBuilderClass.append(className);
                linkToBuilderClass.append("Builder");
            }

            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(encodeAngleBrackets(yangTemplateForNode.render(schemaNode).body()));
            sb.append("</pre>");
            sb.append(NEW_LINE);
            sb.append("The schema path to identify an instance is");
            sb.append(NEW_LINE);
            sb.append("<i>");
            sb.append(YangTextTemplate.formatSchemaPath(module.getName(), schemaNode.getPath().getPathFromRoot()));
            sb.append("</i>");
            sb.append(NEW_LINE);

            if (hasBuilderClass(schemaNode)) {
                sb.append(NEW_LINE);
                sb.append("<p>To create instances of this class use " + "{@link " + linkToBuilderClass + "}.");
                sb.append(NEW_LINE);
                sb.append("@see ");
                sb.append(linkToBuilderClass);
                sb.append(NEW_LINE);
                if (schemaNode instanceof ListSchemaNode) {
                    final List<QName> keyDef = ((ListSchemaNode)schemaNode).getKeyDefinition();
                    if (keyDef != null && !keyDef.isEmpty()) {
                        sb.append("@see ");
                        sb.append(className);
                        sb.append("Key");
                    }
                    sb.append(NEW_LINE);
                }
            }
        }

        return replaceAllIllegalChars(sb);
    }

    static String createDescription(final Module module, final boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();
        final String moduleDescription = encodeAngleBrackets(module.getDescription());
        final String formattedDescription = YangTextTemplate.formatToParagraph(moduleDescription, 0);

        if (!Strings.isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(encodeAngleBrackets(yangTemplateForModule.render(module).body()));
            sb.append("</pre>");
        }

        return replaceAllIllegalChars(sb);
    }

    /**
     * Returns first unique name for the augment generated type builder. The
     * generated type builder name for augment consists from name of augmented
     * node and serial number of its augmentation.
     *
     * @param builders
     *            map of builders which were created in the package to which the
     *            augmentation belongs
     * @param genTypeName
     *            string with name of augmented node
     * @return string with unique name for augmentation builder
     */
    static String augGenTypeName(final Map<String, GeneratedTypeBuilder> builders, final String genTypeName) {
        int index = 1;
        if (builders != null) {
            while (builders.containsKey(genTypeName + index)) {
                index = index + 1;
            }
        }
        return genTypeName + index;
    }

    /**
     * @param unknownSchemaNodes unknows schema nodes
     * @return nodeParameter of UnknownSchemaNode
     */
    static String getAugmentIdentifier(final List<UnknownSchemaNode> unknownSchemaNodes) {
        for (final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            final QName nodeType = unknownSchemaNode.getNodeType();
            if (AUGMENT_IDENTIFIER_NAME.equals(nodeType.getLocalName())
                    && YANG_EXT_NAMESPACE.equals(nodeType.getNamespace().toString())) {
                return unknownSchemaNode.getNodeParameter();
            }
        }
        return null;
    }

    @VisibleForTesting
    public static String replaceAllIllegalChars(final StringBuilder stringBuilder){
        final String ret = UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
