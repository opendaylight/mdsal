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

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

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

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
        }

        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("class", currentClass).add("package", currentPackage);
        }
    }

    private class Primary extends Member {
        private ClassNamingStrategy strategy;
        private List<Secondary> secondaries = List.of();

        Primary(final ClassNamingStrategy strategy) {
            this.strategy = requireNonNull(strategy);
        }

        @Override
        final String computeCurrentClass() {
            return strategy.simpleClassName();
        }

        @Override
        final String computeCurrentPackage() {
            return packageString(strategy.nodeIdentifier());
        }

        final void addSecondary(final Secondary secondary) {
            if (secondaries.isEmpty()) {
                secondaries = new ArrayList<>();
            }
            secondaries.add(requireNonNull(secondary));
        }

        @Override
        final boolean signalConflict() {
            final ClassNamingStrategy newStrategy = strategy.fallback();
            if (newStrategy == null) {
                return false;
            }

            strategy = newStrategy;
            super.signalConflict();
            for (Secondary secondary : secondaries) {
                secondary.primaryConflict();
            }
            return true;
        }

        @Override
        final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper.add("strategy", strategy));
        }
    }

    private final class Prefix extends Primary {
        Prefix(final ClassNamingStrategy strategy) {
            super(strategy);
        }
    }

    private abstract class Secondary extends Member {
        private final String classSuffix;
        final Primary classPrimary;

        Secondary(final Primary primary, final String classSuffix) {
            this.classPrimary = requireNonNull(primary);
            this.classSuffix = requireNonNull(classSuffix);
            primary.addSecondary(this);
        }

        @Override
        final String computeCurrentClass() {
            return classPrimary.currentClass() + classSuffix;
        }

        @Override
        final boolean signalConflict() {
            return classPrimary.signalConflict();
        }

        final void primaryConflict() {
            super.signalConflict();
        }
    }

    private final class LeafSecondary extends Secondary {
        LeafSecondary(final Primary classPrimary, final String classSuffix) {
            super(classPrimary, classSuffix);
        }

        @Override
        String computeCurrentPackage() {
            // This should never happen
            throw new UnsupportedOperationException();
        }
    }

    private final class SuffixSecondary extends Secondary {
        private final AbstractQName packageSuffix;

        SuffixSecondary(final Primary primaryClass, final String classSuffix, final AbstractQName packageSuffix) {
            super(primaryClass, classSuffix);
            this.packageSuffix = requireNonNull(packageSuffix);
        }

        @Override
        String computeCurrentPackage() {
            return classPrimary.currentPackage() + '.' + packageString(packageSuffix);
        }
    }

    private final class AugmentSecondary extends Secondary {
        private final SchemaNodeIdentifier packageSuffix;

        AugmentSecondary(final Primary primary, final String classSuffix, final SchemaNodeIdentifier packageSuffix) {
            super(primary, classSuffix);
            this.packageSuffix = requireNonNull(packageSuffix);
        }

        @Override
        String computeCurrentPackage() {
            final Iterator<QName> it = packageSuffix.getNodeIdentifiers().iterator();

            final StringBuilder sb = new StringBuilder();
            sb.append(packageString(it.next()));
            while (it.hasNext()) {
                sb.append('.').append(packageString(it.next()));
            }
            return sb.toString();
        }
    }

    private List<Member> members = List.of();
    private boolean solved;

    @NonNull Member addPrefix(final ClassNamingStrategy strategy) {
        // Note that contrary to the method name, we are not adding the result to members
        return new Prefix(strategy);
    }

    @NonNull Member addPrimary(final ClassNamingStrategy strategy) {
        return addMember(new Primary(strategy));
    }

    @NonNull Member addSecondary(final Member primary, final String classSuffix) {
        return addMember(new LeafSecondary(castPrimary(primary), classSuffix));
    }

    @NonNull Member addSecondary(final Member primary, final String classSuffix, final AbstractQName packageSuffix) {
        return addMember(new SuffixSecondary(castPrimary(primary), classSuffix, packageSuffix));
    }

    @NonNull Member addSecondary(final Member classPrimary, final String classSuffix,
            final SchemaNodeIdentifier packageSuffix) {
        return addMember(new AugmentSecondary(castPrimary(classPrimary), classSuffix, packageSuffix));
    }

    private static @NonNull Primary castPrimary(final Member primary) {
        verify(primary instanceof Primary, "Unexpected primary %s", primary);
        return (Primary) primary;
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
                for (Collection<Member> conflicting : toAssign.asMap().values()) {
                    int remaining = 0;
                    for (Member member : conflicting) {
                        if (!member.signalConflict()) {
                            remaining++;
                        }
                    }
                    checkState(remaining < 2, "Failed to resolve members %s", conflicting);
                }
            }
        } while (!solved);

        return result;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private @NonNull Member addMember(final @NonNull Member member) {
        if (members.isEmpty()) {
            members = new ArrayList<>();
        }
        members.add(member);
        return member;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static @NonNull String packageString(final AbstractQName component) {
        // Replace dashes with dots, as dashes are not allowed in package names
        return component.getLocalName().replace('-', '.');
    }
}
