/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

public class BindingDOMDataTreeListenerAdapter implements DOMDataTreeListener {

    private final DataTreeListener delegate;
    private final BindingToNormalizedNodeCodec codec;
    private final LogicalDatastoreType store;


    protected BindingDOMDataTreeListenerAdapter(final DataTreeListener delegate,
            final BindingToNormalizedNodeCodec codec, final LogicalDatastoreType store) {
        this.delegate = delegate;
        this.codec = codec;
        this.store = store;
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeCandidate> domChanges,
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domSubtrees) {

        final Collection<DataTreeModification<?>> changes = toBinding(domChanges);
        final Map<DataTreeIdentifier<?>, DataObject> subtrees = toBinding(domSubtrees);

        delegate.onDataTreeChanged(changes, subtrees);
    }

    private Map<DataTreeIdentifier<?>, DataObject> toBinding(
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domSubtrees) {
        // FIXME: Introduce lazy translating map
        final Map<DataTreeIdentifier<?>, DataObject> ret = new HashMap<>();
        for (final Entry<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domEntry : domSubtrees.entrySet()) {
            final Entry<InstanceIdentifier<?>, DataObject> bindingEntry =
                    codec.fromNormalizedNode(domEntry.getKey().getRootIdentifier(), domEntry.getValue());
            ret.put(new DataTreeIdentifier<>(store, bindingEntry.getKey()), bindingEntry.getValue());
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private Collection<DataTreeModification<?>> toBinding(final Collection<DataTreeCandidate> domChanges) {
        return Collection.class.cast(LazyDataTreeModification.from(codec, domChanges, store));
    }

    @Override
    public void onDataTreeFailed(final Collection<DOMDataTreeListeningException> causes) {
        // FIXME: Add cause transformation

    };
}
