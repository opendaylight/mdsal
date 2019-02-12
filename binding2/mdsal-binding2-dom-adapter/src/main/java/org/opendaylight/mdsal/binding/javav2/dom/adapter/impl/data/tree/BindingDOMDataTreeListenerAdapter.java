/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeListener;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeListeningException;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeModification;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.modification.LazyDataTreeModification;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * Data tree listener adapter.
 */
@Beta
public class BindingDOMDataTreeListenerAdapter implements DOMDataTreeListener {

    private final DataTreeListener delegate;
    private final BindingToNormalizedNodeCodec codec;
    private final LogicalDatastoreType store;

    public BindingDOMDataTreeListenerAdapter(final DataTreeListener delegate,
            final BindingToNormalizedNodeCodec codec, final LogicalDatastoreType store) {
        this.delegate = requireNonNull(delegate, "delegate");
        this.codec = requireNonNull(codec, "codec");
        this.store = requireNonNull(store, "store");
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeCandidate> domChanges,
            @Nonnull final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domSubtrees) {

        final Collection<DataTreeModification<?>> changes = toBinding(domChanges);
        final Map<DataTreeIdentifier<?>, TreeNode> subtrees = toBinding(domSubtrees);

        delegate.onDataTreeChanged(changes, subtrees);
    }

    private Map<DataTreeIdentifier<?>, TreeNode>
            toBinding(final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domSubtrees) {
        // FIXME: Introduce lazy translating map
        final Map<DataTreeIdentifier<?>, TreeNode> ret = Maps.newHashMapWithExpectedSize(domSubtrees.size());
        for (final Entry<DOMDataTreeIdentifier, NormalizedNode<?, ?>> domEntry : domSubtrees.entrySet()) {
            final Entry<InstanceIdentifier<?>, TreeNode> bindingEntry =
                    codec.fromNormalizedNode(domEntry.getKey().getRootIdentifier(), domEntry.getValue());
            ret.put(DataTreeIdentifier.create(store, bindingEntry.getKey()), bindingEntry.getValue());
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private Collection<DataTreeModification<?>> toBinding(final Collection<DataTreeCandidate> domChanges) {
        return Collection.class.cast(LazyDataTreeModification.from(codec, domChanges, store));
    }

    @Override
    public void onDataTreeFailed(@Nonnull final Collection<DOMDataTreeListeningException> causes) {
        final List<DataTreeListeningException> bindingCauses = new ArrayList<>(causes.size());
        for (final DOMDataTreeListeningException cause : causes) {
            bindingCauses.add(mapException(cause));
        }

        delegate.onDataTreeFailed(bindingCauses);
    }

    private static DataTreeListeningException mapException(final DOMDataTreeListeningException cause) {
        // FIXME: Extend logic
        return new DataTreeListeningException(cause.getMessage(), cause);
    }
}

