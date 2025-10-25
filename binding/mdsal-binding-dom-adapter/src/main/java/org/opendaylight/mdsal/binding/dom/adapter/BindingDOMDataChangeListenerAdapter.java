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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

final class BindingDOMDataChangeListenerAdapter<T extends DataObject> implements DOMDataTreeChangeListener {
    private final AdapterContext adapterContext;
    private final DataChangeListener<T> listener;

    BindingDOMDataChangeListenerAdapter(final AdapterContext adapterContext, final DataChangeListener<T> listener) {
        this.adapterContext = requireNonNull(adapterContext);
        this.listener = requireNonNull(listener);
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
        final var first = changes.get(0);
        final var serializer = adapterContext.currentSerializer();
        final var codec = serializer.getSubtreeCodec(serializer.coerceInstanceIdentifier(first.getRootPath()));

        listener.dataChanged(deserialize(codec, first.getRootNode().dataBefore()),
            deserialize(codec, changes.getLast().getRootNode().dataAfter()));
    }

    @Override
    public void onInitialData() {
        listener.dataChanged(null, null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("listener", listener).toString();
    }

    @SuppressWarnings("unchecked")
    private @Nullable T deserialize(final CommonDataObjectCodecTreeNode<?> codec, final NormalizedNode data) {
        if (data == null) {
            return null;
        }

        return (T) switch (codec) {
            case BindingDataObjectCodecTreeNode<?> dataObject -> dataObject.deserialize(data);
            case BindingAugmentationCodecTreeNode<?> augmentation -> augmentation.filterFrom(data);
            default -> throw new IllegalStateException("Unhandled codec " + codec);
        };
    }
}
