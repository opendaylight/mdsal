/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.DataChangeListener;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

final class BindingDOMDataChangeListenerAdapter<T extends DataObject> implements ClusteredDOMDataTreeChangeListener {
    private final AdapterContext adapterContext;
    private final DataChangeListener<T> listener;

    BindingDOMDataChangeListenerAdapter(final AdapterContext adapterContext, final DataChangeListener<T> listener) {
        this.adapterContext = requireNonNull(adapterContext);
        this.listener = requireNonNull(listener);
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
        final DataTreeCandidate first = changes.get(0);
        final CurrentAdapterSerializer serializer = adapterContext.currentSerializer();
        final BindingDataObjectCodecTreeNode<DataObject> codec = serializer.getSubtreeCodec(
                serializer.coerceInstanceIdentifier(first.getRootPath()));

        listener.dataChanged(deserialize(codec, first.getRootNode().getDataBefore()),
            deserialize(codec, changes.get(changes.size() - 1).getRootNode().getDataAfter()));
    }

    @Override
    public void onInitialData() {
        listener.dataChanged(null, null);
    }

    @SuppressWarnings("unchecked")
    private T deserialize(final BindingDataObjectCodecTreeNode<?> codec, final Optional<NormalizedNode> data) {
        return (T) data.map(codec::deserialize).orElse(null);
    }
}
