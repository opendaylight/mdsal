/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Adapter exposing Binding {@link DataTreeChangeService} and wrapping a {@link DOMDataTreeChangeService} and is
 * responsible for translation and instantiation of {@link BindingDOMDataTreeChangeListenerAdapter} adapters.
 *
 * <p>
 * Each registered {@link DataTreeChangeListener} is wrapped using adapter and registered directly to DOM service.
 */
final class BindingDOMDataTreeChangeServiceAdapter extends AbstractBindingAdapter<DOMDataTreeChangeService>
        implements DataTreeChangeService {

    private BindingDOMDataTreeChangeServiceAdapter(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeChangeService dataTreeChangeService) {
        super(codec, dataTreeChangeService);
    }

    static DataTreeChangeService create(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeChangeService dataTreeChangeService) {
        return new BindingDOMDataTreeChangeServiceAdapter(codec, dataTreeChangeService);
    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
            registerDataTreeChangeListener(final DataTreeIdentifier<T> treeId, final L listener) {
        final DOMDataTreeIdentifier domIdentifier = toDomTreeIdentifier(treeId);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final BindingDOMDataTreeChangeListenerAdapter<T> domListener =
                listener instanceof ClusteredDataTreeChangeListener
                        ? new BindingClusteredDOMDataTreeChangeListenerAdapter<>(
                                getCodec(), (ClusteredDataTreeChangeListener) listener, treeId.getDatastoreType())
                        : new BindingDOMDataTreeChangeListenerAdapter<>(getCodec(), listener, treeId.getDatastoreType());

        final ListenerRegistration<BindingDOMDataTreeChangeListenerAdapter<T>> domReg =
                getDelegate().registerDataTreeChangeListener(domIdentifier, domListener);
        return new BindingDataTreeChangeListenerRegistration<>(listener, domReg);
    }

    private DOMDataTreeIdentifier toDomTreeIdentifier(final DataTreeIdentifier<?> treeId) {
        final YangInstanceIdentifier domPath = getCodec().toYangInstanceIdentifierBlocking(treeId.getRootIdentifier());
        return new DOMDataTreeIdentifier(treeId.getDatastoreType(), domPath);
    }
}
