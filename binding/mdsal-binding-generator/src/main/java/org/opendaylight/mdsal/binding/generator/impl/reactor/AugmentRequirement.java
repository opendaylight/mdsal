/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

final class AugmentRequirement implements Mutable {
    private final @NonNull Set<QNameModule> squashNamespaces;
    private final @NonNull AbstractAugmentGenerator augment;
    private final @NonNull Iterator<QName> remaining;

    private QNameModule localNamespace;
    private QName qname;

    private AugmentRequirement(final AbstractAugmentGenerator augment, final Iterator<QName> remaining,
            final QName qname, final QNameModule localNamespace, final Set<QNameModule> squashNamespaces) {
        this.augment = requireNonNull(augment);
        this.remaining = requireNonNull(remaining);
        this.qname = requireNonNull(qname);
        this.squashNamespaces = requireNonNull(squashNamespaces);
        this.localNamespace = localNamespace;
    }

    AugmentRequirement(final AbstractAugmentGenerator augment, final Iterator<QName> path) {
        this(augment, path, path.next(), null, new HashSet<>(4));
    }

    AugmentRequirement(final AbstractAugmentGenerator augment, final Iterator<QName> path,
            final GroupingGenerator grouping) {
        this(augment, path, path.next(), grouping.getQName().getModule(), new HashSet<>(4));
        squashNamespaces.add(qname.getModule());
    }

    @NonNull AbstractAugmentGenerator augment() {
        return augment;
    }

    @Nullable QName qname() {
        return qname;
    }

    @NonNull QName adjustedQName() {
        final var qn = verifyNotNull(qname);
        return squashNamespaces.contains(qn.getModule()) ? qn.bindTo(verifyNotNull(localNamespace)) : qn;
    }

    void inGrouping(final GroupingGenerator grouping, final AbstractExplicitGenerator<?> gen) {
        squashNamespaces.add(qname.getModule());
        localNamespace = grouping.statement().argument().getModule();
        grouping.addRequirement(this);
    }

    void inLocal(final AbstractExplicitGenerator<?> gen) {
        addRequirement(gen);
    }

    private void addRequirement(final AbstractExplicitGenerator<?> gen) {
        final var composite = verifyComposite(gen);
        qname = remaining.hasNext() ? remaining.next() : null;
        composite.addRequirement(this);
    }

    private static AbstractCompositeGenerator<?> verifyComposite(final AbstractExplicitGenerator<?> gen) {
        verify(gen instanceof AbstractCompositeGenerator, "Unexpected generator %s", gen);
        return (AbstractCompositeGenerator<?>) gen;
    }
}