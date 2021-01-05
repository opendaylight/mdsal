/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.api.query.ContainsStringMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueNodeCodecContext;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultContainsStringMatchBuilder<T extends DataObject> extends DefaultContainsMatchBuilder<T, String>
        implements ContainsStringMatchBuilder<T> {
    DefaultContainsStringMatchBuilder(final DefaultLeafListStringMatchBuilder<T> builder,
        final ValueNodeCodecContext valueCodec, final Operator combine) {
        super(builder, valueCodec, combine);
    }

    @Override
    public ValueMatch<T> startsWith(final String str) {
        return matchingItems(Match.stringStartsWith(str));
    }

    @Override
    public ValueMatch<T> endsWith(final String str) {
        return matchingItems(Match.stringEndsWith(str));
    }

    @Override
    public ValueMatch<T> contains(final String str) {
        return matchingItems(Match.stringContains(str));
    }

    @Override
    public ValueMatch<T> matchesPattern(final Pattern pattern) {
        return matchingItems(Match.stringMatches(pattern));
    }
}
