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
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

final class BindingDOMDataListenerAdapter<T extends DataObject> implements ClusteredDOMDataTreeChangeListener {
    private final AdapterContext adapterContext;
    private final DataListener<T> listener;

    BindingDOMDataListenerAdapter(final AdapterContext adapterContext, final DataListener<T> listener) {
        this.adapterContext = requireNonNull(adapterContext);
        this.listener = requireNonNull(listener);
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
        final var last = changes.get(changes.size() - 1);
        final var after = last.getRootNode().getDataAfter();
        listener.dataChangedTo(after.isEmpty() ? null : deserialize(last.getRootPath(), after.orElseThrow()));
    }

    @Override
    public void onInitialData() {
        listener.dataChangedTo(null);
    }

    @SuppressWarnings("unchecked")
    private T deserialize(final YangInstanceIdentifier path, final NormalizedNode data) {
        final var serializer = adapterContext.currentSerializer();
        return (T) serializer.getSubtreeCodec(serializer.coerceInstanceIdentifier(path)).deserialize(data);
    }
}
