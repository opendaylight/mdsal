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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.AbstractExplicitGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.Generator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.mdsal.binding.generator.impl.reactor.IdentityGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.InputGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.ModuleGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.OutputGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.RpcGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultBindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
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
    // All RpcOutputs, indexed by their RPC's QName
    private final Map<QName, OutputRuntimeType> rpcOutputs = new HashMap<>();
    // All RpcInputs, indexed by their RPC's QName
    private final Map<QName, InputRuntimeType> rpcInputs = new HashMap<>();
    // All known 'choice's to their corresponding cases
    private final SetMultimap<JavaTypeName, CaseRuntimeType> choiceToCases = HashMultimap.create();
    // All case to cases mapping, values are the cases that can substitute case that is the key
    private final Multimap<GeneratedType, CaseRuntimeType> caseToSubstitutionCases = HashMultimap.create();
    // All augment to augments mapping where values are augments that can substitute augment that is the key
    private final Multimap<GeneratedType, AugmentRuntimeType> augmentToSubstitutionAugments = HashMultimap.create();

    private BindingRuntimeTypesFactory() {
        // Hidden on purpose
    }

    static @NonNull BindingRuntimeTypes createTypes(final @NonNull EffectiveModelContext context) {
        final var moduleGens = new GeneratorReactor(context).execute(TypeBuilderFactory.runtime());

        final Stopwatch sw = Stopwatch.createStarted();
        final BindingRuntimeTypesFactory factory = new BindingRuntimeTypesFactory();
        factory.indexModules(moduleGens);
        LOG.debug("Indexed {} generators in {}", moduleGens.size(), sw);

        return new DefaultBindingRuntimeTypes(context, factory.modules, factory.allTypes, factory.identities,
                factory.rpcInputs, factory.rpcOutputs, factory.choiceToCases, factory.caseToSubstitutionCases,
                factory.augmentToSubstitutionAugments);
    }

    private void indexModules(final Map<QNameModule, ModuleGenerator> moduleGens) {
        for (var entry : moduleGens.entrySet()) {
            final var modGen = entry.getValue();

            // index the module's runtime type
            safePut(modules, "modules", entry.getKey(), modGen.runtimeType().orElseThrow());

            // index module's identities and RPC input/outputs
            for (var gen : modGen) {
                if (gen instanceof IdentityGenerator idGen) {
                    idGen.runtimeType().ifPresent(identity -> {
                        safePut(identities, "identities", identity.statement().argument(), identity);
                    });
                }
                // FIXME: do not collect these once we they generate a proper RuntimeType
                if (gen instanceof RpcGenerator rpcGen) {
                    final QName rpcName = rpcGen.statement().argument();
                    for (var subgen : gen) {
                        if (subgen instanceof InputGenerator inputGen) {
                            inputGen.runtimeType().ifPresent(input -> rpcInputs.put(rpcName, input));
                        } else if (subgen instanceof OutputGenerator outputGen) {
                            outputGen.runtimeType().ifPresent(output -> rpcOutputs.put(rpcName, output));
                        }
                    }
                }
            }
        }

        Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildren = new HashMap<>();
        // unresolved, comment out and see what is not yet working
        Map<AugmentRuntimeType, List<EffectiveStatement<?, ?>>> augmentToChildren = new HashMap<>();
        // unresolved
        indexRuntimeTypes(moduleGens.values(), caseToChildren, augmentToChildren);

        collectSubstsForCase(caseToChildren);
        collectSubstsForAugment(augmentToChildren);

    }

    private void indexRuntimeTypes(final Iterable<? extends Generator> generators,
            Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildren,
            Map<AugmentRuntimeType, List<EffectiveStatement<?, ?>>> augmentToChildren) {

        for (Generator gen : generators) {
            if (gen instanceof AbstractExplicitGenerator<?, ?> explicitGen && gen.generatedType().isPresent()) {
                final var type = explicitGen.runtimeType().orElseThrow();
                // calling iterator() on Generator returns Iterator<Generator> of its childGenerators
                // (check AbstractCompositeGenerator.java)
                Iterator<Generator> it = gen.iterator();
                if (type.javaType() instanceof GeneratedType genType) {
                    final var name = genType.getIdentifier();
                    final var prev = allTypes.put(name, type);
                    verify(prev == null || prev == type, "Conflict on runtime type mapping of %s between %s and %s",
                        name, prev, type);

                    // Global indexing of cases generated for a particular choice. We look at the Generated type
                    // and make assumptions about its shape -- which works just fine without touching the
                    // ChoiceRuntimeType for cases.
                    if (type instanceof CaseRuntimeType caseType) {
                        caseToChildren.put(caseType, generatorsToStatements(it));
                        final var ifaces = genType.getImplements();
                        // The appropriate choice and DataObject at the very least. The choice interface is the first
                        // one mentioned.
                        verify(ifaces.size() >= 2, "Unexpected implemented interfaces %s", ifaces);
                        choiceToCases.put(ifaces.get(0).getIdentifier(), caseType);
                    }

                    if (type instanceof AugmentRuntimeType augmentType) {
                        augmentToChildren.put(augmentType, generatorsToStatements(it));
                    }
                }
            }
            indexRuntimeTypes(gen, caseToChildren, augmentToChildren);
        }
    }

    /**
     * {@link Generator} represent a single node in a generator tree.
     * {@link AbstractExplicitGenerator} is a Generator associated with a particular {@link EffectiveStatement}.
     * This method maps {@link Iterator} of {@link Generator}s to {@link List} of {@link EffectiveStatement}s.
     *
     * @param iterator iterator over a collection of child generators
     * @return  list of statements corresponding to the generators
     */
    private static List<EffectiveStatement<?, ?>> generatorsToStatements(Iterator<Generator> iterator) {
        List<Generator> preResult = new ArrayList<>();
        iterator.forEachRemaining(preResult::add);
        return preResult.stream()
                .map(gen -> ((AbstractExplicitGenerator<?, ?>) gen).statement())
                .collect(Collectors.toList());
    }

    /**
     * This method populates {@code caseToSubstitutionCases} that maps each case to its possible substitutions.
     *
     * @param caseToChildernStmts map of case to its corresponding children
     */
    private void collectSubstsForCase(Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildernStmts) {
        Multimap<GeneratedType, CaseRuntimeType> localToSubstitutions = HashMultimap.create();
        for (Entry<JavaTypeName, CaseRuntimeType> entry : choiceToCases.entries()) {
            JavaTypeName choice = entry.getKey();
            // CaseRuntimeTypes associated with this choice
            Collection<CaseRuntimeType> caseTypes = choiceToCases.get(choice);

            for (CaseRuntimeType caseType : caseTypes) {
                addSubstitutionalCases(localToSubstitutions, caseType, caseTypes, caseToChildernStmts);
            }
        }
        caseToSubstitutionCases.putAll(localToSubstitutions);
    }


    /**
     * Put to map:
     * <ul>
     *  <li>key - GeneratedType - the CaseRuntimeType.javaType() we want to substitute</li>
     *  <li>value - CaseRuntimeType - the type that can be used as a substitution</li>
     *</ul>
     *
     * <p>
     * Two cases could be substituted only if and if:
     * <ul>
     *   <li>Both implements same interfaces</li>
     *   <li>Both have same children</li>
     *   <li>Both are from same choice. Choice class was generated for data node in grouping.</li>
     * </ul>
     *
     * <p>
     * The last condition can be ignored in this case, since the {@code candidates} are extracted
     * from {@code choiceToCases}, which already provides only cases from one particular choice (containing also
     * all the augmented cases belonging to that choice).
     *
     * @param localToSubstitutions mapping from case to all cases that could be used as a substitution
     * @param local current CaseType for which substitutions we are looking for
     * @param candidates available cases from one particular choice
     */
    private static void addSubstitutionalCases(Multimap<GeneratedType, CaseRuntimeType> localToSubstitutions,
            CaseRuntimeType local,
            Collection<CaseRuntimeType> candidates,
            Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildernStmts) {

        for (CaseRuntimeType candidate : candidates) {
            if (candidate.equals(local)) {
                continue;
            }
            boolean substitutional;
            // check if both have same interfaces
            substitutional = candidate.javaType().getImplements().equals(local.javaType().getImplements());
            // check if both have same children
            substitutional &= caseToChildernStmts.get(candidate).equals(caseToChildernStmts.get(local));

            // no need to check if they are from same choice
            if (substitutional) {
                localToSubstitutions.put(local.javaType(), candidate);
            }
        }
    }

    private void collectSubstsForAugment(Map<AugmentRuntimeType, List<EffectiveStatement<?, ?>>> augToChildrenStmts) {
        // let's use allStmts for now
        for (var runtimeType : allTypes.values()) {
            // skip types that are not interesting
            if (!(runtimeType instanceof AugmentRuntimeType augmentType)) {
                continue;
            }
            // do not compare the same instance to itself
            for (var entryAugToChildren : augToChildrenStmts.entrySet()) {
                if (augmentType.equals(entryAugToChildren.getKey())) {
                    continue;
                }
                var substitution = entryAugToChildren.getKey();
                var substitChildren = entryAugToChildren.getValue();
                var substitIfaces = entryAugToChildren.getKey().javaType().getImplements();

                var runtimeTypeChildren = augToChildrenStmts.get(runtimeType);
                var runtimeTypeIfaces = augmentType.javaType().getImplements();

                boolean substitutional;
                // check if both have same interfaces
                substitutional = substitIfaces.equals(runtimeTypeIfaces);
                // check if both have same children
                substitutional &= substitChildren.equals(runtimeTypeChildren);
                // the last check needs to verify if they have the same target class
                AugmentEffectiveStatement typeTarget = augmentType.statement();
                AugmentEffectiveStatement substitTarget = augmentType.statement();
                substitutional &= typeTarget.equals(substitTarget);

                // if the 'augmentType' can be substituted for 'substitution', add it to the map
                if (substitutional) {
                    augmentToSubstitutionAugments.put(augmentType.javaType(), substitution);
                }
            }
        }
    }

    private static <K, V> void safePut(final Map<K, V> map, final String name, final K key, final V value) {
        final var prev = map.put(key, value);
        verify(prev == null, "Conflict in %s, key %s conflicts on %s versus %s", name, key, prev, value);
    }
}
