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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
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

/**
 * A composite generator. Composite generators may contain additional children, which end up being mapped into
 * the naming hierarchy 'under' the composite generator. To support this use case, each composite has a Java package
 * name assigned.
 */
abstract class AbstractCompositeGenerator<T extends EffectiveStatement<?, ?>> extends AbstractExplicitGenerator<T> {
    private final CollisionDomain domain = new CollisionDomain();

    private List<GroupingGenerator> usedGroupings;

    AbstractCompositeGenerator(final T statement) {
        super(statement);
        createChildren(statement, true);
    }

    AbstractCompositeGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        createChildren(statement, false);
    }

    private void createChildren(final EffectiveStatement<?, ?> statement, final boolean includeAugments) {
        final List<Generator> tmp = new ArrayList<>();
        int augmentIndex = 1;

        for (EffectiveStatement<?, ?> stmt : statement.effectiveSubstatements()) {
            final Generator child;
            if (stmt instanceof ActionEffectiveStatement) {
                child = new ActionGenerator((ActionEffectiveStatement) stmt, this);
            } else if (stmt instanceof AnydataEffectiveStatement) {
                child = new OpaqueObjectGenerator<>((AnydataEffectiveStatement) stmt, this);
            } else if (stmt instanceof AnyxmlEffectiveStatement) {
                child = new OpaqueObjectGenerator<>((AnyxmlEffectiveStatement) stmt, this);
            } else if (stmt instanceof CaseEffectiveStatement) {
                child = new CaseGenerator((CaseEffectiveStatement) stmt, this);
            } else if (stmt instanceof ChoiceEffectiveStatement) {
                child = new ChoiceGenerator((ChoiceEffectiveStatement) stmt, this);
            } else if (stmt instanceof ContainerEffectiveStatement) {
                child = new ContainerGenerator((ContainerEffectiveStatement) stmt, this);
            } else if (stmt instanceof GroupingEffectiveStatement) {
                child = new GroupingGenerator((GroupingEffectiveStatement) stmt, this);
            } else if (stmt instanceof IdentityEffectiveStatement) {
                child = new IdentityGenerator((IdentityEffectiveStatement) stmt, this);
            } else if (stmt instanceof InputEffectiveStatement) {
                child = new InputGenerator((InputEffectiveStatement) stmt, this);
            } else if (stmt instanceof LeafEffectiveStatement) {
                child = new LeafGenerator((LeafEffectiveStatement) stmt, this);
            } else if (stmt instanceof LeafListEffectiveStatement) {
                child = new LeafListGenerator((LeafListEffectiveStatement) stmt, this);
            } else if (stmt instanceof ListEffectiveStatement) {
                child = new ListGenerator((ListEffectiveStatement) stmt, this);
            } else if (stmt instanceof NotificationEffectiveStatement) {
                child = new NotificationGenerator((NotificationEffectiveStatement) stmt, this);
            } else if (stmt instanceof OutputEffectiveStatement) {
                child = new OutputGenerator((OutputEffectiveStatement) stmt, this);
            } else if (stmt instanceof RpcEffectiveStatement) {
                child = new RpcGenerator((RpcEffectiveStatement) stmt, this);
            } else if (stmt instanceof TypedefEffectiveStatement) {
                child = new TypedefGenerator((TypedefEffectiveStatement) stmt, this);
            } else if (stmt instanceof AugmentEffectiveStatement && includeAugments) {
                child = new AugmentGenerator((AugmentEffectiveStatement) stmt, this, augmentIndex++);
            } else {
                continue;
            }

            tmp.add(child);
        }

        addChildren(tmp);
    }

    final CollisionDomain domain() {
        return domain;
    }

    void linkCompositeDependencies(final GeneratorContext context) {
        usedGroupings = statement().streamEffectiveSubstatements(UsesEffectiveStatement.class)
            .map(uses -> context.resolveTreeScoped(GroupingGenerator.class, uses.argument()))
            .collect(Collectors.toUnmodifiableList());
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
        for (GroupingGenerator grp : usedGroupings) {
            builder.addImplementsType(grp.createType(builderFactory));
        }
        return usedGroupings.size();
    }

    final void setChildPackageNames() {
        final String myPackage = javaPackage();

        // FIXME: this does not deal with conflicts yet
        for (Generator child : this) {
            if (child instanceof AbstractCompositeGenerator) {
                final AbstractCompositeGenerator<?> composite = (AbstractCompositeGenerator<?>) child;
                if (!composite.isEmpty()) {
                    final String pkg = myPackage + '.' + child.preferredSubpackage();
                    composite.setJavaPackage(BindingMapping.normalizePackageName(pkg));
                    composite.setChildPackageNames();
                }
            }
        }
    }

    final void setDescendantSimpleNames() {
        // This needs to be ordered deepest-first, as we cannot complete the top-level (i.e. immediate children of
        // modules) as that involves 'augment' statements and those point to arbitrary descendants
        for (Generator child : this) {
            if (child instanceof AbstractCompositeGenerator) {
                ((AbstractCompositeGenerator<?>) child).setDescendantSimpleNames();
            }
        }

        // All grandchildren (and their children) are assigned, now assign children
        setChildSimpleNames();
    }

    final void setChildSimpleNames() {
        /*
         * Naming child nodes is tricky.
         *
         * We map multiple YANG namespaces (see YangStatementNamespace) onto a single Java namespace
         * (package/class names), hence we can have legal conflicts on same localName.
         *
         * Furthermore not all localNames are valid Java class/package identifiers, hence even non-equal localNames can
         * conflict on their mapping.
         *
         * Final complication is that we allow user to control preferred name, or we generate one, and we try to come up
         * with nice names like 'foo-bar' becoming FooBar and similar.
         *
         * In all cases we want to end up with cutest possible names while also never creating duplicates. For that we
         * start with each child telling us their preferred name and we collect name->child mapping.
         */
        final Multimap<String, Generator> toAssign = ArrayListMultimap.create();
        for (Generator child : this) {
            if (child.producesType()) {
                toAssign.put(child.preferredName(), child);
            }
        }

        // FIXME: do we need this map?
        final Map<String, Generator> assignments = Maps.newHashMapWithExpectedSize(toAssign.size());
        assignNonConflicting(toAssign, assignments);
        if (!toAssign.isEmpty()) {
            assignConflicting(toAssign, assignments);
            verify(toAssign.isEmpty(), "Failed to resolve %s", toAssign);
        }
    }

    // Deal with names which do not create a conflict. This is very simple and also very effective, we rarely run into
    // conflicts.
    private static void assignNonConflicting(final Multimap<String, Generator> toAssign,
            final Map<String, Generator> assignments) {
        final var it = toAssign.asMap().entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, Collection<Generator>> entry = it.next();
            final Collection<Generator> assignees = entry.getValue();
            if (assignees.size() == 1) {
                verify(assignees instanceof List, "Unexpected assignees %s", assignees);
                assign(assignments, ((List<Generator>) assignees).get(0), entry.getKey());
                it.remove();
            }
        }
    }

    // Deal with name conflicts, which is problematic as we need to locate the source of each conflict
    private static void assignConflicting(final Multimap<String, Generator> toAssign,
            final Map<String, Generator> assignments) {
        // Strategy one: assign a unique suffix to each name based on the YANG namespace it comes from. For that we need
        //               an index first...
        final Table<StatementNamespace, String, List<Generator>> nsNameAssignee =
            HashBasedTable.create();
        for (Entry<String, Collection<Generator>> entry : toAssign.asMap().entrySet()) {
            final var namespaceToChild = nsNameAssignee.column(entry.getKey());
            for (Generator child : entry.getValue()) {
                namespaceToChild.computeIfAbsent(child.namespace(), x -> new ArrayList<>(1)).add(child);
            }
        }

        // ... then walk the index, either assigning the name or remembering to further process it. At the end of the
        // process we have completely taken ownership of 'toAssign' and clear it
        final Multimap<String, Generator> nsNameConflicts = ArrayListMultimap.create();
        for (var cell : nsNameAssignee.cellSet()) {
            final String assignedName = verifyNotNull(cell.getRowKey()).appendSuffix(cell.getColumnKey());
            final List<Generator> assignees = verifyNotNull(cell.getValue());
            if (assignees.size() == 1) {
                assign(assignments, assignees.get(0), assignedName);
            } else {
                nsNameConflicts.putAll(assignedName, assignees);
            }
        }
        toAssign.clear();

        if (!nsNameConflicts.isEmpty()) {
            assignNamespaceConflicting(nsNameConflicts, assignments);
            verify(toAssign.isEmpty(), "Failed to resolve %s", nsNameConflicts);
        }
    }

    // At this point we have taken YANG namespaces out of the picture, yet we still have some conflicts.
    // The only reason for this can be that localName -> preferredName mapping is losing the difference.
    private static void assignNamespaceConflicting(final Multimap<String, Generator> toAssign,
            final Map<String, Generator> assignments) {
        // FIXME: MDSAL-503: implement this
        //        The algorithm needs to essentially fall back to using escape-based translation scheme, where each
        //        localName results in a unique name, while not conflicting with any possible preferredName. The exact
        //        mechanics for that are TBD, but note the keys we get in toAssign already have a namespace-specific
        //        suffix. A requirement for that mapping is that it must not rely on definition order.
        //
        //        But there is another possible step: since we are assigning 14 different statements into the default
        //        namespace (which did not add a suffix), we can try to assign a statement-derived suffix. To make
        //        things easier, we use two-characters: AC, AD, AU, AX, CA, CH, CO, IP, LE, LI, LL, NO, OP, RP.
    }

    private static void assign(final Map<String, Generator> assignments, final Generator assignee,
            final @NonNull String simpleName) {
        assignee.setAssignedName(simpleName);
        assignments.put(simpleName, assignee);
    }
}
