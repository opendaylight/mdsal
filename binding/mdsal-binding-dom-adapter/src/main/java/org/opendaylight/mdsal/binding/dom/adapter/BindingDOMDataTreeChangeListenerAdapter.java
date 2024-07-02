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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

/**
 * Adapter wrapping Binding {@link DataTreeChangeListener} and exposing it as {@link DOMDataTreeChangeListener}
 * and translated DOM events to their Binding equivalent.
 */
@NonNullByDefault
final class BindingDOMDataTreeChangeListenerAdapter<T extends DataObject> implements DOMDataTreeChangeListener {
    private final AdapterContext adapterContext;
    private final DataTreeChangeListener<T> listener;
    private final LogicalDatastoreType store;
    private final @Nullable Class<? extends Augmentation<?>> augment;

    private boolean initialSyncDone;

    BindingDOMDataTreeChangeListenerAdapter(final AdapterContext adapterContext, final LogicalDatastoreType store,
            final DataObjectReference<T> treeId, final DataTreeChangeListener<T> listener) {
        this.adapterContext = requireNonNull(adapterContext);
        this.listener = requireNonNull(listener);
        this.store = requireNonNull(store);
        augment = extractAugment(treeId.lastStep().type());
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Class<? extends Augmentation<?>> extractAugment(final Class<?> target) {
        return Augmentation.class.isAssignableFrom(target)
            ? (Class<? extends Augmentation<?>>) target.asSubclass(Augmentation.class) : null;
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
