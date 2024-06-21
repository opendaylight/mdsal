/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.api.query.QueryResult.Item;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@NonNullByDefault
final class DefaultQueryResultSpliterator<T extends DataObject> implements Spliterator<QueryResult.Item<T>> {
    private final Spliterator<? extends Entry<YangInstanceIdentifier, NormalizedNode>> domSpliterator;
    private final DefaultQueryResult<T> result;

    DefaultQueryResultSpliterator(final DefaultQueryResult<T> result,
            final Spliterator<? extends Entry<YangInstanceIdentifier, NormalizedNode>> domSpliterator) {
        this.result = requireNonNull(result);
        this.domSpliterator = requireNonNull(domSpliterator);
    }

    @Override
    public boolean tryAdvance(final @Nullable Consumer<? super Item<T>> action) {
        return domSpliterator.tryAdvance(dom -> action.accept(new DefaultQueryResultItem<>(result, dom)));
    }

    @Override
    public @Nullable Spliterator<Item<T>> trySplit() {
        final Spliterator<? extends Entry<YangInstanceIdentifier, NormalizedNode>> split = domSpliterator.trySplit();
        return split == null ? null : new DefaultQueryResultSpliterator<>(result, split);
    }

    @Override
    public long estimateSize() {
        return domSpliterator.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        return domSpliterator.getExactSizeIfKnown();
    }

    @Override
    public int characteristics() {
        return domSpliterator.characteristics();
    }
}
