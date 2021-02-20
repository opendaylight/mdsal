/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
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
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
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

    private final @NonNull CollisionDomain domain = new CollisionDomain();
    private final List<Generator> children;

    private List<AugmentGenerator> augments = List.of();
    private List<GroupingGenerator> groupings;

    AbstractCompositeGenerator(final T statement) {
        super(statement);
        children = createChildren(statement, true);
    }

    AbstractCompositeGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        children = createChildren(statement, false);
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

    void linkCompositeDependencies(final GeneratorContext context) {
        groupings = statement().streamEffectiveSubstatements(UsesEffectiveStatement.class)
            .map(uses -> context.resolveTreeScoped(GroupingGenerator.class, uses.argument()))
            .collect(Collectors.toUnmodifiableList());
    }

    final void addAugment(final AugmentGenerator augment) {
        if (augments.isEmpty()) {
            augments = new ArrayList<>(2);
        }
        augments.add(requireNonNull(augment));
    }

    @Override
    final @Nullable AbstractExplicitGenerator<?> findSchemaTreeGenerator(final QName qname) {
        final AbstractExplicitGenerator<?> found = super.findSchemaTreeGenerator(qname);
        if (found != null) {
            return found;
        }
        for (GroupingGenerator grouping : groupings) {
            final AbstractExplicitGenerator<?> gen = grouping.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return gen;
            }
        }
        for (AugmentGenerator augment : augments) {
            final AbstractExplicitGenerator<?> gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return gen;
            }
        }
        return null;
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory) {
        return getType(builderFactory);
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
            builder.addImplementsType(grp.getType(builderFactory));
        }
        return groupings.size();
    }

    final void addGetterMethods(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        for (Generator child : this) {
            // Only process explicit generators here
            if (child instanceof AbstractExplicitGenerator) {
                ((AbstractExplicitGenerator<?>) child).addAsGetterMethod(builder, builderFactory);
            }
        }
    }

    private List<Generator> createChildren(final EffectiveStatement<?, ?> statement, final boolean includeAugments) {
        final List<Generator> tmp = new ArrayList<>();

        for (EffectiveStatement<?, ?> stmt : statement.effectiveSubstatements()) {
            if (stmt instanceof ActionEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new ActionGenerator((ActionEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof AnydataEffectiveStatement) {
                if (!isAddedByUses(stmt)) {
                    tmp.add(new OpaqueObjectGenerator<>((AnydataEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof AnyxmlEffectiveStatement) {
                if (!isAddedByUses(stmt)) {
                    tmp.add(new OpaqueObjectGenerator<>((AnyxmlEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof CaseEffectiveStatement) {
                tmp.add(new CaseGenerator((CaseEffectiveStatement) stmt, this));
            } else if (stmt instanceof ChoiceEffectiveStatement) {
                if (!isAddedByUses(stmt)) {
                    tmp.add(new ChoiceGenerator((ChoiceEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof ContainerEffectiveStatement) {
                if (!isAddedByUses(stmt) && !isAugmenting(stmt)) {
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
                if (!isAddedByUses(stmt) && !isAugmenting(stmt)) {
                    final ListGenerator listGen = new ListGenerator((ListEffectiveStatement) stmt, this);
                    tmp.add(listGen);

                    if (listGen.producesType()) {
                        stmt.findFirstEffectiveSubstatement(KeyEffectiveStatement.class).ifPresent(
                            key -> tmp.add(new KeyGenerator(key, this, listGen)));
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
            } else if (stmt instanceof AugmentEffectiveStatement && includeAugments) {
                tmp.add(new AugmentGenerator((AugmentEffectiveStatement) stmt, this));
            } else {
                LOG.trace("Ignoring statement {}", stmt);
                continue;
            }
        }

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

    private static boolean isAddedByUses(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof AddedByUsesAware && ((AddedByUsesAware) stmt).isAddedByUses();
    }

    private static boolean isAugmenting(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof CopyableNode && ((CopyableNode) stmt).isAugmenting();
    }
}
