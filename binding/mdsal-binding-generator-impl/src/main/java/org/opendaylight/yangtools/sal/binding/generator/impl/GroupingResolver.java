package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;

final class GroupingResolver {
    private GroupingResolver() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts all <b>groupings</b> of the module to the list of
     * <code>Type</code> objects. Firstly are groupings sorted according mutual
     * dependencies. At least dependent (independent) groupings are in the list
     * saved at first positions. For every grouping the record is added to map
     *
     * @param module
     *            current module
     * @param groupings
     *            of groupings from which types will be generated
     *
     */
    static void groupingsToGenTypes(final Module module, final Collection<GroupingDefinition> groupings, final
                                    Map<Module, ModuleContext> genCtx) {
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort()
                .sort(groupings);
        for (final GroupingDefinition grouping : groupingsSortedByDependencies) {
            groupingToGenType(basePackageName, grouping, module, genCtx);
        }
    }

    /**
     * Converts individual grouping to GeneratedType. Firstly generated type
     * builder is created and every child node of grouping is resolved to the
     * method.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param grouping
     *            GroupingDefinition which contains data about grouping
     * @param module
     *            current module
     * @return GeneratedType which is generated from grouping (object of type
     *         <code>GroupingDefinition</code>)
     */
    static void groupingToGenType(final String basePackageName, final GroupingDefinition grouping, final Module
            module, final Map<Module, ModuleContext> genCtx) {
        final String packageName = packageNameForGeneratedType(basePackageName, grouping.getPath());
        final GeneratedTypeBuilder genType = BindingGeneratorImplInterfaceUtils.addDefaultInterfaceDefinition(packageName,
                grouping, module);
        BindingGeneratorImplInterfaceUtils.annotateDeprecatedIfNecessary(grouping.getStatus(), genType);
        genCtx.get(module).addGroupingType(grouping.getPath(), genType);
        BindingGeneratorImplInterfaceUtils.resolveDataSchemaNodes(module, basePackageName, genType, genType, grouping
                .getChildNodes());
        groupingsToGenTypes(module, grouping.getGroupings(), genCtx);
        BindingGeneratorImplInterfaceUtils.processUsesAugments(grouping, module);
    }
}