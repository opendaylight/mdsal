/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

final class LazilyTransformedMapNode<T extends DataObject> extends AbstractLazilyTransformedList<T> {
    private static final class State {
        private final Function<MapEntryNode, ? extends DataObject> function;

        Iterator<MapEntryNode> iterator;
        int nextIndex = 0;

        State(final Function<MapEntryNode, ? extends DataObject> function, final Collection<MapEntryNode> delegate) {
            this.function = requireNonNull(function);
            this.iterator = delegate.iterator();
        }

        DataObject nextObject() {
            final DataObject next = function.apply(iterator.next());
            nextIndex++;
            if (!iterator.hasNext()) {
                iterator = null;
            }
            return next;
        }
    }

    private volatile State state;

    LazilyTransformedMapNode(final Collection<MapEntryNode> delegate, final int size,
            final Function<MapEntryNode, T> function) {
        super(size);
        this.state = new State(function, delegate);
    }

    @Override
    DataObject populateOffset(final int index) {
        final State local = state;
        return local == null ? casValue(index, null) : populateOffset(local, index);
    }

    private @NonNull DataObject populateOffset(final @NonNull State local, final int index) {
        synchronized (local) {
            while (local.iterator != null) {
                if (local.nextIndex > index) {
                    return casValue(index, null);
                }
                if (local.nextIndex == index) {
                    return casValue(index, (T) local.nextObject());
                }
                casValue(local.nextIndex, (T) local.nextObject());
            }
            state = null;
        }

        return casValue(index, null);
    }
}
