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
import java.util.List;
import org.opendaylight.yangtools.binding.DataObject;

final class DataChangeListenerAdapter<T extends DataObject> extends ForwardingObject
        implements DataTreeChangeListener<T> {
    private final DataChangeListener<T> delegate;

    DataChangeListenerAdapter(final DataChangeListener<T> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
        delegate.dataChanged(changes.get(0).getRootNode().dataBefore(),
            changes.get(changes.size() - 1).getRootNode().dataAfter());
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
