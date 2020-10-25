/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An object holding the results of a {@link DOMQuery} execution.
 */
@Beta
@NonNullByDefault
public final class DOMQueryResult implements Immutable {
    private static final DOMQueryResult EMPTY_INSTANCE = new DOMQueryResult(ImmutableList.of());

    private final ImmutableList<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items;

    private DOMQueryResult(final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items) {
        this.items = ImmutableList.copyOf(items);
    }

    public static DOMQueryResult of() {
        return EMPTY_INSTANCE;
    }

    public static DOMQueryResult of(final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items) {
        return items.isEmpty() ? of() : new DOMQueryResult(items);
    }

    public List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> items() {
        return items;
    }
}
