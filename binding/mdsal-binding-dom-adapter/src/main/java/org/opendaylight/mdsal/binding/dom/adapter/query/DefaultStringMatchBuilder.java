/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.api.query.StringMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Contains;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.EndsWith;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.MatchesPattern;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.StartsWith;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultStringMatchBuilder<T extends DataObject> extends AbstractValueMatchBuilder<T, String>
        implements StringMatchBuilder<T> {
    DefaultStringMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        super(builder, select, method);
    }

    @Override
    public ValueMatch<T> startsWith(final String str) {
        return withPredicate(new StartsWith(relativePath(), str));
    }

    @Override
    public ValueMatch<T> endsWith(final String str) {
        return withPredicate(new EndsWith(relativePath(), str));
    }

    @Override
    public ValueMatch<T> contains(final String str) {
        return withPredicate(new Contains(relativePath(), str));
    }

    @Override
    public ValueMatch<T> matchesPattern(final Pattern pattern) {
        return withPredicate(new MatchesPattern(relativePath(), pattern));
    }
}
