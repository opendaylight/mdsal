/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * Default implementation of {@link BindingGenerator}.
 */
@Beta
@MetaInfServices
@Singleton
// Note: not exposed in OSGi on purpose, as this should only be needed at compile-time
public final class DefaultBindingGenerator implements BindingGenerator {
    @Override
    public List<Type> generateTypes(final SchemaContext context, final Collection<? extends Module> modules) {
        return generateFor(context, modules);
    }

    @VisibleForTesting
    static @NonNull List<Type> generateFor(final SchemaContext context) {
        return generateFor(context, context.getModules());
    }

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
    @VisibleForTesting
    static @NonNull List<Type> generateFor(final SchemaContext context, final Collection<? extends Module> modules) {
        GeneratorUtils.checkContext(context);
        checkArgument(modules != null, "Set of Modules cannot be NULL.");

        final Map<SchemaNode, JavaTypeName> renames = new IdentityHashMap<>();
        for (;;) {
            try {
                return new CodegenTypeGenerator(context, renames).toTypes(modules);
            } catch (RenameMappingException e) {
                GeneratorUtils.rename(renames, e);
            }
        }
    }
}
