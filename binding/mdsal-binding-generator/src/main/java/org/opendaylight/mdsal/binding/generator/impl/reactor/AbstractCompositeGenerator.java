/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite generator. Composite generators may contain additional children, which end up being mapped into
 * the naming hierarchy 'under' the composite generator. To support this use case, each composite has a Java package
 * name assigned.
 */
abstract class AbstractCompositeGenerator<T extends EffectiveStatement<?, ?>> extends AbstractExplicitGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCompositeGenerator.class);

    private final @NonNull CollisionDomain domain = new CollisionDomain(this);
    private final List<Generator> children;

    private List<AbstractAugmentGenerator> augments = List.of();
    private List<GroupingGenerator> groupings;

    AbstractCompositeGenerator(final T statement) {
        super(statement);
        children = createChildren(statement);
    }

    AbstractCompositeGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        children = createChildren(statement);
    }

    @Override
    public final Iterator<Generator> iterator() {
        return children.iterator();
    }

    @Override
    final boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    final @Nullable Generator findGenerator(final EffectiveStatement<?, ?> stmt) {
        for (Generator gen : children) {
            if (gen instanceof AbstractExplicitGenerator && ((AbstractExplicitGenerator<?>) gen).statement() == stmt) {
                return gen;
            }
        }
        return null;
    }

    final @NonNull CollisionDomain domain() {
        return domain;
    }

    final void linkUsesDependencies(final GeneratorContext context) {
        // We are establishing two linkages here:
        // - we are resolving 'uses' statements to their corresponding 'grouping' definitions
        // - we propagate those groupings as anchors to any augment statements
        final List<GroupingGenerator> tmp = new ArrayList<>();
        for (EffectiveStatement<?, ?> stmt : statement().effectiveSubstatements()) {
            if (stmt instanceof UsesEffectiveStatement) {
                final UsesEffectiveStatement uses = (UsesEffectiveStatement) stmt;
                final GroupingGenerator grouping = context.resolveTreeScoped(GroupingGenerator.class, uses.argument());
                tmp.add(grouping);

                for (Generator gen : this) {
                    if (gen instanceof UsesAugmentGenerator) {
                        ((UsesAugmentGenerator) gen).linkGroupingDependency(uses, grouping);
                    }
                }
            }
        }
        groupings = List.copyOf(tmp);
    }

    final void addAugment(final AbstractAugmentGenerator augment) {
        if (augments.isEmpty()) {
            augments = new ArrayList<>(2);
        }
        augments.add(requireNonNull(augment));
    }

    @Override
    final AbstractCompositeGenerator<?> getOriginal() {
        return (AbstractCompositeGenerator<?>) super.getOriginal();
    }

    final @NonNull AbstractExplicitGenerator<?> getOriginalChild(final QName childQName) {
        // First try groupings/augments ...
        final AbstractExplicitGenerator<?> found = findInferredGenerator(childQName);
        if (found != null) {
            return found;
        }

        // ... no luck, we really need to start looking at our origin
        final AbstractExplicitGenerator<?> prev = verifyNotNull(previous(),
            "Failed to find %s in scope of %s", childQName, this);

        final QName prevQName = childQName.bindTo(prev.getQName().getModule());
        return verifyNotNull(prev.findSchemaTreeGenerator(prevQName),
            "Failed to find child %s (proxy for %s) in %s", prevQName, childQName, prev).getOriginal();
    }

    @Override
    final @Nullable AbstractExplicitGenerator<?> findSchemaTreeGenerator(final QName qname) {
        final AbstractExplicitGenerator<?> found = super.findSchemaTreeGenerator(qname);
        return found != null ? found : findInferredGenerator(qname);
    }

    private @Nullable AbstractExplicitGenerator<?> findInferredGenerator(final QName qname) {
        // First search our local groupings ...
        for (GroupingGenerator grouping : groupings) {
            final AbstractExplicitGenerator<?> gen = grouping.findSchemaTreeGenerator(
                qname.bindTo(grouping.statement().argument().getModule()));
            if (gen != null) {
                return gen;
            }
        }
        // ... next try local augments, which may have groupings themselves
        for (AbstractAugmentGenerator augment : augments) {
            final AbstractExplicitGenerator<?> gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return gen;
            }
        }
        return null;
    }

    /**
     * Update the specified builder to implement interfaces generated for the {@code grouping} statements this generator
     * is using.
     *
     * @param builder Target builder
     * @param builderFactory factory for creating {@link TypeBuilder}s
     * @return The number of groupings this type uses.
     */
    final int addUsesInterfaces(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        for (GroupingGenerator grp : groupings) {
            builder.addImplementsType(grp.getGeneratedType(builderFactory));
        }
        return groupings.size();
    }

    static final void addAugmentable(final GeneratedTypeBuilder builder) {
        builder.addImplementsType(BindingTypes.augmentable(builder));
    }

    final void addGetterMethods(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        for (Generator child : this) {
            // Only process explicit generators here
            if (child instanceof AbstractExplicitGenerator) {
                ((AbstractExplicitGenerator<?>) child).addAsGetterMethod(builder, builderFactory);
            }

            final GeneratedType enclosedType = child.enclosedType(builderFactory);
            if (enclosedType instanceof GeneratedTransferObject) {
                builder.addEnclosingTransferObject((GeneratedTransferObject) enclosedType);
            } else if (enclosedType instanceof Enumeration) {
                builder.addEnumeration((Enumeration) enclosedType);
            } else {
                verify(enclosedType == null, "Unhandled enclosed type %s in %s", enclosedType, child);
            }
        }
    }

    private List<Generator> createChildren(final EffectiveStatement<?, ?> statement) {
        final List<Generator> tmp = new ArrayList<>();
        final List<AbstractAugmentGenerator> tmpAug = new ArrayList<>();

        for (EffectiveStatement<?, ?> stmt : statement.effectiveSubstatements()) {
            if (stmt instanceof ActionEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new ActionGenerator((ActionEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof AnydataEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new OpaqueObjectGenerator<>((AnydataEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof AnyxmlEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new OpaqueObjectGenerator<>((AnyxmlEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof CaseEffectiveStatement) {
                tmp.add(new CaseGenerator((CaseEffectiveStatement) stmt, this));
            } else if (stmt instanceof ChoiceEffectiveStatement) {
                // FIXME: use isOriginalDeclaration() ?
                if (!isAddedByUses(stmt)) {
                    tmp.add(new ChoiceGenerator((ChoiceEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof ContainerEffectiveStatement) {
                if (isOriginalDeclaration(stmt)) {
                    tmp.add(new ContainerGenerator((ContainerEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof GroupingEffectiveStatement) {
                tmp.add(new GroupingGenerator((GroupingEffectiveStatement) stmt, this));
            } else if (stmt instanceof IdentityEffectiveStatement) {
                tmp.add(new IdentityGenerator((IdentityEffectiveStatement) stmt, this));
            } else if (stmt instanceof InputEffectiveStatement) {
                // FIXME: do not generate legacy RPC layout
                tmp.add(this instanceof RpcGenerator ? new RpcContainerGenerator((InputEffectiveStatement) stmt, this)
                    : new OperationContainerGenerator((InputEffectiveStatement) stmt, this));
            } else if (stmt instanceof LeafEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new LeafGenerator((LeafEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof LeafListEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new LeafListGenerator((LeafListEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof ListEffectiveStatement) {
                if (isOriginalDeclaration(stmt)) {
                    final ListGenerator listGen = new ListGenerator((ListEffectiveStatement) stmt, this);
                    tmp.add(listGen);

                    final KeyGenerator keyGen = listGen.keyGenerator();
                    if (keyGen != null) {
                        tmp.add(keyGen);
                    }
                }
            } else if (stmt instanceof NotificationEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new NotificationGenerator((NotificationEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof OutputEffectiveStatement) {
                // FIXME: do not generate legacy RPC layout
                tmp.add(this instanceof RpcGenerator ? new RpcContainerGenerator((OutputEffectiveStatement) stmt, this)
                    : new OperationContainerGenerator((OutputEffectiveStatement) stmt, this));
            } else if (stmt instanceof RpcEffectiveStatement) {
                tmp.add(new RpcGenerator((RpcEffectiveStatement) stmt, this));
            } else if (stmt instanceof TypedefEffectiveStatement) {
                tmp.add(new TypedefGenerator((TypedefEffectiveStatement) stmt, this));
            } else if (stmt instanceof AugmentEffectiveStatement) {
                if (this instanceof ModuleGenerator) {
                    tmpAug.add(new ModuleAugmentGenerator((AugmentEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof UsesEffectiveStatement) {
                final UsesEffectiveStatement uses = (UsesEffectiveStatement) stmt;
                for (EffectiveStatement<?, ?> usesSub : uses.effectiveSubstatements()) {
                    if (usesSub instanceof AugmentEffectiveStatement) {
                        tmpAug.add(new UsesAugmentGenerator((AugmentEffectiveStatement) usesSub, this, uses));
                    }
                }
            } else if (stmt instanceof YangDataEffectiveStatement) {



            } else {
                LOG.trace("Ignoring statement {}", stmt);
                continue;
            }
        }

        // Sort augments and add them last. This ensures child iteration order always reflects potential
        // interdependencies, hence we do not need to worry about them.
        tmpAug.sort(AbstractAugmentGenerator.COMPARATOR);
        tmp.addAll(tmpAug);

        // Compatibility FooService and FooListener interfaces, only generated for modules.
        if (this instanceof ModuleGenerator) {
            final ModuleGenerator moduleGen = (ModuleGenerator) this;

            final List<NotificationGenerator> notifs = tmp.stream()
                .filter(NotificationGenerator.class::isInstance)
                .map(NotificationGenerator.class::cast)
                .collect(Collectors.toUnmodifiableList());
            if (!notifs.isEmpty()) {
                tmp.add(new NotificationServiceGenerator(moduleGen, notifs));
            }

            final List<RpcGenerator> rpcs = tmp.stream()
                .filter(RpcGenerator.class::isInstance)
                .map(RpcGenerator.class::cast)
                .collect(Collectors.toUnmodifiableList());
            if (!rpcs.isEmpty()) {
                tmp.add(new RpcServiceGenerator(moduleGen, rpcs));
            }
        }

        return List.copyOf(tmp);
    }

    // Utility equivalent of (!isAddedByUses(stmt) && !isAugmenting(stmt)). Takes advantage of relationship between
    // CopyableNode and AddedByUsesAware
    private static boolean isOriginalDeclaration(final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof AddedByUsesAware) {
            if (((AddedByUsesAware) stmt).isAddedByUses()
                || stmt instanceof CopyableNode && ((CopyableNode) stmt).isAugmenting()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAddedByUses(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof AddedByUsesAware && ((AddedByUsesAware) stmt).isAddedByUses();
    }

    private static boolean isAugmenting(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof CopyableNode && ((CopyableNode) stmt).isAugmenting();
    }
}
