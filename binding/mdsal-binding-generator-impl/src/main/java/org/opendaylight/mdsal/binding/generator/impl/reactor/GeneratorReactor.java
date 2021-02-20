/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class GeneratorReactor extends GeneratorContext implements Mutable {
    private enum State {
        INITIALIZED,
        EXECUTING,
        FINISHED
    }

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorReactor.class);

    private final Deque<Iterable<? extends Generator<?>>> stack = new ArrayDeque<>();
    private final @NonNull Map<QNameModule, ModuleGenerator> generators;
    private final Collection<ModuleGenerator> children;
    private final SchemaInferenceStack inferenceStack;

    private State state = State.INITIALIZED;

    public GeneratorReactor(final EffectiveModelContext context) {
        inferenceStack = SchemaInferenceStack.of(context);
        // Construct modules and their subtrees
        generators = ImmutableMap.copyOf(Maps.transformValues(context.getModuleStatements(), ModuleGenerator::new));
        children = generators.values();
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
     *   <li>resolve {@link ChoiceIn}/{@link ChildOf} hierarchy</li>
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
                return generators;
            case EXECUTING:
                throw new IllegalStateException("Cannot resume partial execution");
            default:
                throw new IllegalStateException("Unhandled state" + state);
        }

        /*
         * Step one: link typedef statements, so that typedef's 'base' axis is fully established
         * Step two: link all type statements, so that leafs and leaf-lists have restrictions established
         *
         * Since our implementation class hierarchy captures all three statements involved and we do not attempt to
         * look go through the hierarchy.
         */
        forEachTypeObjectGenerator(children, AbstractTypeObjectGenerator::linkType);

        // Step three: resolve all 'type leafref' statements, so they point to their corresponding Java type
        //             representation
        forEachTypeObjectGenerator(children, AbstractTypeObjectGenerator::bindLeafref);

        // Step four: walk all composite generators and resolve 'uses' statements to the corresponding grouping node,
        //            establishing implied inheritance
        forEachCompositeGenerator(AbstractCompositeGenerator::linkUses);

        // Step five: walk all composite generators and link ChildOf/ChoiceIn relationships with parents
        for (ModuleGenerator module : children) {
            for (Generator<?> child : module) {
                if (child instanceof AbstractCompositeGenerator) {
                    recursiveLinkParent((AbstractCompositeGenerator<?>) child, module);
                }
            }
        }

        // FIXME: finish these
        stack.push(children);
        for (ModuleGenerator module : children) {
            module.assignJavaTypeNames();
        }
        stack.pop();

        state = State.FINISHED;
        return generators;
    }

    @Override
    <E extends EffectiveStatement<QName, ?>, G extends Generator<E>> G resolveTreeScoped(final Class<G> type,
            final QName argument) {
        LOG.info("Searching for tree-scoped argument {} at {}", argument, stack);

        // FIXME: recognize when this is coming from a different module and search only that module's immediate children
        for (Iterable<? extends Generator<?>> ancestor : stack) {
            for (Generator<?> child : ancestor) {
                if (type.isInstance(child) && argument.equals(child.statement().argument())) {
                    LOG.info("Found matching {}", child);
                    return type.cast(child);
                }
            }
        }

        return null;
//        throw new IllegalStateException("Could not find " + argument + " in " + stack);
    }

    @Override
    AbstractTypeObjectGenerator<?> resolveLeafref(final PathExpression path) {
        LOG.info("Resolving path {}", path);
        verify(inferenceStack.isEmpty(), "Unexpected data tree state %s", inferenceStack);
        try {
            // Populate inferenceStack with a grouping + data tree equivalent of current stack's state.
            final Iterator<Iterable<? extends Generator<?>>> it = stack.descendingIterator();
            // Skip first item, as it points to our children
            verify(it.hasNext(), "Unexpected empty stack");
            it.next();

            while (it.hasNext()) {
                final Iterable<? extends Generator<?>> item = it.next();
                verify(item instanceof Generator, "Unexpected stack item %s", item);
                ((Generator<?>) item).pushToInference(inferenceStack);
            }

            return inferenceStack.inInstantiatedContext() ? strictResolvePath(path) : lenientResolveLeafref(path);
        } finally {
            inferenceStack.clear();
        }
    }

    private @NonNull AbstractTypeObjectGenerator<?> strictResolvePath(final @NonNull PathExpression path) {
        try {
            inferenceStack.resolvePathExpression(path);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to resolve " + path, e);
        }
        return mapToGenerator();
    }

    private @Nullable AbstractTypeObjectGenerator<?> lenientResolveLeafref(final @NonNull PathExpression path) {
        try {
            inferenceStack.resolvePathExpression(path);
        } catch (IllegalArgumentException e) {
            LOG.trace("Ignoring unresolved path {}", path, e);
            return null;
        }
        return mapToGenerator();
    }

    private @NonNull AbstractTypeObjectGenerator<?> mapToGenerator() {
        // FIXME: map dataTreeStack back to generators
        return null;
    }

    private void forEachCompositeGenerator(final BiConsumer<AbstractCompositeGenerator<?>, GeneratorContext> action) {
        stack.push(children);
        forEachCompositeGenerator(children, action);
        stack.pop();
    }

    private void forEachCompositeGenerator(final Iterable<? extends Generator<?>> parent,
            final BiConsumer<AbstractCompositeGenerator<?>, GeneratorContext> action) {
        for (Generator<?> child : parent) {
            if (child instanceof AbstractCompositeGenerator) {
                LOG.info("Visiting composite {}", child);
                final AbstractCompositeGenerator<?> composite = (AbstractCompositeGenerator<?>) child;
                stack.push(composite);
                action.accept(composite, this);
                forEachCompositeGenerator(composite, action);
                stack.pop();
            }
        }
    }

    // Note: unlike forEachCompositeGenerator(), this method does not push matching child to stack
    private void forEachTypeObjectGenerator(final Iterable<? extends Generator<?>> parent,
            final BiConsumer<AbstractTypeObjectGenerator<?>, GeneratorContext> action) {
        stack.push(parent);
        for (Generator<?> child : parent) {
            if (child instanceof AbstractTypeObjectGenerator) {
                LOG.info("Visiting TypeObject {}", child);
                action.accept((AbstractTypeObjectGenerator<?>) child, this);
            } else if (child instanceof AbstractCompositeGenerator) {
                LOG.info("Visiting composite {}", child);
                forEachTypeObjectGenerator(child, action);
            }
        }
        stack.pop();
    }

    private static void recursiveLinkParent(final AbstractCompositeGenerator<?> generator,
            final AbstractCompositeGenerator<?> parent) {
        generator.linkParent(parent);
        for (Generator<?> child : generator) {
            if (child instanceof AbstractCompositeGenerator) {
                recursiveLinkParent((AbstractCompositeGenerator<?>) child, generator);
            }
        }
    }
}
