/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
@NonNullByDefault
public final class EagerDOMQueryResult implements DOMQueryResult {
    private static final EagerDOMQueryResult EMPTY_INSTANCE = new EagerDOMQueryResult(ImmutableList.of());

    private final ImmutableList<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items;

    private EagerDOMQueryResult(final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items) {
        this.items = ImmutableList.copyOf(items);
    }

    public static EagerDOMQueryResult of() {
        return EMPTY_INSTANCE;
    }

    public static EagerDOMQueryResult of(final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> item) {
        return new EagerDOMQueryResult(ImmutableList.of(item));
    }

    public static EagerDOMQueryResult of(
            final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items) {
        return items.isEmpty() ? of() : new EagerDOMQueryResult(items);
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
}
