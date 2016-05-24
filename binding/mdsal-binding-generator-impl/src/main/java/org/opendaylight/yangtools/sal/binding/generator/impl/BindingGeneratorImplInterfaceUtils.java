package org.opendaylight.yangtools.sal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.DATA_OBJECT;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.augmentable;
import static org.opendaylight.yangtools.binding.generator.util.Types.typeForClass;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.binding.generator.util.BindingTypes;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public final class BindingGeneratorImplInterfaceUtils {
    private static final Splitter BSDOT_SPLITTER = Splitter.on("\\.");
    private static final char NEW_LINE = '\n';
    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    private BindingGeneratorImplInterfaceUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
                                                               final Module module) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null, module);
    }

    /**
     * Instantiates generated type builder with <code>packageName</code> and
     * <code>schemaNode</code>.
     *
     * The new builder always implements
     * {@link org.opendaylight.yangtools.yang.binding.DataObject DataObject}.<br>
     * If <code>schemaNode</code> is instance of GroupingDefinition it also
     * implements {@link org.opendaylight.yangtools.yang.binding.Augmentable
     * Augmentable}.<br>
     * If <code>schemaNode</code> is instance of
     * {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer
     * DataNodeContainer} it can also implement nodes which are specified in
     * <i>uses</i>.
     *
     * @param packageName
     *            string with the name of the package to which
     *            <code>schemaNode</code> belongs.
     * @param schemaNode
     *            schema node for which is created generated type builder
     * @param parent
     *            parent type (can be null)
     * @return generated type builder <code>schemaNode</code>
     */
    public static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
                                                               final Type parent, final Module module) {
        final GeneratedTypeBuilder it = addRawInterfaceDefinition(packageName, schemaNode, "");
        if (parent == null) {
            it.addImplementsType(DATA_OBJECT);
        } else {
            it.addImplementsType(BindingTypes.childOf(parent));
        }
        if (!(schemaNode instanceof GroupingDefinition)) {
            it.addImplementsType(augmentable(it));
        }

        if (schemaNode instanceof DataNodeContainer) {
            GroupingResolver.groupingsToGenTypes(module, ((DataNodeContainer) schemaNode).getGroupings(), null);
            addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, it);
        }

        return it;
    }

    /**
     * Wraps the calling of the same overloaded method.
     *
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @return generated type builder for <code>schemaNode</code>
     */
    public static GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode) {
        return addRawInterfaceDefinition(packageName, schemaNode, "");
    }

    /**
     * Returns reference to generated type builder for specified
     * <code>schemaNode</code> with <code>packageName</code>.
     *
     * Firstly the generated type builder is searched in
     * {@link BindingGeneratorImpl#genTypeBuilders genTypeBuilders}. If it isn't
     * found it is created and added to <code>genTypeBuilders</code>.
     *
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @param prefix
     *            return type name prefix
     * @return generated type builder for <code>schemaNode</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> is null</li>
     *             <li>if <code>packageName</code> is null</li>
     *             <li>if QName of schema node is null</li>
     *             <li>if schemaNode name is null</li>
     *             </ul>
     *
     */
    public static GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
                                                           final String prefix) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(packageName != null, "Package Name for Generated Type cannot be NULL.");
        checkArgument(schemaNode.getQName() != null, "QName for Data Schema Node cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        String genTypeName;
        if (prefix == null) {
            genTypeName = BindingMapping.getClassName(schemaNodeName);
        } else {
            genTypeName = prefix + BindingMapping.getClassName(schemaNodeName);
        }

        // FIXME: Validation of name conflict
        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
        final Module module = findParentModule(schemaContext, schemaNode);
        qnameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        newType.addComment(schemaNode.getDescription());
        newType.setDescription(createDescription(schemaNode, newType.getFullyQualifiedName(), true));
        newType.setReference(schemaNode.getReference());
        newType.setSchemaPath(schemaNode.getPath().getPathFromRoot());
        newType.setModuleName(module.getName());

        if (!genTypeBuilders.containsKey(packageName)) {
            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
            builders.put(genTypeName, newType);
            genTypeBuilders.put(packageName, builders);
        } else {
            final Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
            if (!builders.containsKey(genTypeName)) {
                builders.put(genTypeName, newType);
            }
        }
        return newType;
    }

    /**
     * Adds the implemented types to type builder.
     *
     * The method passes through the list of <i>uses</i> in
     * {@code dataNodeContainer}. For every <i>use</i> is obtained corresponding
     * generated type from {@link BindingGeneratorImpl
     * allGroupings} which is added as <i>implements type</i> to
     * <code>builder</code>
     *
     * @param dataNodeContainer
     *            element which contains the list of used YANG groupings
     * @param builder
     *            builder to which are added implemented types according to
     *            <code>dataNodeContainer</code>
     * @return generated type builder with all implemented types
     */
    public static GeneratedTypeBuilder addImplementedInterfaceFromUses(final DataNodeContainer dataNodeContainer,
                                                                 final GeneratedTypeBuilder builder) {
        for (final UsesNode usesNode : dataNodeContainer.getUses()) {
            if (usesNode.getGroupingPath() != null) {
                final GeneratedType genType = findGroupingByPath(usesNode.getGroupingPath()).toInstance();
                if (genType == null) {
                    throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                            + builder.getName());
                }

                builder.addImplementsType(genType);
            }
        }
        return builder;
    }

    public static void annotateDeprecatedIfNecessary(final Status status, final GeneratedTypeBuilder builder) {
        if (status == Status.DEPRECATED) {
            builder.addAnnotation("", "Deprecated");
        }
    }

    public static Constant qnameConstant(final GeneratedTypeBuilderBase<?> toBuilder, final String constantName,
                                         final QName name) {
        return toBuilder.addConstant(typeForClass(QName.class), constantName, name);
    }

    public static String createDescription(final Set<? extends SchemaNode> schemaNodes, final String moduleName,
        final boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();

        if (!isNullOrEmpty(schemaNodes)) {
            final SchemaNode node = schemaNodes.iterator().next();

            if (node instanceof RpcDefinition) {
                sb.append("Interface for implementing the following YANG RPCs defined in module <b>" + moduleName + "</b>");
            } else if (node instanceof NotificationDefinition) {
                sb.append("Interface for receiving the following YANG notifications defined in module <b>" + moduleName + "</b>");
            }
        }
        sb.append(NEW_LINE);

        if (verboseClassComments) {
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(encodeAngleBrackets(YangTemplate.generateYangSnipet(schemaNodes)));
            sb.append("</pre>");
            sb.append(NEW_LINE);
        }

        return replaceAllIllegalChars(sb);
    }

    public static String createDescription(final SchemaNode schemaNode, final String fullyQualifiedName, final
        boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();
        final String nodeDescription = encodeAngleBrackets(schemaNode.getDescription());
        final String formattedDescription = YangTextTemplate.formatToParagraph(nodeDescription, 0);

        if (!Strings.isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            final Module module = findParentModule(schemaContext, schemaNode);
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
            sb.append(encodeAngleBrackets(YangTemplate.generateYangSnipet(schemaNode)));
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

    public static String createDescription(final Module module, final boolean verboseClassComments) {
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
            sb.append(encodeAngleBrackets(YangTemplate.generateYangSnipet(module)));
            sb.append("</pre>");
        }

        return replaceAllIllegalChars(sb);
    }

    private static boolean hasBuilderClass(final SchemaNode schemaNode) {
        if (schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode ||
                schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition) {
            return true;
        }
        return false;
    }

    private static String replaceAllIllegalChars(final StringBuilder stringBuilder){
        final String ret = UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }

    private static boolean isNullOrEmpty(final Collection<?> list) {
        return list == null || list.isEmpty();
    }
}