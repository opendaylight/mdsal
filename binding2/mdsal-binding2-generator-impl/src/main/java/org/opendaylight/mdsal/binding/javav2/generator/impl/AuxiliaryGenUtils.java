/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.replacePackageTopNamespace;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.BOOLEAN;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNode;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNodes;
import org.opendaylight.mdsal.binding.javav2.generator.impl.util.YangTextTemplate;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.YangSnippetCleaner;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * Auxiliary util class for {@link GenHelperUtil} class
 */
@Beta
final class AuxiliaryGenUtils {

    private static final Splitter BSDOT_SPLITTER = Splitter.on(".");
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

    public static boolean hasBuilderClass(final SchemaNode schemaNode, final BindingNamespaceType namespaceType) {
        return (namespaceType.equals(BindingNamespaceType.Data)
                && (schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode
                || schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition
                || schemaNode instanceof ChoiceCaseNode));
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
        // underscore used as separator for distinction of method parts in convertIdentifier()
        method.append('_').append(localName);
        return JavaIdentifierNormalizer.normalizeSpecificIdentifier(method.toString(), JavaIdentifier.METHOD);
    }

    static String createDescription(final SchemaNode schemaNode, final String fullyQualifiedName,
                                    final SchemaContext schemaContext, final boolean verboseClassComments,
                                    final BindingNamespaceType namespaceType) {
        final StringBuilder sb = new StringBuilder();
        final String nodeDescription = encodeAngleBrackets(schemaNode.getDescription());
        final String formattedDescription = YangTextTemplate.formatToParagraph(nodeDescription, 0);

        if (!Strings.isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        final Module module = SchemaContextUtil.findParentModule(schemaContext, schemaNode);
        if (verboseClassComments) {
            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            String formedYang = YangSnippetCleaner.clean(yangTemplateForNode.render(schemaNode, module).body());
            sb.append(encodeAngleBrackets(formedYang));
            sb.append("</pre>");
            sb.append(NEW_LINE);
            sb.append("The schema path to identify an instance is");
            sb.append(NEW_LINE);
            sb.append("<i>");
            sb.append(YangTextTemplate.formatSchemaPath(module.getName(), schemaNode.getPath().getPathFromRoot()));
            sb.append("</i>");
            sb.append(NEW_LINE);

            if (hasBuilderClass(schemaNode, namespaceType) && !(schemaNode instanceof OperationDefinition)) {
                final StringBuilder linkToBuilderClass = new StringBuilder();
                final String basePackageName = BindingMapping.getRootPackageName(module);

                linkToBuilderClass
                        .append(replacePackageTopNamespace(basePackageName, fullyQualifiedName,
                                namespaceType, BindingNamespaceType.Builder))
                        .append("Builder");
                sb.append(NEW_LINE);
                sb.append("<p>To create instances of this class use " + "{@link " + linkToBuilderClass + "}.");
                sb.append(NEW_LINE);
                sb.append("@see ");
                sb.append(linkToBuilderClass);
                sb.append(NEW_LINE);
                if (schemaNode instanceof ListSchemaNode) {
                    final StringBuilder linkToKeyClass = new StringBuilder();

                    final String[] namespace = Iterables.toArray(BSDOT_SPLITTER.split(fullyQualifiedName), String.class);
                    final String className = namespace[namespace.length - 1];

                    linkToKeyClass.append(BindingGeneratorUtil.packageNameForSubGeneratedType(basePackageName, schemaNode,
                            BindingNamespaceType.Key))
                            .append('.')
                            .append(className)
                            .append("Key");

                    final List<QName> keyDef = ((ListSchemaNode)schemaNode).getKeyDefinition();
                    if (keyDef != null && !keyDef.isEmpty()) {
                        sb.append("@see ");
                        sb.append(linkToKeyClass);
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
            String formedYang = YangSnippetCleaner.clean(yangTemplateForModule.render(module).body());
            sb.append(encodeAngleBrackets(formedYang));
            sb.append("</pre>");
        }

        return replaceAllIllegalChars(sb);
    }

    static String createDescription(final Set<? extends SchemaNode> schemaNodes, final Module module, final
            boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();

        if (!isNullOrEmpty(schemaNodes)) {
            final SchemaNode node = schemaNodes.iterator().next();

            if (node instanceof RpcDefinition) {
                sb.append("Interface for implementing the following YANG RPCs defined in module <b>" + module.getName() + "</b>");
            } else if (node instanceof NotificationDefinition) {
                sb.append("Interface for receiving the following YANG notifications defined in module <b>" + module.getName() + "</b>");
            }
        }
        sb.append(NEW_LINE);

        if (verboseClassComments) {
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(encodeAngleBrackets(yangTemplateForNodes.render(schemaNodes, module).body()));
            sb.append("</pre>");
            sb.append(NEW_LINE);
        }

        return replaceAllIllegalChars(sb);
    }

    private static boolean isNullOrEmpty(final Collection<?> list) {
        return list == null || list.isEmpty();
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
     * @param unknownSchemaNodes unknown schema nodes
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

    /**
     * Adds enumeration builder created from <code>enumTypeDef</code> to
     * <code>typeBuilder</code>.
     *
     * Each <code>enumTypeDef</code> item is added to builder with its name and
     * value.
     *
     * @param enumTypeDef
     *            EnumTypeDefinition contains enum data
     * @param enumName
     *            string contains name which will be assigned to enumeration
     *            builder
     * @param typeBuilder
     *            GeneratedTypeBuilder to which will be enum builder assigned
     * @param module
     *            Module in which type should be generated
     * @return enumeration builder which contains data from
     *         <code>enumTypeDef</code>
     */
    static EnumBuilder resolveInnerEnumFromTypeDefinition(final EnumTypeDefinition enumTypeDef, final QName enumName,
                                                          final Map<Module, ModuleContext> genCtx, final GeneratedTypeBuilder typeBuilder, final Module module) {
        if (enumTypeDef != null && typeBuilder != null && enumTypeDef.getQName().getLocalName() != null) {
            final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumName.getLocalName(), genCtx.get(module));
            final String enumTypedefDescription = encodeAngleBrackets(enumTypeDef.getDescription());
            enumBuilder.setDescription(enumTypedefDescription);
            enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
            final ModuleContext ctx = genCtx.get(module);
            ctx.addInnerTypedefType(enumTypeDef.getPath(), enumBuilder);
            return enumBuilder;
        }
        return null;
    }


    /**
     * Builds generated TO builders for <code>typeDef</code> of type
     * {@link UnionTypeDefinition} or {@link BitsTypeDefinition} which are
     * also added to <code>typeBuilder</code> as enclosing transfer object.
     *
     * If more then one generated TO builder is created for enclosing then all
     * of the generated TO builders are added to <code>typeBuilder</code> as
     * enclosing transfer objects.
     *
     * @param typeDef
     *            type definition which can be of type <code>UnionType</code> or
     *            <code>BitsTypeDefinition</code>
     * @param typeBuilder
     *            generated type builder to which is added generated TO created
     *            from <code>typeDef</code>
     * @param leaf
     *            string with name for generated TO builder
     * @param parentModule
     *            parent module
     * @return generated TO builder for <code>typeDef</code>
     */
    static GeneratedTOBuilder addTOToTypeBuilder(final TypeDefinition<?> typeDef, final GeneratedTypeBuilder
            typeBuilder, final DataSchemaNode leaf, final Module parentModule, final TypeProvider typeProvider,
            final SchemaContext schemaContext, ModuleContext context) {
        final String classNameFromLeaf = leaf.getQName().getLocalName();
        GeneratedTOBuilder genTOBuilder = null;
        final String packageName = typeBuilder.getFullyQualifiedName();
        if (typeDef instanceof UnionTypeDefinition) {
            genTOBuilder = ((TypeProviderImpl) typeProvider)
                    .provideGeneratedTOBuilderForUnionTypeDef(packageName, ((UnionTypeDefinition) typeDef),
                            classNameFromLeaf, leaf, schemaContext,
                            ((TypeProviderImpl) typeProvider).getGenTypeDefsContextMap(), context);
        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilder = (((TypeProviderImpl) typeProvider)).provideGeneratedTOBuilderForBitsTypeDefinition(
                    packageName, typeDef, classNameFromLeaf, parentModule.getName(), context);
        }
        if (genTOBuilder != null) {
            typeBuilder.addEnclosingTransferObject(genTOBuilder);
            return genTOBuilder;
        }
        return null;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static Type createReturnTypeForUnion(final GeneratedTOBuilder genTOBuilder, final TypeDefinition<?> typeDef,
            final GeneratedTypeBuilder typeBuilder, final Module parentModule, final TypeProvider typeProvider) {
        final GeneratedTOBuilderImpl returnType = (GeneratedTOBuilderImpl) genTOBuilder;
        final String typedefDescription = encodeAngleBrackets(typeDef.getDescription());

        returnType.setDescription(typedefDescription);
        returnType.setReference(typeDef.getReference());
        returnType.setSchemaPath((List) typeDef.getPath().getPathFromRoot());
        returnType.setModuleName(parentModule.getName());

        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        TypeProviderImpl.addUnitsToGenTO(genTOBuilder, typeDef.getUnits());

        return returnType.toInstance();
    }

    static boolean isInnerType(final LeafSchemaNode leaf, final TypeDefinition<?> type) {
        return leaf.getPath().equals(type.getPath()) || leaf.getPath().equals(type.getPath().getParent());

    }

    /**
     * Generates for the <code>list</code> which contains any list keys special
     * generated TO builder.
     *
     * @param packageName
     *            string with package name to which the list belongs
     * @param list
     *            schema node of list
     * @return generated TO builder which represents the keys of the
     *         <code>list</code> or empty TO builder if <code>list</code> is null or list of
     *         key definitions is null or empty.
     */
    static GeneratedTOBuilder resolveListKeyTOBuilder(final String packageName, final ListSchemaNode list,
            ModuleContext context) {
        GeneratedTOBuilder genTOBuilder = null;
        if ((list.getKeyDefinition() != null) && (!list.getKeyDefinition().isEmpty())) {
            // underscore used as separator for distinction of class name parts
            final String genTOName =
                    new StringBuilder(list.getQName().getLocalName()).append('_').append(BindingNamespaceType.Key)
                            .toString();
            genTOBuilder = new GeneratedTOBuilderImpl(packageName, genTOName, context);
        }
        return genTOBuilder;
    }

    static GeneratedTypeBuilder resolveListKeyTypeBuilder(final String packageName, final ListSchemaNode list,
            ModuleContext context) {
        GeneratedTypeBuilder genTypeBuilder = null;
        if ((list.getKeyDefinition() != null) && (!list.getKeyDefinition().isEmpty())) {
            // underscore used as separator for distinction of class name parts
            final String genTOName =
                    new StringBuilder(list.getQName().getLocalName()).append('_').append(BindingNamespaceType.Key)
                            .toString();
            genTypeBuilder = new GeneratedTypeBuilderImpl(packageName, genTOName, context);
        }
        return genTypeBuilder;
    }

    /**
     * Converts <code>leaf</code> schema node to property of generated TO
     * builder.
     *
     * @param toBuilder
     *            generated TO builder to which is <code>leaf</code> added as
     *            property
     * @param leaf
     *            leaf schema node which is added to <code>toBuilder</code> as
     *            property
     * @param returnType
     *            property type
     * @param isReadOnly
     *            boolean value which says if leaf property is|isn't read only
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code>, <code>toBuilder</code> or leaf
     *         name equals null or if leaf is added by <i>uses</i>.</li>
     *         <li>true - other cases</li>
     *         </ul>
     */
    static boolean resolveLeafSchemaNodeAsProperty(final String nodeName, final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
        final Type returnType, final boolean isReadOnly) {

        if (returnType == null) {
            return false;
        }
        final String leafName = leaf.getQName().getLocalName();
        final String leafGetterName;

        if ("key".equals(leafName.toLowerCase())) {
            StringBuilder sb = new StringBuilder(leafName)
                    .append('_').append("RESERVED_WORD");
            leafGetterName = sb.toString();
        } else {
            leafGetterName = leafName;
        }

        final String leafDesc = encodeAngleBrackets(leaf.getDescription());
        final GeneratedPropertyBuilder propBuilder =
                toBuilder.addProperty(JavaIdentifierNormalizer.normalizeSpecificIdentifier(leafGetterName, JavaIdentifier.METHOD));
        propBuilder.setReadOnly(isReadOnly);
        propBuilder.setReturnType(returnType);
        propBuilder.setComment(leafDesc);
        toBuilder.addEqualsIdentity(propBuilder);
        toBuilder.addHashIdentity(propBuilder);
        toBuilder.addToStringProperty(propBuilder);
        return true;
    }

    static void checkModuleAndModuleName(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
    }

    @VisibleForTesting
    public static String replaceAllIllegalChars(final StringBuilder stringBuilder){
        final String ret = UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
