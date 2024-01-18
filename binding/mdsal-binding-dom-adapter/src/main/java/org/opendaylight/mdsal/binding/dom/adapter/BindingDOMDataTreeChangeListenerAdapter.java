/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

/**
 * Adapter wrapping Binding {@link DataTreeChangeListener} and exposing it as {@link DOMDataTreeChangeListener}
 * and translated DOM events to their Binding equivalent.
 */
final class BindingDOMDataTreeChangeListenerAdapter<T extends DataObject> implements DOMDataTreeChangeListener {
    private final AdapterContext adapterContext;
    private final DataTreeChangeListener<T> listener;
    private final LogicalDatastoreType store;
    private final Class<T> augment;

    private boolean initialSyncDone;

    BindingDOMDataTreeChangeListenerAdapter(final AdapterContext adapterContext, final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> listener) {
        this.adapterContext = requireNonNull(adapterContext);
        this.listener = requireNonNull(listener);
        store = treeId.datastore();

        final var target = treeId.path().getTargetType();
        augment = Augmentation.class.isAssignableFrom(target) ? target : null;
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeCandidate> domChanges) {
        final var changes = LazyDataTreeModification.<T>from(adapterContext.currentSerializer(), domChanges, store,
            augment);
        if (!changes.isEmpty()) {
            listener.onDataTreeChanged(changes);
        } else if (!initialSyncDone) {
            onInitialData();
        }
    }

    @Override
    public void onInitialData() {
        initialSyncDone = true;
        listener.onInitialData();
    }
}
