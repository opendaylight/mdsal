/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.query;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class DOMQueryIterator extends AbstractIterator<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> {
    private final DOMQuerySpliterator sp;

    private @Nullable Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> next = null;

    DOMQueryIterator(final DOMQuerySpliterator sp) {
        this.sp = requireNonNull(sp);
    }

    @Override
    protected Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> computeNext() {
        return sp.tryAdvance(entry -> next = entry) ? next : endOfData();
    }
}
