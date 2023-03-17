/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceWildcard;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Adapter exposing Binding {@link DataTreeChangeService} and wrapping a {@link DOMDataTreeChangeService} and is
 * responsible for translation and instantiation of {@link BindingDOMDataTreeChangeListenerAdapter} adapters.
 *
 * <p>
 * Each registered {@link DataTreeChangeListener} is wrapped using adapter and registered directly to DOM service.
 */
final class BindingDOMDataTreeChangeServiceAdapter extends AbstractBindingAdapter<DOMDataTreeChangeService>
        implements DataTreeChangeService {
    BindingDOMDataTreeChangeServiceAdapter(final AdapterContext adapterContext,
            final DOMDataTreeChangeService dataTreeChangeService) {
        super(adapterContext, dataTreeChangeService);
    }

    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
            registerDataTreeChangeListener(final @NonNull DataTreeIdentifier<T> treeId, final @NonNull L listener) {
        final DOMDataTreeIdentifier domIdentifier = toDomTreeIdentifier(treeId);
        final LogicalDatastoreType storeType = treeId.getDatastoreType();
        final BindingDOMDataTreeChangeListenerAdapter<T> domListener =
                listener instanceof ClusteredDataTreeChangeListener
                        ? new BindingClusteredDOMDataTreeChangeListenerAdapter<>(
                                adapterContext(), (ClusteredDataTreeChangeListener<T>) listener, storeType)
                        : new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), listener, storeType);

        final ListenerRegistration<BindingDOMDataTreeChangeListenerAdapter<T>> domReg =
                getDelegate().registerDataTreeChangeListener(domIdentifier, domListener);
        return new BindingDataTreeChangeListenerRegistration<>(listener, domReg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
        registerDataTreeChangeListener(final @NonNull LogicalDatastoreType store, final @NonNull InstanceIdentifier<T>
            path, final @NonNull L listener) {

        final DOMDataTreeIdentifier domIdentifier = toDomTreeIdentifier(store, path);
        final BindingDOMDataTreeChangeListenerAdapter<T> domListener =
                listener instanceof ClusteredDataTreeChangeListener
                        ? new BindingClusteredDOMDataTreeChangeListenerAdapter<>(
                        adapterContext(), (ClusteredDataTreeChangeListener<T>) listener, store)
                        : new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), listener, store);
        final ListenerRegistration<BindingDOMDataTreeChangeListenerAdapter<T>> domReg =
                getDelegate().registerDataTreeChangeListener(domIdentifier, domListener);
        return new BindingDataTreeChangeListenerRegistration<>(listener, domReg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
        registerDataTreeChangeListener(final @NonNull LogicalDatastoreType store, final @NonNull InstanceWildcard<T>
            path, final @NonNull L listener) {
        final DOMDataTreeIdentifier domIdentifier = toDomTreeIdentifier(store, path);
        final BindingDOMDataTreeChangeListenerAdapter<T> domListener =
                listener instanceof ClusteredDataTreeChangeListener
                        ? new BindingClusteredDOMDataTreeChangeListenerAdapter<>(
                        adapterContext(), (ClusteredDataTreeChangeListener<T>) listener, store)
                        : new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), listener, store);
        final ListenerRegistration<BindingDOMDataTreeChangeListenerAdapter<T>> domReg =
                getDelegate().registerDataTreeChangeListener(domIdentifier, domListener);
        return new BindingDataTreeChangeListenerRegistration<>(listener, domReg);
    }

    private DOMDataTreeIdentifier toDomTreeIdentifier(final DataTreeIdentifier<?> treeId) {
        return new DOMDataTreeIdentifier(treeId.getDatastoreType(),
            currentSerializer().toYangInstanceIdentifier(treeId.getRootIdentifier()));
    }

    private DOMDataTreeIdentifier toDomTreeIdentifier(final LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.InstanceIdentifier<?> id) {
        return new DOMDataTreeIdentifier(store,
                currentSerializer().toYangInstanceIdentifier(id));
    }

    private DOMDataTreeIdentifier toDomTreeIdentifier(final LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.InstanceWildcard<?> id) {
        return new DOMDataTreeIdentifier(store,
                currentSerializer().toYangInstanceIdentifier(id));
    }
}
