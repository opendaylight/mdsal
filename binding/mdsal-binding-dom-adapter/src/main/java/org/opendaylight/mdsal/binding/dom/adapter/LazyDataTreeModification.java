/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

/**
 * Lazily translated {@link DataTreeModification} based on {@link DataTreeCandidate}.
 *
 * <p>
 * {@link DataTreeModification} represents Data tree change event, but whole tree is not translated or resolved eagerly,
 * but only child nodes which are directly accessed by user of data object modification.
 */
final class LazyDataTreeModification<T extends DataObject> implements DataTreeModification<T> {
    private final @NonNull DataTreeIdentifier<T> path;
    private final @NonNull DataObjectModification<T> rootNode;

    private LazyDataTreeModification(final DataTreeIdentifier<T> path, final DataObjectModification<T> modification) {
        this.path = requireNonNull(path);
        this.rootNode = requireNonNull(modification);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends DataObject> DataTreeModification<T> create(final CurrentAdapterSerializer serializer,
            final DataTreeCandidate domChange, final LogicalDatastoreType datastoreType) {
        final InstanceIdentifier<?> bindingPath = serializer.coerceInstanceIdentifier(domChange.getRootPath());
        final BindingDataObjectCodecTreeNode<?> codec = serializer.getSubtreeCodec(bindingPath);
        final DataTreeIdentifier<?> path = DataTreeIdentifier.create(datastoreType, bindingPath);
        return new LazyDataTreeModification(path, LazyDataObjectModification.create(codec, domChange.getRootNode()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends DataObject> DataTreeModification<T> create(final CurrentAdapterSerializer serializer,
            final DOMDataTreeCandidate candidate) {
        final DOMDataTreeIdentifier domRootPath = candidate.getRootPath();
        final InstanceIdentifier<?> bindingPath = serializer.coerceInstanceIdentifier(domRootPath.getRootIdentifier());
        final BindingDataObjectCodecTreeNode<?> codec = serializer.getSubtreeCodec(bindingPath);
        return new LazyDataTreeModification(DataTreeIdentifier.create(domRootPath.getDatastoreType(), bindingPath),
            LazyDataObjectModification.create(codec, candidate.getRootNode()));
    }

    static <T extends DataObject> @NonNull List<DataTreeModification<T>> from(final CurrentAdapterSerializer codec,
            final List<DataTreeCandidate> domChanges, final LogicalDatastoreType datastoreType) {
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
    public LogicalDatastoreType datastore() {
        return path.getDatastoreType();
    }

    @Override
    public org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> rootPath() {
        return org.opendaylight.mdsal.binding.api.InstanceIdentifier.ofLegacy(path.getRootIdentifier());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("path", path).add("rootNode", rootNode).toString();
    }
}
