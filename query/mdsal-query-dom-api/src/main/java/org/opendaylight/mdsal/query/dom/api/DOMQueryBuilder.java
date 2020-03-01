/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.dom.api;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.CheckedBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@Beta
public final class DOMQueryBuilder implements CheckedBuilder<DOMQuery, IllegalStateException> {
    private static final class PathPredicate implements Immutable {
        final YangInstanceIdentifier path;
        final DOMQueryPredicate predicate;

        PathPredicate(final YangInstanceIdentifier path, final DOMQueryPredicate predicate) {
            this.path = requireNonNull(path);
            this.predicate = requireNonNull(predicate);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("path", path).add("predicate", predicate).toString();
        }

    }

    private final List<PathPredicate> predicates = new ArrayList<>();

    private YangInstanceIdentifier queryRoot;
    private YangInstanceIdentifier querySelect;

    public @NonNull DOMQueryBuilder setRootPath(final @NonNull YangInstanceIdentifier rootPath) {
        checkState(queryRoot == null, "Root path has already been set to %s", queryRoot);
        queryRoot = requireNonNull(rootPath);
        return this;
    }

    public @NonNull DOMQueryBuilder setSelectPath(final @NonNull YangInstanceIdentifier selectPath) {
        checkState(querySelect == null, "Root path has already been set to %s", querySelect);
        querySelect = requireNonNull(selectPath);
        return this;
    }

    public @NonNull DOMQueryBuilder addPredicate(final @NonNull YangInstanceIdentifier path,
            final @NonNull DOMQueryPredicate predicate) {
        predicates.add(new PathPredicate(path, predicate));
        return this;
    }

    @Override
    public DOMQuery build() {
        // TODO Auto-generated method stub
        return null;
    }
}
