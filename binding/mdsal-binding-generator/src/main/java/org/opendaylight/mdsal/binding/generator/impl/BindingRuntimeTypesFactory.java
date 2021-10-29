/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Verify.verify;

import com.google.common.base.Stopwatch;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.mdsal.binding.generator.impl.reactor.IdentityGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.ModuleGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BindingRuntimeTypesFactory implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeTypesFactory.class);

    // Modules, indexed by their QNameModule
    private final Map<QNameModule, ModuleRuntimeType> modules = new HashMap<>();

    // Identities, indexed by their QName
    private final Map<QName, IdentityRuntimeType> identities = new HashMap<>();

    // All known types, indexed by their JavaTypeName
    private final Map<JavaTypeName, RuntimeType> allTypes = new HashMap<>();

    private BindingRuntimeTypesFactory() {
        // Hidden on purpose
    }

    static @NonNull BindingRuntimeTypes createTypes(final @NonNull EffectiveModelContext context) {
        final var moduleGens = new GeneratorReactor(context).execute(TypeBuilderFactory.runtime());

        final Stopwatch sw = Stopwatch.createStarted();
        final BindingRuntimeTypesFactory factory = new BindingRuntimeTypesFactory();
        factory.indexModules(moduleGens);
        LOG.debug("Indexed {} generators in {}", moduleGens.size(), sw);

//        return new BindingRuntimeTypes(context, factory.augmentationToSchema, factory.typeToSchema,
//            factory.schemaToType, factory.identities);

        throw new UnsupportedOperationException();
    }

    private void indexModules(final Map<QNameModule, ModuleGenerator> moduleGens) {
        for (var entry : moduleGens.entrySet()) {
            final var modGen = entry.getValue();

            // index the module's runtime type
            modGen.runtimeType().ifPresent(type -> {
                // FIXME: fix return type and ditch this cast
                safePut(modules, "modules", entry.getKey(), type);
                putType(type);
            });

            // index module's identities
            for (var gen : modGen) {
                if (gen instanceof IdentityGenerator) {
                    ((IdentityGenerator) gen).runtimeType().ifPresent(identity -> {
                        safePut(identities, "identities", identity.statement().argument(), identity);
                        putType(identity);
                    });
                }
            }
        }

        // FIXME: we need to index groupings, typedefs, etc.
        // FIXME: toRuntimeType() needs to be idempotent
    }

    private void putType(final RuntimeType type) {
        final var name = type.getIdentifier();
        final var prev = allTypes.put(name, type);
        verify(prev == null || prev == type, "Conflict on runtime type mapping of %s between %s and %s", name, prev,
            type);
    }

    private static <K, V> void safePut(final Map<K, V> map, final String name, final K key, final V value) {
        final var prev = map.put(key, value);
        verify(prev == null, "Conflict in %s, key %s conflicts on %s versus %s", name, key, prev, value);
    }

    // FIXME: remove code below

//    private void indexTypes(final Iterable<? extends Generator> generators) {
//        for (Generator gen : generators) {
//            gen.generatedType().ifPresent(type -> indexType(gen, type));
//            indexTypes(gen);
//        }
//    }
//
//    private void indexType(final @NonNull Generator generator, final @NonNull GeneratedType type) {
//        if (generator instanceof AbstractExplicitGenerator) {
//            final EffectiveStatement<?, ?> stmt = ((AbstractExplicitGenerator<?>) generator).statement();
//            if (stmt instanceof IdentityEffectiveStatement) {
//                identities.put(((IdentityEffectiveStatement) stmt).argument(), type);
//            } else if (stmt instanceof AugmentEffectiveStatement) {
//                verify(stmt instanceof AugmentationSchemaNode, "Unexpected statement %s", stmt);
//                // FIXME: bad cast
//                verify(generator instanceof AugmentRuntimeType);
//                augmentationToSchema.put(type, (AugmentRuntimeType) generator);
//            }
//
//            final WithStatus schema;
//            if (stmt instanceof TypedDataSchemaNode) {
//                schema = ((TypedDataSchemaNode) stmt).getType();
//            } else if (stmt instanceof TypedefEffectiveStatement) {
//                schema = ((TypedefEffectiveStatement) stmt).getTypeDefinition();
//            } else if (stmt instanceof WithStatus) {
//                schema = (WithStatus) stmt;
//            } else {
//                return;
//            }
//
//            typeToSchema.put(type, schema);
//            final var prevType = schemaToType.put(schema, type);
//            verify(prevType == null, "Conflicting types %s and %s on %s", type, prevType, schema);
//        }
//    }
}
