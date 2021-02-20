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
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
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

/**
 * A composite generator. Composite generators may contain additional children, which end up being mapped into
 * the naming hierarchy 'under' the composite generator. To support this use case, each composite has a Java package
 * name assigned.
 */
public abstract class AbstractCompositeGenerator<T extends EffectiveStatement<?, ?>> extends Generator<T>
        implements Iterable<Generator<?>> {
    private final List<Generator<?>> children;
    private final List<UsesEffectiveStatement> uses;

    private String javaPackage;

    AbstractCompositeGenerator(final T statement) {
        super(statement);

        final List<UsesEffectiveStatement> tmpUses = new ArrayList<>(2);
        final List<Generator<?>> tmpChildren = new ArrayList<>();

        for (EffectiveStatement<?, ?> stmt : statement.effectiveSubstatements()) {
            // We process nodes introduced through augment or uses separately
            if (stmt instanceof AddedByUsesAware && ((AddedByUsesAware) stmt).isAddedByUses()
                || stmt instanceof CopyableNode && ((CopyableNode) stmt).isAugmenting()) {
                continue;
            }

            final Generator<?> child;
            if (stmt instanceof ActionEffectiveStatement) {
                child = new ActionGenerator((ActionEffectiveStatement) stmt);
            } else if (stmt instanceof AnydataEffectiveStatement) {
                child = new AnydataGenerator((AnydataEffectiveStatement) stmt);
            } else if (stmt instanceof AnyxmlEffectiveStatement) {
                child = new AnyxmlGenerator((AnyxmlEffectiveStatement) stmt);
            } else if (stmt instanceof AugmentEffectiveStatement) {
                child = new AugmentGenerator((AugmentEffectiveStatement) stmt);
            } else if (stmt instanceof CaseEffectiveStatement) {
                child = new CaseGenerator((CaseEffectiveStatement) stmt);
            } else if (stmt instanceof ChoiceEffectiveStatement) {
                child = new ChoiceGenerator((ChoiceEffectiveStatement) stmt);
            } else if (stmt instanceof ContainerEffectiveStatement) {
                child = new ContainerGenerator((ContainerEffectiveStatement) stmt);
            } else if (stmt instanceof GroupingEffectiveStatement) {
                child = new GroupingGenerator((GroupingEffectiveStatement) stmt);
            } else if (stmt instanceof IdentityEffectiveStatement) {
                child = new IdentityGenerator((IdentityEffectiveStatement) stmt);
            } else if (stmt instanceof InputEffectiveStatement) {
                child = new InputGenerator((InputEffectiveStatement) stmt);
            } else if (stmt instanceof LeafEffectiveStatement) {
                child = new LeafGenerator((LeafEffectiveStatement) stmt);
            } else if (stmt instanceof LeafListEffectiveStatement) {
                child = new LeafListGenerator((LeafListEffectiveStatement) stmt);
            } else if (stmt instanceof ListEffectiveStatement) {
                child = new ListGenerator((ListEffectiveStatement) stmt);
            } else if (stmt instanceof NotificationEffectiveStatement) {
                child = new NotificationGenerator((NotificationEffectiveStatement) stmt);
            } else if (stmt instanceof OutputEffectiveStatement) {
                child = new OutputGenerator((OutputEffectiveStatement) stmt);
            } else if (stmt instanceof RpcEffectiveStatement) {
                child = new RpcGenerator((RpcEffectiveStatement) stmt);
            } else if (stmt instanceof TypedefEffectiveStatement) {
                child = new TypedefGenerator((TypedefEffectiveStatement) stmt);
            } else {
                if (stmt instanceof UsesEffectiveStatement) {
                    tmpUses.add((UsesEffectiveStatement) stmt);
                }
                continue;
            }

            tmpChildren.add(child);
        }

        children = ImmutableList.copyOf(tmpChildren);
        uses = ImmutableList.copyOf(tmpUses);
    }

    @Override
    public final Iterator<Generator<?>> iterator() {
        return children.iterator();
    }

    final @NonNull String javaPackage() {
        final String local = javaPackage;
        checkState(local != null, "Attempted to access Java package of %s", this);
        return local;
    }

    final void setJavaPackage(final String javaPackage) {
        checkState(this.javaPackage == null, "Attempted to re-assign package from %s to %s in %s", this.javaPackage,
            javaPackage, this);
        this.javaPackage = requireNonNull(javaPackage);
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
        final Multimap<String, Generator<?>> toAssign = ArrayListMultimap.create();
        for (Generator<?> child : children) {
            toAssign.put(child.preferredName(), child);
        }

        // FIXME: do we need this map?
        final Map<String, Generator<?>> assignments = Maps.newHashMapWithExpectedSize(toAssign.size());
        assignNonConflicting(toAssign, assignments);
        if (!toAssign.isEmpty()) {
            assignConflicting(toAssign, assignments);
            verify(toAssign.isEmpty(), "Failed to resolve %s", toAssign);
        }

        // All children are now properly assigned, now let them assign names to their children (and their children, ...)
        for (Generator<?> child : children) {
            if (child instanceof AbstractCompositeGenerator) {
                ((AbstractCompositeGenerator<?>) child).setChildSimpleNames();
            }
        }
    }

    // Deal with names which do not create a conflict. This is very simple and also very effective, we rarely run into
    // conflicts.
    private static void assignNonConflicting(final Multimap<String, Generator<?>> toAssign,
            final Map<String, Generator<?>> assignments) {
        final var it = toAssign.asMap().entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, Collection<Generator<?>>> entry = it.next();
            final Collection<Generator<?>> assignees = entry.getValue();
            if (assignees.size() == 1) {
                verify(assignees instanceof List, "Unexpected assignees %s", assignees);
                assign(assignments, ((List<Generator<?>>) assignees).get(0), entry.getKey());
                it.remove();
            }
        }
    }

    // Deal with name conflicts, which is problematic as we need to locate the source of each conflict
    private static void assignConflicting(final Multimap<String, Generator<?>> toAssign,
            final Map<String, Generator<?>> assignments) {
        // Strategy one: assign a unique suffix to each name based on the YANG namespace it comes from. For that we need
        //               an index first...
        final Table<StatementNamespace, String, List<Generator<?>>> nsNameAssignee =
            HashBasedTable.create();
        for (Entry<String, Collection<Generator<?>>> entry : toAssign.asMap().entrySet()) {
            final var namespaceToChild = nsNameAssignee.column(entry.getKey());
            for (Generator<?> child : entry.getValue()) {
                namespaceToChild.computeIfAbsent(child.namespace(), x -> new ArrayList<>(1)).add(child);
            }
        }

        // ... then walk the index, either assigning the name or remembering to further process it. At the end of the
        // process we have completely taken ownership of 'toAssign' and clear it
        final Multimap<String, Generator<?>> nsNameConflicts = ArrayListMultimap.create();
        for (var cell : nsNameAssignee.cellSet()) {
            final String assignedName = verifyNotNull(cell.getRowKey()).appendSuffix(cell.getColumnKey());
            final List<Generator<?>> assignees = verifyNotNull(cell.getValue());
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
    private static void assignNamespaceConflicting(final Multimap<String, Generator<?>> toAssign,
            final Map<String, Generator<?>> assignments) {
        // FIXME: track down issue number
        // FIXME: YANGTOOLS-XXX: implement this
        //        The algorithm needs to essentially fall back to using escape-based translation scheme, where each
        //        localName results in a unique name, while not conflicting with any possible preferredName. The exact
        //        mechanics for that are TBD, but note the keys we get in toAssign already have a namespace-specific
        //        suffix. A requirement for that mapping is that it must not rely on definition order.
        //
        //        But there is another possible step: since we are assigning 14 different statements into the default
        //        namespace (which did not add a suffix), we can try to assign a statement-derived suffix. To make
        //        things easier, we use two-characters: AC, AD, AU, AX, CA, CH, CO, IP, LE, LI, LL, NO, OP, RP.
    }

    private static void assign(final Map<String, Generator<?>> assignments, final Generator<?> assignee,
            final @NonNull String simpleName) {
        assignee.setSimpleName(simpleName);
        assignments.put(simpleName, assignee);
    }
}
