/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * Lazily translated {@link DataTreeModification} based on {@link DataTreeCandidate}.
 *
 * <p>
 * {@link DataTreeModification} represents Data tree change event,
 * but whole tree is not translated or resolved eagerly, but only child nodes
 * which are directly accessed by user of data object modification.
 *
 */
class LazyDataTreeModification<T extends DataObject> implements DataTreeModification<T> {

    private final DataTreeIdentifier<T> path;
    private final DataObjectModification<T> rootNode;

    LazyDataTreeModification(final DataTreeIdentifier<T> path, final DataObjectModification<T> modification) {
        this.path = Preconditions.checkNotNull(path);
        this.rootNode = Preconditions.checkNotNull(modification);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends DataObject> DataTreeModification<T> create(final BindingToNormalizedNodeCodec codec,
            final DataTreeCandidate domChange, final LogicalDatastoreType datastoreType) {
        final Entry<InstanceIdentifier<?>, BindingCodecTreeNode<?>> codecCtx =
                codec.getSubtreeCodec(domChange.getRootPath());
        final DataTreeIdentifier<?> path = DataTreeIdentifier.create(datastoreType, codecCtx.getKey());
        final DataObjectModification<?> modification =
                LazyDataObjectModification.create(codecCtx.getValue(), domChange.getRootNode());
        return new LazyDataTreeModification(path, modification);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends DataObject> DataTreeModification<T> create(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeCandidate candidate) {
        final Entry<InstanceIdentifier<?>, BindingCodecTreeNode<?>> codecCtx =
                codec.getSubtreeCodec(candidate.getRootPath().getRootIdentifier());
        final DataTreeIdentifier<?> path =
                DataTreeIdentifier.create(candidate.getRootPath().getDatastoreType(), codecCtx.getKey());
        final DataObjectModification<?> modification =
                LazyDataObjectModification.create(codecCtx.getValue(), candidate.getRootNode());
        return new LazyDataTreeModification(path, modification);
    }

    static <T extends DataObject> Collection<DataTreeModification<T>> from(final BindingToNormalizedNodeCodec codec,
            final Collection<DataTreeCandidate> domChanges, final LogicalDatastoreType datastoreType) {
        final List<DataTreeModification<T>> result = new ArrayList<>(domChanges.size());
        for (final DataTreeCandidate domChange : domChanges) {
            result.add(LazyDataTreeModification.create(codec, domChange, datastoreType));
        }
        return result;
    }

    @Override
    public DataObjectModification<T> getRootNode() {
        return rootNode;
    }

    @Override
    public DataTreeIdentifier<T> getRootPath() {
        return path;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("path", path).add("rootNode", rootNode).toString();
    }
}
