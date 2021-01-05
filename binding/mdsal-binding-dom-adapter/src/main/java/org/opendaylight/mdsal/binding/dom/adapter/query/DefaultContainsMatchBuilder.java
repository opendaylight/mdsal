/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.ContainsMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.binding.DataObject;

class DefaultContainsMatchBuilder<T extends DataObject, V> implements ContainsMatchBuilder<T, V> {
    private final AbstractLeafListValueMatchBuilder<T, V> builder;

    DefaultContainsMatchBuilder(final AbstractLeafListValueMatchBuilder<T, V> builder) {
        this.builder = requireNonNull(builder);
    }

    @Override
    public final ValueMatch<T> item(final V value) {
        return matching(Match.containsItem(value));
    }

    @Override
    public final ValueMatch<T> allOf(final Collection<? extends V> values) {
        return matching(Match.containsAll(values));
    }

    @Override
    public final ValueMatch<T> anyOf(final Collection<? extends V> values) {
        return matching(Match.containsAny(values));
    }

    @Override
    public final ValueMatch<T> noneOf(final Collection<? extends V> values) {
        return matching(Match.containsAny(values).negate());
    }

    @Override
    public final ValueMatch<T> subsetOf(final Collection<? extends V> values) {
        // FIXME: implement this
        return matching(null);
    }

    final @NonNull ValueMatch<T> matching(final @NonNull Match match) {
        return builder.withMatch(match);
    }
}
