/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.modification;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeModification;
import org.opendaylight.mdsal.binding.javav2.api.TreeNodeModification;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * Lazily translated {@link DataTreeModification} based on {@link DataTreeCandidate}.
 *
 * <p>
 * {@link DataTreeModification} represents Data tree change event, but whole tree is not translated or
 * resolved eagerly, but only child nodes which are directly accessed by user of tree node modification.
 *
 */
@Beta
public final class LazyDataTreeModification<T extends TreeNode> implements DataTreeModification<T> {

    private final DataTreeIdentifier<T> path;
    private final TreeNodeModification<T> rootNode;

    private LazyDataTreeModification(final DataTreeIdentifier<T> path, final TreeNodeModification<T> modification) {
        this.path = requireNonNull(path);
        this.rootNode = requireNonNull(modification);
    }

    @Nonnull
    @Override
    public TreeNodeModification<T> getRootNode() {
        return rootNode;
    }

    @Nonnull
    @Override
    public DataTreeIdentifier<T> getRootPath() {
        return path;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T extends TreeNode> DataTreeModification<T> create(final BindingToNormalizedNodeCodec codec,
            final DataTreeCandidate domChange, final LogicalDatastoreType datastoreType) {
        final Entry<InstanceIdentifier<?>, BindingTreeNodeCodec<?>> codecCtx =
                codec.getSubtreeCodec(domChange.getRootPath());
        final DataTreeIdentifier<?> path = DataTreeIdentifier.create(datastoreType, codecCtx.getKey());
        final TreeNodeModification<?> modification =
                LazyTreeNodeModification.create(codecCtx.getValue(), domChange.getRootNode());
        return new LazyDataTreeModification(path, modification);
    }

    /**
     * Create instance of Binding date tree modification according to DOM candidate of changes.
     *
     * @param codec
     *            - codec for modificated data
     * @param candidate
     *            - changted DOM data
     * @return modificated data tree
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends TreeNode> DataTreeModification<T> create(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeCandidate candidate) {
        final Entry<InstanceIdentifier<?>, BindingTreeNodeCodec<?>> codecCtx =
                codec.getSubtreeCodec(candidate.getRootPath().getRootIdentifier());
        final DataTreeIdentifier<?> path =
                DataTreeIdentifier.create(candidate.getRootPath().getDatastoreType(), codecCtx.getKey());
        final TreeNodeModification<?> modification =
                LazyTreeNodeModification.create(codecCtx.getValue(), candidate.getRootNode());
        return new LazyDataTreeModification(path, modification);
    }

    /**
     * DOM data changes to new Binding data.
     *
     * @param codec
     *            - Binding to DOM codec
     * @param domChanges
     *            - DOM data changes
     * @param datastoreType
     *            - datastore type
     * @return collection of new Binding data according to DOM data changes
     */
    public static <T extends TreeNode> Collection<DataTreeModification<T>> from(
            final BindingToNormalizedNodeCodec codec, final Collection<DataTreeCandidate> domChanges,
            final LogicalDatastoreType datastoreType) {
        final List<DataTreeModification<T>> result = new ArrayList<>(domChanges.size());
        for (final DataTreeCandidate domChange : domChanges) {
            result.add(LazyDataTreeModification.create(codec, domChange, datastoreType));
        }
        return result;
    }

}

