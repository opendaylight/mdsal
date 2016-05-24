package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.Collection;
import java.util.Collections;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public final class BindingGeneratorImplUtils {
    private BindingGeneratorImplUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Adds the methods to <code>typeBuilder</code> which represent subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * The subnodes aren't mapped to the methods if they are part of grouping or
     * augment (in this case are already part of them).
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param parent
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf
     *            parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same builder as input
     *         parameter. The getter methods (representing child nodes) could be
     *         added to it.
     */
    public static GeneratedTypeBuilder resolveDataSchemaNodes(final Module module, final String basePackageName,
                                                        final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes) {
        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module);
                }
            }
        }
        return parent;
    }

    public static void processUsesAugments(final DataNodeContainer node, final Module module) {
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        for (final UsesNode usesNode : node.getUses()) {
            for (final AugmentationSchema augment : usesNode.getAugmentations()) {
                usesAugmentationToGenTypes(basePackageName, augment, module, usesNode, node);
                processUsesAugments(augment, module);
            }
        }
    }

    public static GeneratedTypeBuilder findChildNodeByPath(final SchemaPath path, final Collection<ModuleContext> values) {
        for (final ModuleContext ctx : values) {
            final GeneratedTypeBuilder result = ctx.getChildNode(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static GeneratedTypeBuilder findGroupingByPath(final SchemaPath path, final Collection<ModuleContext> values) {
        for (final ModuleContext ctx : values) {
            final GeneratedTypeBuilder result = ctx.getGrouping(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static GeneratedTypeBuilder findCaseByPath(final SchemaPath path, final Collection<ModuleContext> values) {
        for (final ModuleContext ctx : values) {
            final GeneratedTypeBuilder result = ctx.getCase(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}