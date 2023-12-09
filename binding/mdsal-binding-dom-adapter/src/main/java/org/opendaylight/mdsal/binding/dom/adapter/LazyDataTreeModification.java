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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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
        rootNode = requireNonNull(modification);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends DataObject> @Nullable DataTreeModification<T> from(final CurrentAdapterSerializer serializer,
            final DataTreeCandidate domChange, final LogicalDatastoreType datastoreType, final Class<T> augment) {
        final var bindingPath = createBindingPath(serializer, domChange.getRootPath(), augment);
        final var codec = serializer.getSubtreeCodec(bindingPath);
        final var modification = LazyDataObjectModification.from(codec, domChange.getRootNode());
        return modification == null ? null
            : new LazyDataTreeModification(DataTreeIdentifier.of(datastoreType, bindingPath), modification);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends DataObject> @Nullable DataTreeModification<T> from(final CurrentAdapterSerializer serializer,
            final DOMDataTreeCandidate candidate, final Class<T> augment) {
        final var domRootPath = candidate.getRootPath();
        final var bindingPath = createBindingPath(serializer, domRootPath.path(), augment);
        final var codec = serializer.getSubtreeCodec(bindingPath);
        final var modification = LazyDataObjectModification.from(codec, candidate.getRootNode());
        return modification == null ? null
            : new LazyDataTreeModification(DataTreeIdentifier.of(domRootPath.path(), bindingPath), modification);
    }

    static <T extends DataObject> @NonNull List<DataTreeModification<T>> from(final CurrentAdapterSerializer codec,
            final List<DataTreeCandidate> domChanges, final LogicalDatastoreType datastoreType,
            final Class<T> augment) {
        final var result = new ArrayList<DataTreeModification<T>>(domChanges.size());
        for (var domChange : domChanges) {
            final var bindingChange = from(codec, domChange, datastoreType, augment);
            if (bindingChange != null) {
                result.add(bindingChange);
            }
        }
        return result;
    }

    // We are given a DOM path, which does not reflect augmentations, as those are not representable in NormalizedNode
    // world. This method takes care of reconstructing the InstanceIdentifier, appending the missing Augmentation. This
    // important to get the correct codec into the mix -- otherwise we would be operating on the parent container's
    // codec and mis-report what is actually going on.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @NonNull InstanceIdentifier<?> createBindingPath(final CurrentAdapterSerializer serializer,
            final YangInstanceIdentifier domPath, final Class<?> augment) {
        final var bindingPath = serializer.coerceInstanceIdentifier(domPath);
        return augment == null ? bindingPath : bindingPath.augmentation((Class) augment.asSubclass(Augmentation.class));
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
