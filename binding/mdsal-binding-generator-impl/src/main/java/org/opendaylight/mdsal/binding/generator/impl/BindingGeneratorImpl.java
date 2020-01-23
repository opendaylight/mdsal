/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingGeneratorImpl implements BindingGenerator, BindingRuntimeGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BindingGeneratorImpl.class);

    /**
     * Resolves generated types from <code>context</code> schema nodes only for modules specified
     * in <code>modules</code>. Generated types are created for modules, groupings, types, containers, lists, choices,
     * augments, rpcs, notification, identities.
     *
     * @param context schema context which contains data about all schema nodes saved in modules
     * @param modules set of modules for which schema nodes should be generated types
     * @return list of types (usually <code>GeneratedType</code> or
     *         <code>GeneratedTransferObject</code>) which:
     *         <ul>
     *         <li>are generated from <code>context</code> schema nodes and</li>
     *         <li>are also part of some of the module in <code>modules</code>
     *         set.</li>
     *         </ul>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if arg <code>context</code> is null or</li>
     *             <li>if arg <code>modules</code> is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context, final Collection<? extends Module> modules) {
        checkContext(context);
        checkArgument(modules != null, "Set of Modules cannot be NULL.");

        final Map<SchemaNode, JavaTypeName> renames = new IdentityHashMap<>();
        for (;;) {
            try {
                return new CodegenTypeGenerator(context, renames).toTypes(modules);
            } catch (RenameMappingException e) {
                rename(renames, e);
            }
        }
    }

    @Override
    public BindingRuntimeTypes generateTypeMapping(final SchemaContext context) {
        checkContext(context);

        final Map<SchemaNode, JavaTypeName> renames = new IdentityHashMap<>();
        for (;;) {
            try {
                return new RuntimeTypeGenerator(context, renames).toTypeMapping();
            } catch (RenameMappingException e) {
                rename(renames, e);
            }
        }
    }

    private static void checkContext(final SchemaContext context) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
    }

    private static void rename(final Map<SchemaNode, JavaTypeName> renames, final RenameMappingException ex) {
        final JavaTypeName name = ex.getName();
        final SchemaNode def = ex.getDefinition();
        final JavaTypeName existing = renames.get(def);
        if (existing != null) {
            throw new IllegalStateException("Attempted to relocate " + def + " to " + name + ", already remapped to "
                    + existing, ex);
        }

        final String suffix;
        if (def instanceof IdentitySchemaNode) {
            suffix = "$I";
        } else if (def instanceof GroupingDefinition) {
            suffix = "$G";
        } else if (def instanceof TypeDefinition) {
            suffix = "$T";
        } else {
            throw new IllegalStateException("Unhandled remapping of " + def + " at " + name, ex);
        }

        final JavaTypeName newName = name.createSibling(name.simpleName() + suffix);
        renames.put(def, newName);
        LOG.debug("Restarting code generation after remapping {} to {}", name, newName);
    }
}
