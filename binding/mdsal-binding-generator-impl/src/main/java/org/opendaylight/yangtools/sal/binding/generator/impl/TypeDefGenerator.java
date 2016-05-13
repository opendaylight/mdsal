/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;

final class TypeDefGenerator {
    private final TypeProvider typeProvider;
    private final Map<Module, ModuleContext> genCtx;

    public TypeDefGenerator(final TypeProvider typeProvider, final Map<Module, ModuleContext> genCtx) {
        this.typeProvider = typeProvider;
        this.genCtx = genCtx;
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
    public void allTypeDefinitionsToGenTypes(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
//        final DataNodeIterator it = new DataNodeIterator(module);
        final Set<TypeDefinition<?>> typeDefinitions = module.getTypeDefinitions();
        checkState(typeDefinitions != null, "Type Definitions for module «module.name» cannot be NULL.");

        for (final TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef != null) {
                final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef,
                        typedef);
                if (type != null) {
                    final ModuleContext ctx = genCtx.get(module);
                    ctx.addTypedefType(typedef.getPath(), type);
                    ctx.addTypeToSchema(type,typedef);
                }
            }
        }
    }
}