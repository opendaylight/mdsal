/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import java.util.regex.Pattern;
import org.opendaylight.mdsal.query.binding.adapter.AdaptingQueryBuilder.BoundMethod;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderValueString;
import org.opendaylight.mdsal.query.binding.api.ValueMatch;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.Contains;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.EndsWith;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.MatchesPattern;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate.StartsWith;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultMatchBuilderValueString<R extends DataObject, O extends DataObject, T extends DataObject>
        extends AbstractMatchBuilderValue<R, O, T, String> implements MatchBuilderValueString<T> {
    DefaultMatchBuilderValueString(final AdaptingQueryBuilder builder, final BoundMethod method) {
        super(builder, method);
    }

    @Override
    public ValueMatch<T> startsWith(final String str) {
        return withPredicate(new StartsWith(str));
    }

    @Override
    public ValueMatch<T> endsWith(final String str) {
        return withPredicate(new EndsWith(str));
    }

    @Override
    public ValueMatch<T> contains(final String str) {
        return withPredicate(new Contains(str));
    }

    @Override
    public ValueMatch<T> matchesPattern(final Pattern pattern) {
        return withPredicate(new MatchesPattern(pattern));
    }
}
