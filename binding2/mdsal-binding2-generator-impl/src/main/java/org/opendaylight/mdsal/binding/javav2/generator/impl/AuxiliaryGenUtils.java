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
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNode;
import org.opendaylight.mdsal.binding.javav2.generator.impl.util.YangTextTemplate;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.NonJavaCharsConverter;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
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
        if ((schemaNode instanceof ContainerSchemaNode) || (schemaNode instanceof ListSchemaNode) ||
                (schemaNode instanceof RpcDefinition) || (schemaNode instanceof NotificationDefinition)) {
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
        method.append('_').append(localName);
        return NonJavaCharsConverter.convertIdentifier(method.toString(), JavaIdentifier.METHOD);
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
                    if ((keyDef != null) && !keyDef.isEmpty()) {
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
        if ((enumTypeDef != null) && (typeBuilder != null) && (enumTypeDef.getQName().getLocalName() != null)) {
            final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumName.getLocalName());
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
            final SchemaContext schemaContext) {
        final String classNameFromLeaf = leaf.getQName().getLocalName();
        final List<GeneratedTOBuilder> genTOBuilders = new ArrayList<>();
        final String packageName = typeBuilder.getFullyQualifiedName();
        if (typeDef instanceof UnionTypeDefinition) {
            final List<GeneratedTOBuilder> types = ((TypeProviderImpl) typeProvider)
                    .provideGeneratedTOBuildersForUnionTypeDef(packageName, ((UnionTypeDefinition) typeDef),
                            classNameFromLeaf, leaf, schemaContext, ((TypeProviderImpl) typeProvider).getGenTypeDefsContextMap());
            genTOBuilders.addAll(types);

            GeneratedTOBuilder resultTOBuilder;
            if (types.isEmpty()) {
                throw new IllegalStateException("No GeneratedTOBuilder objects generated from union " + typeDef);
            }
            resultTOBuilder = types.remove(0);
            for (final GeneratedTOBuilder genTOBuilder : types) {
                resultTOBuilder.addEnclosingTransferObject(genTOBuilder);
            }

            final GeneratedPropertyBuilder genPropBuilder = resultTOBuilder.addProperty("value");
            genPropBuilder.setReturnType(Types.CHAR_ARRAY);
            resultTOBuilder.addEqualsIdentity(genPropBuilder);
            resultTOBuilder.addHashIdentity(genPropBuilder);
            resultTOBuilder.addToStringProperty(genPropBuilder);

        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilders.add((((TypeProviderImpl) typeProvider)).provideGeneratedTOBuilderForBitsTypeDefinition(
                    packageName, typeDef, classNameFromLeaf, parentModule.getName()));
        }
        if (!genTOBuilders.isEmpty()) {
            for (final GeneratedTOBuilder genTOBuilder : genTOBuilders) {
                typeBuilder.addEnclosingTransferObject(genTOBuilder);
            }
            return genTOBuilders.get(0);
        }
        return null;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static Type createReturnTypeForUnion(final GeneratedTOBuilder genTOBuilder, final TypeDefinition<?> typeDef,
            final GeneratedTypeBuilder typeBuilder, final Module parentModule, final TypeProvider typeProvider) {
        final GeneratedTOBuilderImpl returnType = new GeneratedTOBuilderImpl(genTOBuilder.getPackageName(),
                genTOBuilder.getName());
        final String typedefDescription = encodeAngleBrackets(typeDef.getDescription());

        returnType.setDescription(typedefDescription);
        returnType.setReference(typeDef.getReference());
        returnType.setSchemaPath((List) typeDef.getPath().getPathFromRoot());
        returnType.setModuleName(parentModule.getName());

        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        TypeProviderImpl.addUnitsToGenTO(genTOBuilder, typeDef.getUnits());



        final GeneratedTOBuilder unionBuilder = createUnionBuilder(genTOBuilder,typeBuilder);


        final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
        method.setReturnType(returnType);
        method.addParameter(Types.STRING, "defaultValue");
        method.setAccessModifier(AccessModifier.PUBLIC);
        method.setStatic(true);

        final Set<Type> types = ((TypeProviderImpl) typeProvider).getAdditionalTypes().get(parentModule);
        if (types == null) {
            ((TypeProviderImpl) typeProvider).getAdditionalTypes().put(parentModule,
                    Sets.newHashSet(unionBuilder.toInstance()));
        } else {
            types.add(unionBuilder.toInstance());
        }
        return returnType.toInstance();
    }

    private static GeneratedTOBuilder createUnionBuilder(final GeneratedTOBuilder genTOBuilder, final GeneratedTypeBuilder typeBuilder) {
        final String outerCls = Types.getOuterClassName(genTOBuilder);
        final StringBuilder name;
        if (outerCls != null) {
            name = new StringBuilder(outerCls);
        } else {
            name = new StringBuilder();
        }
        name.append(genTOBuilder.getName());
        name.append("Builder");
        final GeneratedTOBuilderImpl unionBuilder = new GeneratedTOBuilderImpl(typeBuilder.getPackageName(),name.toString());
        unionBuilder.setIsUnionBuilder(true);
        return unionBuilder;
    }

    static boolean isInnerType(final LeafSchemaNode leaf, final TypeDefinition<?> type) {
        if (leaf.getPath().equals(type.getPath())) {
            return true;
        }
        if (leaf.getPath().equals(type.getPath().getParent())) {
            return true;
        }

        return false;
    }

    /**
     * Selects the names of the list keys from <code>list</code> and returns
     * them as the list of the strings
     *
     * @param list
     *            of string with names of the list keys
     * @return list of string which represents names of the list keys. If the
     *         <code>list</code> contains no keys then the empty list is
     *         returned.
     */
    static List<String> listKeys(final ListSchemaNode list) {
        final List<String> listKeys = new ArrayList<>();

        final List<QName> keyDefinition = list.getKeyDefinition();
        if (keyDefinition != null) {
            for (final QName keyDef : keyDefinition) {
                listKeys.add(keyDef.getLocalName());
            }
        }
        return listKeys;
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
    static GeneratedTOBuilder resolveListKeyTOBuilder(final String packageName, final ListSchemaNode list) {
        GeneratedTOBuilder genTOBuilder = null;
        if ((list.getKeyDefinition() != null) && (!list.getKeyDefinition().isEmpty())) {
            final String genTOName =
                    new StringBuilder(list.getQName().getLocalName()).append('_').append("key").toString();
            genTOBuilder =
                    new GeneratedTOBuilderImpl(new StringBuilder(packageName).append(".wrapper").toString(), genTOName);
        }
        return genTOBuilder;
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
    static boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
        final Type returnType, final boolean isReadOnly) {

        if (returnType == null) {
            return false;
        }
        final String leafName = leaf.getQName().getLocalName();
        final String leafDesc = encodeAngleBrackets(leaf.getDescription());
        final GeneratedPropertyBuilder propBuilder =
                toBuilder.addProperty(NonJavaCharsConverter.convertIdentifier(leafName, JavaIdentifier.METHOD));
        propBuilder.setReadOnly(isReadOnly);
        propBuilder.setReturnType(returnType);
        propBuilder.setComment(leafDesc);
        toBuilder.addEqualsIdentity(propBuilder);
        toBuilder.addHashIdentity(propBuilder);
        toBuilder.addToStringProperty(propBuilder);
        return true;
    }

    @VisibleForTesting
    public static String replaceAllIllegalChars(final StringBuilder stringBuilder){
        final String ret = UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
