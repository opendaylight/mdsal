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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Lookup context for dealing with namespace translation during execution of {@link AbstractCompositeGenerator}'s
 * createInternalRuntimeType(). It tracks which namespaces should be translated on account of crossing into source
 * {@code grouping} statement.
 */
final class ChildLookup implements Immutable {
    private static final @NonNull ChildLookup EMPTY = new ChildLookup(ImmutableSet.of(), null);

    private final ImmutableSet<QNameModule> squashNamespaces;
    private final QNameModule localNamespace;

    private ChildLookup(final ImmutableSet<QNameModule> squashNamespaces, final QNameModule localNamespace) {
        this.squashNamespaces = requireNonNull(squashNamespaces);
        this.localNamespace = localNamespace;
        verify(localNamespace == null == squashNamespaces.isEmpty(), "Unexpected lookup state %s", this);
    }

    static @NonNull ChildLookup empty() {
        return EMPTY;
    }

    @NonNull QName adjustQName(final @NonNull QName qname) {
        return squashNamespaces.contains(qname.getModule()) ? qname.bindTo(verifyNotNull(localNamespace)) : qname;
    }

    @NonNull ChildLookup inGrouping(final QName qname, final GroupingGenerator generator) {
        final var grpNamespace = generator.getQName().getModule();
        final var itemNamespace = qname.getModule();

        final ImmutableSet<QNameModule> newSquashNamespaces;
        if (squashNamespaces.contains(itemNamespace)) {
            if (grpNamespace.equals(localNamespace)) {
                return this;
            }
            newSquashNamespaces = squashNamespaces;
        } else {
            newSquashNamespaces = ImmutableSet.<QNameModule>builderWithExpectedSize(squashNamespaces.size() + 1)
                .addAll(squashNamespaces).add(itemNamespace).build();
        }

        return new ChildLookup(newSquashNamespaces, grpNamespace);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("squash", squashNamespaces)
            .add("local", localNamespace)
            .toString();
    }

}
