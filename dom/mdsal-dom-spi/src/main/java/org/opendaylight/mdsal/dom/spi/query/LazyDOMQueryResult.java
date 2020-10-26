/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@NonNullByDefault
final class LazyDOMQueryResult implements DOMQueryResult {
    private final NormalizedNode<?, ?> queryRoot;
    private final DOMQuery query;

    LazyDOMQueryResult(final NormalizedNode<?, ?> queryRoot, final DOMQuery query) {
        this.queryRoot = requireNonNull(queryRoot);
        this.query = requireNonNull(query);
    }

    @Override
    public Iterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> iterator() {
        return new LazyDOMQueryResultIterator(query, queryRoot);
    }
}
