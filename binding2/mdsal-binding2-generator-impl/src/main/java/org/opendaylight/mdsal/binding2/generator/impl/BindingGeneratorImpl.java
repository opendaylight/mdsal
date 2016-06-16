/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

/**
 * Main class for Binding generator v2. Provides transformation of Schema Context to
 * generated transfer objects. Process is accompanied with Twirl templates to generate
 * particular Javadoc for related YANG elements.
 */
@Beta
public class BindingGeneratorImpl implements BindingGenerator {

    /**
     * When set to true, generated classes will include Javadoc comments
     * which are useful for users.
     */
    private final boolean verboseClassComments;

    /**
     * Outer key represents the package name. Outer value represents map of all
     * builders in the same package. Inner key represents the schema node name
     * (in JAVA class/interface name format). Inner value represents instance of
     * builder for schema node specified in key part.
     */
    private Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders;

    private Map<Module, ModuleContext> genCtx = new HashMap<>();

    /**
     * Creates a new binding generator v2.
     *
     * @param verboseClassComments generate verbose comments
     */
    public BindingGeneratorImpl(final boolean verboseClassComments) {
        this.verboseClassComments = verboseClassComments;
    }

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private TypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented element
     * when creating augmentation builder
     */
    private SchemaContext schemaContext;

    /**
     * Resolves generated types from <code>context</code> schema nodes of all
     * modules.
     *
     * Generated types are created for modules, groupings, types, containers,
     * lists, choices, augments, rpcs, notification, identities.
     *
     * @param context
     *            schema context which contains data about all schema nodes
     *            saved in modules
     * @return list of types (usually <code>GeneratedType</code>
     *         <code>GeneratedTransferObject</code>which are generated from
     *         <code>context</code> data.
     * @throws IllegalArgumentException
     *             if arg <code>context</code> is null
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(SchemaContext context) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Set<Module> modules = context.getModules();
        return generateTypes(context, modules);
    }

    @Override
    public List<Type> generateTypes(SchemaContext context, Set<Module> modules) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        checkArgument(modules != null, "Set of Modules cannot be NULL.");

        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Module[] modulesArray = new Module[context.getModules().size()];
        context.getModules().toArray(modulesArray);
        final List<Module> contextModules = ModuleDependencySort.sort(modulesArray);

        genTypeBuilders = new HashMap<>();

        for (final Module contextModule : contextModules) {
            genCtx = ModuleToGenType.generate(contextModule, context, typeProvider, verboseClassComments);
        }
        for (final Module contextModule : contextModules) {
            genCtx = AugmentToGenType.generate(contextModule, schemaContext, genCtx,
                    genTypeBuilders, verboseClassComments);
        }

        final List<Type> filteredGenTypes = new ArrayList<>();
        for (final Module m : modules) {
            final ModuleContext ctx = checkNotNull(genCtx.get(m), "Module context not found for module %s", m);
            filteredGenTypes.addAll(ctx.getGeneratedTypes());
            final Set<Type> additionalTypes = ((TypeProviderImpl) typeProvider).getAdditionalTypes().get(m);
            if (additionalTypes != null) {
                filteredGenTypes.addAll(additionalTypes);
            }
        }

        return filteredGenTypes;
    }
}
