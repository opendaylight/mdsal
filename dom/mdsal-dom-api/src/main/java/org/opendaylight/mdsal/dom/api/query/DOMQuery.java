/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@Beta
public final class DOMQuery implements Immutable {
    private final @NonNull YangInstanceIdentifier root;
    // Note: relative to root
    private final @NonNull YangInstanceIdentifier select;
    private final @NonNull ImmutableList<DOMQueryPredicate> predicates;

    public DOMQuery(final YangInstanceIdentifier root, final YangInstanceIdentifier select,
            final List<? extends DOMQueryPredicate> predicates) {
        this.root = requireNonNull(root);
        this.select = requireNonNull(select);
        this.predicates = ImmutableList.copyOf(predicates);
    }

    public @NonNull YangInstanceIdentifier getRoot() {
        return root;
    }

    public @NonNull YangInstanceIdentifier getSelect() {
        return select;
    }

    public @NonNull List<? extends DOMQueryPredicate> getPredicates() {
        return predicates;
    }
}
