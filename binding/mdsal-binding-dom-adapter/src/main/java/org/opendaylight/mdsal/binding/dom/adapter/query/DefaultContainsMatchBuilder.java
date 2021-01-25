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
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.ContainsMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.binding.DataObject;

class DefaultContainsMatchBuilder<T extends DataObject, V> implements ContainsMatchBuilder<T, V> {
    /**
     * Logical match operator on a set of elements.
     */
    enum Operator {
        ITEM(Function.identity()),
        ALL(Match::matchesAll),
        ANY(Match::matchesAny),
        NONE(x -> Match.matchesAny(x).negate());

        final @NonNull Function<Match, Match> function;

        Operator(final @NonNull Function<Match, Match> function) {
            this.function = function;
        }
    }

    private final AbstractLeafListMatchBuilder<T, V> builder;
    private final Function<Match, Match> operator;

    DefaultContainsMatchBuilder(final AbstractLeafListMatchBuilder<T, V> builder, final Operator operator) {
        this.builder = requireNonNull(builder);
        this.operator = operator.function;
    }

    @Override
    public final ValueMatch<T> item(final V value) {
        return builder.withMatch(Match.containsItem(value));
    }

    @Override
    public final ValueMatch<T> allOf(final Collection<? extends V> values) {
        return builder.withMatch(Match.containsAll(values));
    }

    @Override
    public final ValueMatch<T> anyOf(final Collection<? extends V> values) {
        return builder.withMatch(Match.containsAny(values));
    }

    @Override
    public final ValueMatch<T> noneOf(final Collection<? extends V> values) {
        return builder.withMatch(Match.containsAny(values).negate());
    }

    @Override
    public final ValueMatch<T> subsetOf(final Collection<? extends V> values) {
        // FIXME: implement this
        return builder.withMatch(null);
    }

    final @NonNull ValueMatch<T> matchingItems(final @NonNull Match itemMatch) {
        return builder.withMatch(operator.apply(itemMatch));
    }
}
