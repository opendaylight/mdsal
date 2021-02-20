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
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;

final class CollisionDomain {
    abstract class Member {
        private String currentPackage;
        private String currentClass;

        final @NonNull String currentClass() {
            if (currentClass == null) {
                currentClass = computeCurrentClass();
            }
            return currentClass;
        }

        final @NonNull String currentPackage() {
            if (currentPackage == null) {
                currentPackage = computeCurrentPackage();
            }
            return currentPackage;
        }

        abstract String computeCurrentClass();

        abstract String computeCurrentPackage();

        boolean signalConflict() {
            solved = false;
            currentClass = null;
            currentPackage = null;
            return true;
        }
    }

    private final class Primary extends Member {
        private NamingStrategy strategy;
        private List<Secondary> secondaries = List.of();

        Primary(final NamingStrategy strategy) {
            this.strategy = requireNonNull(strategy);
        }

        @Override
        String computeCurrentClass() {
            return strategy.simpleClassName();
        }

        @Override
        String computeCurrentPackage() {
            return strategy.packageNameSegment();
        }

        void addSecondary(final Secondary secondary) {
            if (secondaries.isEmpty()) {
                secondaries = new ArrayList<>();
            }
            secondaries.add(requireNonNull(secondary));
        }

        @Override
        boolean signalConflict() {
            final NamingStrategy newStrategy = strategy.fallback();
            if (newStrategy == null) {
                return false;
            }

            strategy = newStrategy;
            super.signalConflict();
            for (Secondary secondary : secondaries) {
                // FIXME: propagate invalidation
            }
            return true;
        }

    }

    private final class Secondary extends Member {
        private final Primary primary;
        private final String suffix;

        Secondary(final Primary primary, final String suffix) {
            this.primary = requireNonNull(primary);
            this.suffix = requireNonNull(suffix);
        }

        @Override
        String computeCurrentClass() {
            return primary.currentClass() + suffix;
        }

        @Override
        String computeCurrentPackage() {
            return primary.currentPackage() + suffix;
        }
    }

    private List<Member> members = List.of();
    private boolean solved;

    @NonNull Member addPrimary(final NamingStrategy strategy) {
        final Primary primary = new Primary(strategy);
        addMember(primary);
        return primary;
    }

    @NonNull Member addSecondary(final Member primary, final String suffix) {
        verify(primary instanceof Primary, "Unexpected primary %s", primary);
        final Primary cast = (Primary) primary;
        final Secondary secondary = new Secondary(cast, suffix);
        cast.addSecondary(secondary);
        addMember(secondary);
        return secondary;
    }

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
    boolean findSolution() {
        if (solved) {
            // Already solved, nothing to do
            return false;
        }
        if (members.size() < 2) {
            // Zero or one member: no conflict possible
            solved = true;
            return false;
        }

        boolean result = false;
        do {
            // Construct mapping to discover any naming overlaps.
            final Multimap<String, Member> toAssign = ArrayListMultimap.create();
            for (Member member : members) {
                toAssign.put(member.currentClass(), member);
            }

            // Deal with names which do not create a conflict. This is very simple and also very effective, we rarely
            // run into conflicts.
            final var it = toAssign.asMap().entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String, Collection<Member>> entry = it.next();
                final Collection<Member> assignees = entry.getValue();
                if (assignees.size() == 1) {
                    it.remove();
                }
            }

            // This looks counter-intuitive, but the idea is simple: the act of assigning a different strategy may end
            // up creating conflicts where there were none -- including in this domain. Marking this bit allows us to
            // react to such invalidation chains and retry the process.
            solved = true;
            if (!toAssign.isEmpty()) {
                result = true;
                // We still have some assignments we need to resolve -- which means we need to change their strategy.
                for (Collection<Member> members : toAssign.asMap().values()) {
                    int remaining = 0;
                    for (Member member : members) {
                        if (!member.signalConflict()) {
                            remaining++;
                        }
                    }
                    checkState(remaining < 2, "Failed to resolve members %s", members);
                }
            }
        } while (!solved);

        return result;
    }

    private void addMember(final Member member) {
        if (members.isEmpty()) {
            members = new ArrayList<>();
        }
        members.add(member);
    }
}
