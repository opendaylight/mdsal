/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
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
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
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

    private final Deque<Iterable<? extends Generator>> stack = new ArrayDeque<>();
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
     *   <li>link the {@code typedef} inheritance hierarchy by visiting all {@link TypedefGenerator}s and memoizing the
     *       {@code type} lookup</li>
     *   <li>link the {@code identity} inheritance hierarchy by visiting all {@link IdentityGenerator}s and memoizing
     *       the {@code base} lookup</li>
     *   <li>link the {@code type} statements and resolve type restriction hierarchy, determining the set of Java
             classes required for Java equivalent of effective YANG type definitions</li>
     *   <li>bind {@code leafref} and {@code identityref} references to their Java class roots</li>
     *   <li>resolve class inheritance along {@code uses} statements</li>
     *   <li>resolve {@link ChoiceIn}/{@link ChildOf} hierarchy</li>
     *   <li>assign Java package names and {@link JavaTypeName}s to all generated classes</li>
     *   <li>create {@link Type} instances</li>
     * </ol>
     *
     * @param builderFactory factory for creating {@link TypeBuilder}s for resulting types
     * @return Resolved generators
     * @throws IllegalStateException if the reactor has failed execution
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    public @NonNull Map<QNameModule, ModuleGenerator> execute(final TypeBuilderFactory builderFactory) {
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
         * Step one: link typedef statements, so that typedef's 'type' axis is fully established
         * Step two: link all identity statements, so that identity's 'base' axis is fully established\
         * Step three: link all type statements, so that leafs and leaf-lists have restrictions established
         *
         * Since our implementation class hierarchy captures all four statements involved in a common superclass, we
         * can perform this in a single pass.
         */
        forEachDependentGenerator(children, AbstractDependentGenerator::linkDependencies);

        // Step four: resolve all 'type leafref' and 'type identityref' statements, so they point to their
        //             corresponding Java type representation.
        forEachTypeObjectGenerator(children, AbstractTypeObjectGenerator::bindTypeDefinition);

        // Step five: walk all composite generators and resolve 'uses' statements to the corresponding grouping node,
        //            establishing implied inheritance. We also use this pass to link augmentation targets, as that is
        //            required their simple names.
        forEachCompositeGenerator(children, AbstractCompositeGenerator::linkCompositeDependencies);

        // Step six: walk all composite generators and link ChildOf/ChoiceIn relationships with parents. We have taken
        //           care of this step during tree construction, hence this now a no-op.

        /*
         * Step seven: assign java packages and JavaTypeNames
         *
         * This is a really tricky part, as we have large number of factors to consider:
         * - we are mapping grouping, typedef, identity and schema tree namespaces into Fully Qualified Class Names,
         *   i.e. four namespaces into one
         * - our source of class naming are YANG identifiers, which allow characters not allowed by Java
         * - we generate class names as well as nested package hierarchy
         * - we want to generate names which look like Java as much as possible
         * - we need to always have an (arbitrarily-ugly) fail-safe name
         *
         * To deal with all that, we split this problem into multiple manageable chunks.
         *
         * The first chunk is here: we walk all generators and ask them to do two things:
         * - instantiate their CollisionMembers and link them to appropriate CollisionDomains
         * - return their collision domain
         */
        final List<CollisionDomain> domains = new ArrayList<>();
        collectCollisionDomains(domains, children);

        // FIXME: rework assignment logic










//        * The first chunk is here: we walk all the modules and assign simple names (for use with classes) and package
//        * names (to host them). Since all members will generate at least one named Java entity -- a class or a method,
//        * we start off with assigning Java Identifier for use as the class name. We then assign package names, where
//        * the assigned simple name serves as the primary source of truth -- hence we are making sure we have patterns
//        * which hint at which container-like class is the source of the package name.


        // First we need to process each module's children's descendants, so that 'augment' statements can freely
        // reference their names.
        for (ModuleGenerator module : children) {
            for (Generator child : module) {
                if (child instanceof AbstractCompositeGenerator<?>) {
                    ((AbstractCompositeGenerator<?>) child).setDescendantSimpleNames();
                }
            }
        }

        // Now we can proceed with assigning all modules' direct children, as any augmentation references should be
        // successfully resolved. As we complete each module's simple name assignment also trigger package name
        // assignment
        for (ModuleGenerator module : children) {
            // TODO: can this create conflicts somewhere?
            module.setAssignedName(module.preferredName());

            // Real complexity is in the implementation method. It is expected to deal with the details of resolving
            // naming conflicts at each tree level, so that each sibling has a unique name.
            module.setChildSimpleNames();

            // Once we have simple names, assign package names. Note that modules have their package names assigned
            // at construction, as they are root invariants.
            module.setChildPackageNames();
        }

        // Step seven: generate actual Types
        //
        // We have now properly cross-linked all generators and have assigned their naming roots, so from this point
        // it looks as though we are performing a simple recursive execution. In reality, though, the actual path taken
        // through generators is dictated by us as well as generator linkage.
        for (ModuleGenerator module : children) {
            module.createType(builderFactory);
        }

        state = State.FINISHED;
        return generators;
    }


    private void collectCollisionDomains(final List<CollisionDomain> result,
            final Iterable<? extends Generator> generators) {
        for (Generator gen : generators) {
            gen.member();
            collectCollisionDomains(result, gen);
            if (gen instanceof AbstractCompositeGenerator) {
                result.add(((AbstractCompositeGenerator<?>) gen).domain());
            }
        }
    }

    @Override
    <E extends SchemaTreeEffectiveStatement<?>> AbstractExplicitGenerator<E> resolveSchemaNode(
            final SchemaNodeIdentifier path) {
        verify(path instanceof SchemaNodeIdentifier.Absolute, "Unexpected path %s", path);

        AbstractExplicitGenerator<?> current = generators.get(path.firstNodeIdentifier().getModule());
        checkState(current != null, "Cannot find module for %s", path);

        for (QName qname : path.getNodeIdentifiers()) {
            current = resolveSchemaNode(current, qname);
        }

        return (AbstractExplicitGenerator<E>) current;
    }

    private static AbstractExplicitGenerator<?> resolveSchemaNode(final Generator parent, final QName qname) {
        for (Generator child : parent) {
            if (child instanceof AbstractExplicitGenerator) {
                final AbstractExplicitGenerator<?> gen = (AbstractExplicitGenerator<?>) child;
                final EffectiveStatement<?, ?> stmt = gen.statement();
                if (stmt instanceof SchemaTreeEffectiveStatement && qname.equals(stmt.argument())) {
                    return gen;
                }
            }
        }

        throw new IllegalStateException("Failed to find " + qname);
    }

    @Override
    <E extends EffectiveStatement<QName, ?>, G extends AbstractExplicitGenerator<E>> G resolveTreeScoped(
            final Class<G> type, final QName argument) {
        LOG.trace("Searching for tree-scoped argument {} at {}", argument, stack);

        // Check if the requested QName matches current module, if it does search the stack
        final Iterable<? extends Generator> last = stack.getLast();
        verify(last instanceof ModuleGenerator, "Unexpected last stack item %s", last);

        if (argument.getModule().equals(((ModuleGenerator) last).statement().localQNameModule())) {
            for (Iterable<? extends Generator> ancestor : stack) {
                for (Generator child : ancestor) {
                    if (type.isInstance(child)) {
                        final G cast = type.cast(child);
                        if (argument.equals(cast.statement().argument())) {
                            LOG.trace("Found matching {}", child);
                            return cast;
                        }
                    }
                }
            }
        } else {
            final ModuleGenerator module = generators.get(argument.getModule());
            if (module != null) {
                for (Generator child : module) {
                    if (type.isInstance(child)) {
                        final G cast = type.cast(child);
                        if (argument.equals(cast.statement().argument())) {
                            LOG.trace("Found matching {}", child);
                            return cast;
                        }
                    }
                }
            }
        }

        throw new IllegalStateException("Could not find " + argument + " in " + stack);
    }

    @Override
    IdentityGenerator resolveIdentity(final QName name) {
        final ModuleGenerator module = generators.get(name.getModule());
        if (module != null) {
            for (Generator gen : module) {
                if (gen instanceof IdentityGenerator) {
                    final IdentityGenerator idgen = (IdentityGenerator) gen;
                    if (name.equals(idgen.statement().argument())) {
                        return idgen;
                    }
                }
            }
        }
        throw new IllegalStateException("Failed to find identity " + name);
    }

    @Override
    AbstractTypeObjectGenerator<?> resolveLeafref(final PathExpression path) {
        LOG.info("Resolving path {}", path);
        verify(inferenceStack.isEmpty(), "Unexpected data tree state %s", inferenceStack);
        try {
            // Populate inferenceStack with a grouping + data tree equivalent of current stack's state.
            final Iterator<Iterable<? extends Generator>> it = stack.descendingIterator();
            // Skip first item, as it points to our children
            verify(it.hasNext(), "Unexpected empty stack");
            it.next();

            while (it.hasNext()) {
                final Iterable<? extends Generator> item = it.next();
                verify(item instanceof Generator, "Unexpected stack item %s", item);
                ((Generator) item).pushToInference(inferenceStack);
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
            LOG.info("Ignoring unresolved path {}", path, e);
            return null;
        }
        return mapToGenerator();
    }

    // Map inferenceStack's current statement to the corresponding generator
    private @NonNull AbstractTypeObjectGenerator<?> mapToGenerator() {
        final ModuleEffectiveStatement module = inferenceStack.currentModule();
        final ModuleGenerator mod = verifyNotNull(generators.get(module.localQNameModule()),
            "Cannot match module %s", module);
        verify(module == mod.statement(), "Module mismatch on %s", module);

        Generator current = mod;
        for (EffectiveStatement<?, ?> stmt : inferenceStack.toInference().statementPath()) {
            current = verifyNotNull(current.findGenerator(stmt), "Cannot match statement %s in %s", stmt, current);
        }
        verify(current instanceof AbstractTypeObjectGenerator, "Unexpected generator %s", current);
        return (AbstractTypeObjectGenerator<?>) current;
    }

    // Note: unlike other methods, this method pushes matching child to the stack
    private void forEachCompositeGenerator(final Iterable<? extends Generator> parent,
            final BiConsumer<AbstractCompositeGenerator<?>, GeneratorContext> action) {
        for (Generator child : parent) {
            if (child instanceof AbstractCompositeGenerator) {
                LOG.trace("Visiting composite {}", child);
                final AbstractCompositeGenerator<?> composite = (AbstractCompositeGenerator<?>) child;
                stack.push(composite);
                action.accept(composite, this);
                forEachCompositeGenerator(composite, action);
                stack.pop();
            }
        }
    }

    private void forEachDependentGenerator(final Iterable<? extends Generator> parent,
            final BiConsumer<AbstractDependentGenerator<?>, GeneratorContext> action) {
        for (Generator child : parent) {
            if (child instanceof AbstractDependentGenerator) {
                action.accept((AbstractDependentGenerator<?>) child, this);
            } else if (child instanceof AbstractCompositeGenerator) {
                stack.push(child);
                forEachDependentGenerator(child, action);
                stack.pop();
            }
        }
    }

    private void forEachTypeObjectGenerator(final Iterable<? extends Generator> parent,
            final BiConsumer<AbstractTypeObjectGenerator<?>, GeneratorContext> action) {
        for (Generator child : parent) {
            if (child instanceof AbstractTypeObjectGenerator) {
                action.accept((AbstractTypeObjectGenerator<?>) child, this);
            } else if (child instanceof AbstractCompositeGenerator) {
                stack.push(child);
                forEachTypeObjectGenerator(child, action);
                stack.pop();
            }
        }
    }
}
