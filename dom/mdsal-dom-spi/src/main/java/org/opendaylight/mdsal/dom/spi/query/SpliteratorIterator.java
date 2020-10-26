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
import java.util.Spliterator;

final class SpliteratorIterator<T> extends AbstractIterator<T> {
    private final Spliterator<T> split;

    private T next;

    SpliteratorIterator(final Spliterator<T> split) {
        this.split = requireNonNull(split);
    }

    @Override
    protected T computeNext() {
        next = null;
        return split.tryAdvance(item -> next = item) ? next : endOfData();
    }
}
