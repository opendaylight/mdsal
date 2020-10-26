/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@NonNullByDefault
final class SimpleDOMQueryResult implements DOMQueryResult {
    static final SimpleDOMQueryResult EMPTY_INSTANCE = new SimpleDOMQueryResult(ImmutableList.of());

    private final ImmutableList<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items;

    SimpleDOMQueryResult(final ImmutableList<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items) {
        this.items = items;
    }

    SimpleDOMQueryResult(final List<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items) {
        this(ImmutableList.copyOf(items));
    }

    @Override
    public Iterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> iterator() {
        return items.iterator();
    }

    @Override
    public Spliterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> spliterator() {
        return items.spliterator();
    }

    @Override
    public List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items() {
        return items;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("items", items).toString();
    }
}
