/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

record SimpleDOMQueryResult(@NonNull ImmutableList<Entry<YangInstanceIdentifier, NormalizedNode>> items)
        implements DOMQueryResult {
    static final SimpleDOMQueryResult EMPTY_INSTANCE = new SimpleDOMQueryResult(ImmutableList.of());

    SimpleDOMQueryResult {
        requireNonNull(items);
    }

    @Override
    public Iterator<@NonNull Entry<@NonNull YangInstanceIdentifier, @NonNull NormalizedNode>> iterator() {
        return items.iterator();
    }

    @Override
    public Spliterator<@NonNull Entry<@NonNull YangInstanceIdentifier, @NonNull NormalizedNode>> spliterator() {
        return items.spliterator();
    }
}
