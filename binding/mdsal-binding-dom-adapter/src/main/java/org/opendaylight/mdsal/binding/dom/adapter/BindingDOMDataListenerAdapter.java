/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

final class BindingDOMDataListenerAdapter<T extends DataObject> implements DOMDataTreeChangeListener {
    private final AdapterContext adapterContext;
    private final DataListener<T> listener;

    BindingDOMDataListenerAdapter(final AdapterContext adapterContext, final DataListener<T> listener) {
        this.adapterContext = requireNonNull(adapterContext);
        this.listener = requireNonNull(listener);
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
        final var last = changes.get(changes.size() - 1);
        final var after = last.getRootNode().dataAfter();
        listener.dataChangedTo(after == null ? null : deserialize(last.getRootPath(), after));
    }

    @Override
    public void onInitialData() {
        listener.dataChangedTo(null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("listener", listener).toString();
    }

    @SuppressWarnings("unchecked")
    private T deserialize(final YangInstanceIdentifier path, final @NonNull NormalizedNode data) {
        final var serializer = adapterContext.currentSerializer();
        final var codec = serializer.getSubtreeCodec(serializer.coerceInstanceIdentifier(path));
        return (T) switch (codec) {
            case BindingDataObjectCodecTreeNode<?> dataObject -> dataObject.deserialize(data);
            case BindingAugmentationCodecTreeNode<?> augmentation -> augmentation.filterFrom(data);
            default -> throw new IllegalStateException("Unhandled codec " + codec);
        };
    }
}
