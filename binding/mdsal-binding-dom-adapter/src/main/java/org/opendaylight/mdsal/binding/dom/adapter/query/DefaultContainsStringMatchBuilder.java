/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.api.query.ContainsStringMatchBuilder;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultContainsStringMatchBuilder<T extends DataObject> extends DefaultContainsMatchBuilder<T, String>
        implements ContainsStringMatchBuilder<T> {

    DefaultContainsStringMatchBuilder(final DefaultStringLeafListMatchBuilder<T> builder) {
        super(builder);
    }

    @Override
    public ValueMatch<T> startingWith(final String str) {
        // FIXME: implement this
        return matching(null);
    }

    @Override
    public ValueMatch<T> endingWith(final String str) {
        // FIXME: implement this
        return matching(null);
    }

    @Override
    public ValueMatch<T> containing(final String str) {
        // FIXME: implement this
        return matching(null);
    }

    @Override
    public ValueMatch<T> matchingPattern(final Pattern pattern) {
        // FIXME: implement this
        return matching(null);
    }
}
