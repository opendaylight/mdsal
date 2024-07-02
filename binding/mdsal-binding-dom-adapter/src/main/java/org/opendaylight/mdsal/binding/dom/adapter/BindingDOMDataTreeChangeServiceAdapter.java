/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.DataChangeListener;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;

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
    public <T extends DataObject> Registration registerTreeChangeListener(final LogicalDatastoreType datastore,
            final DataObjectReference<T> subtrees, final DataTreeChangeListener<T> listener) {
        return getDelegate().registerTreeChangeListener(toDomTreeIdentifier(datastore, subtrees),
            new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), datastore, subtrees, listener));
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public <T extends DataObject> Registration registerLegacyTreeChangeListener(final LogicalDatastoreType datastore,
            final DataObjectReference<T> subtrees, final DataTreeChangeListener<T> listener) {
        return getDelegate().registerLegacyTreeChangeListener(toDomTreeIdentifier(datastore, subtrees),
            new BindingDOMDataTreeChangeListenerAdapter<>(adapterContext(), datastore, subtrees, listener));
    }

    @Override
    public <T extends DataObject> Registration registerDataListener(final LogicalDatastoreType datastore,
            final DataObjectIdentifier<T> path, final DataListener<T> listener) {
        return getDelegate().registerTreeChangeListener(toDomTreeIdentifier(datastore, path),
            new BindingDOMDataListenerAdapter<>(adapterContext(), listener));
    }

    @Override
    public <T extends DataObject> Registration registerDataChangeListener(final LogicalDatastoreType datastore,
            final DataObjectIdentifier<T> path, final DataChangeListener<T> listener) {
        return getDelegate().registerTreeChangeListener(toDomTreeIdentifier(datastore, path),
            new BindingDOMDataChangeListenerAdapter<>(adapterContext(), listener));
    }

    @NonNullByDefault
    private DOMDataTreeIdentifier toDomTreeIdentifier(final LogicalDatastoreType datastore,
            final DataObjectReference<?> subtrees) {
        return DOMDataTreeIdentifier.of(datastore, currentSerializer().toYangInstanceIdentifier(subtrees));
    }
}
