/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

final class LazilyTransformedUnkeyedListNode<T extends DataObject> extends AbstractLazilyTransformedList<T> {
    private final Function<UnkeyedListEntryNode, T> function;
    private final UnkeyedListNode delegate;

    LazilyTransformedUnkeyedListNode(final UnkeyedListNode delegate, final int size,
            final Function<UnkeyedListEntryNode, T> function) {
        super(size);
        this.delegate = requireNonNull(delegate);
        this.function = requireNonNull(function);
    }

    @Override
    DataObject populateOffset(final int index) {
        return casValue(index, function.apply(delegate.getChild(index)));
    }
}
