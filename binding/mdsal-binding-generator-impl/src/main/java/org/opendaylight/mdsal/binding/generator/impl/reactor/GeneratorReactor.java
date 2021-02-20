/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A multi-stage reactor for generating {@link GeneratedType} instances from an {@link EffectiveModelContext}.
 *
 * <p>
 * The reason for multi-stage processing is that the problem ahead of us involves:
 * <ul>
 *   <li>mapping {@code typedef} and restricted {@code type} statements onto Java classes</li>
 *   <li>mapping a number of schema tree nodes into Java interfaces with properties</li>
 *   <li>taking advantage of Java composition to provide {@code grouping} mobility</li>
 * </ul>
 */
public final class GeneratorReactor implements Mutable {
    private enum State {
        INITIALIZED,
        EXECUTING,
        FINISHED
    }

    private final Deque<Iterable<? extends Generator<?>>> stack = new ArrayDeque<>();
    private final @NonNull Map<QNameModule, ModuleGenerator> modules;
    private final Iterable<ModuleGenerator> iterable;
    private final EffectiveModelContext context;

    private State state = State.INITIALIZED;

    private GeneratorReactor(final EffectiveModelContext context) {
        this.context = requireNonNull(context);
        // Construct modules and their subtrees
        modules = ImmutableMap.copyOf(Maps.transformValues(context.getModuleStatements(), ModuleGenerator::new));
        iterable = () -> modules.values().iterator();
    }

    /**
     * Execute the reactor. Execution follows the following steps:
     * <ol>
     *   <li>link the {@code typedef} inheritance hierarchy by all visiting all {@link TypedefGenerator}s and
     *       memoizing the {@code base} lookup</li>
     *   <li>link the {@code type} statements and resolve type restriction hierarchy, determining the set of Java
             classes required for Java equivalent of effective YANG type definitions</li>
     *   <li>bind {@code leafref} references to their Java class roots</li>
     *   <li>resolve class inheritance along {@code uses} statements</li>
     *   <li>resolve {@link ChoiceIn} hierarchy</li>
     *   <li>resolve {@link ChildOf} hierarchy</li>
     *   <li>assign Java package names and {@link JavaTypeName}s to all generated classes</li>
     *   <li>create {@link Type} instances</li>
     * </ol>
     *
     * @return Resolved generators
     * @throws IllegalStateException if the reactor already executed
     */
    public @NonNull Map<QNameModule, ModuleGenerator> execute() {
        switch (state) {
            case INITIALIZED:
                state = State.EXECUTING;
                break;
            case FINISHED:
                return modules;
            case EXECUTING:
                throw new IllegalStateException("Cannot resume partial execution");
            default:
                throw new IllegalStateException("Unhandled state" + state);
        }

        // Step one: link typedef statements, so that typedef's 'base' axis is fully established
        forEachSimpleGenerator(stack, iterable, TypedefGenerator.class, TypedefGenerator::linkBaseGenerator);

        // Step two: link all type statements, so that leafs and leaf-lists have restrictions established
        forEachSimpleGenerator(stack, iterable, AbstractTypeAwareGenerator.class, AbstractTypeAwareGenerator::linkType);

        // Step three: resolve all 'type leafref' statements, so they point to their corresponding Java type
        //             representation
        forEachSimpleGenerator(stack, iterable, AbstractLeafrefAwareGenerator.class,
            AbstractLeafrefAwareGenerator::bindLeafref);

        // FIXME: finish these
        stack.push(iterable);
        for (ModuleGenerator module : modules.values()) {
            resolveUsesInheritance(module);
        }
        for (ModuleGenerator module : modules.values()) {
            resolveChoiceInHierarchy(module);
        }
        for (ModuleGenerator module : modules.values()) {
            module.assignJavaTypeNames();
        }
        stack.pop();

        state = State.FINISHED;
        return modules;
    }

    private void resolveUsesInheritance(final ModuleGenerator module) {
        stack.push(module);
        // FIXME: implement this
        stack.pop();
    }

    private void resolveChoiceInHierarchy(final ModuleGenerator module) {
        stack.push(module);
        // FIXME: implement this
        stack.pop();
    }

    private static <T extends Generator<?>> void forEachSimpleGenerator(
            final Deque<Iterable<? extends Generator<?>>> stack, final Iterable<? extends Generator<?>> parent,
            final Class<T> type, final BiConsumer<T, Iterable<? extends Iterable<? extends Generator<?>>>> action) {
        stack.push(parent);
        for (Generator<?> child : parent) {
            if (type.isInstance(child)) {
                action.accept(type.cast(child), stack);
            } else if (child instanceof AbstractCompositeGenerator) {
                forEachSimpleGenerator(stack, (AbstractCompositeGenerator<?>) child, type, action);
            }
        }
        stack.pop();
    }
}
