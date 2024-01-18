/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataChangeListener;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Adapter exposing Binding {@link DataTreeChangeService} and wrapping a {@link DataTreeChangeExtension} and is
 * responsible for translation and instantiation of {@link BindingDOMDataTreeChangeListenerAdapter} adapters.
 *
 * <p>
 * Each registered {@link DataTreeChangeListener} is wrapped using adapter and registered directly to DOM service.
 */
final class BindingDOMDataTreeChangeServiceAdapter extends AbstractBindingAdapter<DataTreeChangeExtension>
        implements DataTreeChangeService {
    BindingDOMDataTreeChangeServiceAdapter(final AdapterContext adapterContext,
            final DataTreeChangeExtension dataTreeChangeExtension) {
        super(adapterContext, dataTreeChangeExtension);
    }

    @Override
    public <T extends DataObject> Registration registerTreeChangeListener(final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> listener) {
        return getDelegate().registerDataTreeListener(toDomTreeIdentifier(treeId),
            new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), treeId, listener));
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public <T extends DataObject> Registration registerLegacyTreeChangeListener(final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> listener) {
        return getDelegate().registerLegacyDataTreeListener(toDomTreeIdentifier(treeId),
            new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), treeId, listener));
    }

    @Override
    public <T extends DataObject> Registration registerDataListener(final DataTreeIdentifier<T> treeId,
            final DataListener<T> listener) {
        return getDelegate().registerDataTreeListener(toDomTreeInstance(treeId),
            new BindingDOMDataListenerAdapter<>(adapterContext(), listener));
    }

    @Override
    public <T extends DataObject> Registration registerDataChangeListener(final DataTreeIdentifier<T> treeId,
            final DataChangeListener<T> listener) {
        return getDelegate().registerDataTreeListener(toDomTreeInstance(treeId),
            new BindingDOMDataChangeListenerAdapter<>(adapterContext(), listener));
    }

    private @NonNull DOMDataTreeIdentifier toDomTreeIdentifier(final DataTreeIdentifier<?> treeId) {
        return DOMDataTreeIdentifier.of(treeId.datastore(),
            currentSerializer().toYangInstanceIdentifier(treeId.path()));
    }

    private @NonNull DOMDataTreeIdentifier toDomTreeInstance(final DataTreeIdentifier<?> treeId) {
        final var instanceIdentifier = treeId.path();
        if (instanceIdentifier.isWildcarded()) {
            throw new IllegalArgumentException("Cannot register listener for wildcard " + instanceIdentifier);
        }
        return toDomTreeIdentifier(treeId);
    }
}
