/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeListener;
import org.opendaylight.mdsal.binding.api.DataTreeListeningException;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

public class BindingDOMDataTreeListenerAdapter extends AbstractBindingAdapter<DataTreeListener>
        implements DOMDataTreeListener {

    private final LogicalDatastoreType store;

    protected BindingDOMDataTreeListenerAdapter(final DataTreeListener delegate,
            final BindingToNormalizedNodeCodec codec, final LogicalDatastoreType store) {
        super(codec, delegate);
        this.store = Preconditions.checkNotNull(store, "store");
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeCandidate> domChanges,
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domSubtrees) {

        final Collection<DataTreeModification<?>> changes = toBinding(domChanges);
        final Map<DataTreeIdentifier<?>, DataObject> subtrees = toBinding(domSubtrees);

        getDelegate().onDataTreeChanged(changes, subtrees);
    }

    private Map<DataTreeIdentifier<?>, DataObject> toBinding(
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domSubtrees) {
        // FIXME: Introduce lazy translating map
        final Map<DataTreeIdentifier<?>, DataObject> ret = Maps.newHashMapWithExpectedSize(domSubtrees.size());
        for (final Entry<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domEntry : domSubtrees.entrySet()) {
            final Entry<InstanceIdentifier<?>, DataObject> bindingEntry =
                    getCodec().fromNormalizedNode(domEntry.getKey().getRootIdentifier(), domEntry.getValue());
            ret.put(DataTreeIdentifier.create(store, bindingEntry.getKey()), bindingEntry.getValue());
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private Collection<DataTreeModification<?>> toBinding(final Collection<DataTreeCandidate> domChanges) {
        return Collection.class.cast(LazyDataTreeModification.from(getCodec(), domChanges, store));
    }

    @Override
    public void onDataTreeFailed(final Collection<DOMDataTreeListeningException> causes) {
        List<DataTreeListeningException> bindingCauses = new ArrayList<>(causes.size());
        for (DOMDataTreeListeningException cause : causes) {
            bindingCauses.add(mapException(cause));
        }

        getDelegate().onDataTreeFailed(bindingCauses);
    }

    private static DataTreeListeningException mapException(final DOMDataTreeListeningException cause) {
        // FIXME: Extend logic
        return new DataTreeListeningException(cause.getMessage(), cause);
    }
}
