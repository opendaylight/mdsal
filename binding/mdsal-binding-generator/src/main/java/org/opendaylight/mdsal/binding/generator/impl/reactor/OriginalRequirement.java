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
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

final class OriginalRequirement {
    private final @NonNull Consumer<AbstractCompositeGenerator<?>> callback;
    private final @NonNull Set<QNameModule> squashNamespaces;
    private final @Nullable QNameModule localNamespace;
    private final @NonNull Iterator<QName> remaining;
    private final @NonNull QName qname;

    private OriginalRequirement(final Consumer<AbstractCompositeGenerator<?>> callback,
            final Iterator<QName> remaining, final QName qname, final QNameModule localNamespace,
            final Set<QNameModule> squashNamespaces) {
        this.callback = requireNonNull(callback);
        this.remaining = requireNonNull(remaining);
        this.qname = requireNonNull(qname);
        this.squashNamespaces = requireNonNull(squashNamespaces);
        this.localNamespace = localNamespace;
    }

    OriginalRequirement(final Consumer<AbstractCompositeGenerator<?>> callback, final Iterator<QName> path) {
        this(callback, path, path.next(), null, new HashSet<>(4));
    }

    OriginalRequirement(final Consumer<AbstractCompositeGenerator<?>> callback, final Iterator<QName> path,
            final GroupingGenerator grouping) {
        this(callback, path, path.next(), grouping.getQName().getModule(), new HashSet<QNameModule>(4));
        squashNamespaces.add(qname.getModule());
    }

    @NonNull QName qname() {
        return qname;
    }

    @NonNull QName adjustedQName() {
        return squashNamespaces.contains(qname.getModule()) ? qname.bindTo(verifyNotNull(localNamespace)) : qname;
    }

    void inAugment(final AbstractExplicitGenerator<?> gen) {
        addRequirement(gen, new HashSet<>(4));
    }

    void inGrouping(final GroupingGenerator grouping, final AbstractExplicitGenerator<?> gen) {
        squashNamespaces.add(grouping.statement().argument().getModule());
        addRequirement(gen, squashNamespaces);
    }

    void inLocal(final AbstractExplicitGenerator<?> gen) {
        addRequirement(gen, squashNamespaces);
    }

    private void addRequirement(final AbstractExplicitGenerator<?> gen, final Set<QNameModule> newSquashModules) {
        final var composite = verifyComposite(gen);
        if (remaining.hasNext()) {
            composite.addRequirement(new OriginalRequirement(callback, remaining, remaining.next(), localNamespace,
                newSquashModules));
        } else {
            composite.addRequirement(callback);
        }
    }

    private static AbstractCompositeGenerator<?> verifyComposite(final AbstractExplicitGenerator<?> gen) {
        verify(gen instanceof AbstractCompositeGenerator, "Unexpected generator %s", gen);
        return (AbstractCompositeGenerator<?>) gen;
    }
}