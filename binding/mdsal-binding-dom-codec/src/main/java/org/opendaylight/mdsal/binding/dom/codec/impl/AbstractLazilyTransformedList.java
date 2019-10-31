/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;

abstract class AbstractLazilyTransformedList<T extends DataObject> extends AbstractList<T> implements Immutable {
    private static final VarHandle DOA_VH = MethodHandles.arrayElementVarHandle(DataObject[].class);

    private final DataObject[] entries;

    AbstractLazilyTransformedList(final int size) {
        this.entries = new DataObject[size];
    }

    @Override
    public final int size() {
        return entries.length;
    }

    @Override
    public final @NonNull T get(final int index) {
        final DataObject ret = (DataObject) DOA_VH.getAcquire(entries, index);
        return (T) (ret != null ? ret : populateOffset(index));
    }

    @Override
    protected final void removeRange(final int fromIndex, final int toIndex) {
        throw new UnsupportedOperationException();
    }

    final @NonNull DataObject casValue(final int index, final @NonNull T value) {
        final DataObject witness = (DataObject) DOA_VH.compareAndExchangeRelease(entries, index, null, value);
        return witness == null ? value : witness;
    }

    abstract @NonNull DataObject populateOffset(int index);
}
