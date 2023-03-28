/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Iterables;
import java.util.Collection;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DataChangeListenerAdapter<T extends DataObject> extends ForwardingObject
        implements ClusteredDataTreeChangeListener<T> {
    private final DataChangeListener<T> delegate;

    DataChangeListenerAdapter(final DataChangeListener<T> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<T>> changes) {
        delegate.dataChanged(changes.iterator().next().getRootNode().getDataBefore(),
            Iterables.getLast(changes).getRootNode().getDataAfter());
    }

    @Override
    public void onInitialData() {
        delegate.dataChanged(null, null);
    }

    @Override
    protected DataChangeListener<T> delegate() {
        return delegate;
    }
}
