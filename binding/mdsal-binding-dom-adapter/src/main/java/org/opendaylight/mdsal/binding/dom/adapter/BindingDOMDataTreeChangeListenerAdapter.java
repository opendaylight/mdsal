/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * Adapter wrapping Binding {@link DataTreeChangeListener} and exposing
 * it as {@link DOMDataTreeChangeListener} and translated DOM events
 * to their Binding equivalent.
 *
 */
class BindingDOMDataTreeChangeListenerAdapter<T extends DataObject> implements DOMDataTreeChangeListener {

    private final BindingToNormalizedNodeCodec codec;
    private final DataTreeChangeListener<T> listener;
    private final LogicalDatastoreType store;

    BindingDOMDataTreeChangeListenerAdapter(final BindingToNormalizedNodeCodec codec,
            final DataTreeChangeListener<T> listener, final LogicalDatastoreType store) {
        this.codec = Preconditions.checkNotNull(codec);
        this.listener = Preconditions.checkNotNull(listener);
        this.store = Preconditions.checkNotNull(store);
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeCandidate> domChanges) {
        final Collection<DataTreeModification<T>> bindingChanges
                = LazyDataTreeModification.from(codec, domChanges, store);
        listener.onDataTreeChanged(bindingChanges);
    }
}
