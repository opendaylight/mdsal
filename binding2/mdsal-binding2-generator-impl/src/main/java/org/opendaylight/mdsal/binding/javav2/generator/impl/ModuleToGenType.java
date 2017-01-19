/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding2.util.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;

@Beta
final class ModuleToGenType {

    private ModuleToGenType() {
        throw new UnsupportedOperationException("Utility class");
    }

    static Map<Module, ModuleContext> generate(final Module module, final SchemaContext schemaContext,
                                               TypeProvider typeProvider, final boolean verboseClassComments) {
        Map<Module, ModuleContext> genCtx = new HashMap<>();

        genCtx.put(module, new ModuleContext());
        genCtx = allTypeDefinitionsToGenTypes(module, genCtx, typeProvider);

        //TODO: call generate for other entities (groupings, rpcs, identities, notifications)

        if (!module.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = GenHelperUtil.moduleToDataType(module, genCtx, verboseClassComments);
            genCtx.get(module).addModuleNode(moduleType);
            final String basePackageName = BindingMapping.getRootPackageName(module);
            GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, moduleType, moduleType, module
                    .getChildNodes());
        }

        return genCtx;
    }

    /**
     * Converts all extended type definitions of module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of type definitions
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if module is null</li>
     *             <li>if name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of type definitions from module is null
     */
    private static Map<Module, ModuleContext> allTypeDefinitionsToGenTypes(final Module module, Map<Module, ModuleContext> genCtx,
                                                     TypeProvider typeProvider) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
        Preconditions.checkState(typeDefinitions != null, "Type Definitions for module «module.name» cannot be NULL.");

        typeDefinitions.stream().filter(typedef -> typedef != null).forEach(typedef -> {
            final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef,
                    typedef);
            if (type != null) {
                final ModuleContext ctx = genCtx.get(module);
                ctx.addTypedefType(typedef.getPath(), type);
                ctx.addTypeToSchema(type, typedef);
            }
        });
        return genCtx;
    }
}
