/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.VerifyException;
import com.google.common.util.concurrent.FluentFuture;
import java.util.HashSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer.AugmentationResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer.NodeResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer.NormalizedResult;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

class BindingDOMWriteTransactionAdapter<T extends DOMDataTreeWriteTransaction> extends AbstractForwardedTransaction<T>
        implements WriteTransaction {
    BindingDOMWriteTransactionAdapter(final AdapterContext adapterContext, final T delegateTx) {
        super(adapterContext, delegateTx);
    }

    @Override
    public final <U extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data) {
        put(store, toNormalized("put", path, data));
    }

    private void put(final LogicalDatastoreType store, final NormalizedResult normalized) {
        final var delegate = getDelegate();
        final var domPath = normalized.path();

        if (normalized instanceof AugmentationResult augment) {
            // Augmentation: put() child nodes provided with augmentation, delete() those having no data
            final var putIds = new HashSet<YangInstanceIdentifier.PathArgument>();
            for (var child : augment.children()) {
                final var childId = child.name();
                delegate.put(store, domPath.node(childId), child);
                putIds.add(childId);
            }
            for (var childId : augment.possibleChildren()) {
                if (!putIds.contains(childId)) {
                    delegate.delete(store, domPath.node(childId));
                }
            }
        } else if (normalized instanceof NodeResult node) {
            delegate.put(store, domPath, node.node());
        } else {
            throw new VerifyException("Unhandled result " + normalized);
        }
    }

    @Deprecated
    @Override
    public final <U extends DataObject> void mergeParentStructurePut(final LogicalDatastoreType store,
            final InstanceIdentifier<U> path, final U data) {
        final var serializer = adapterContext().currentSerializer();
        final var normalized = toNormalized(serializer, "put", path, data);
        ensureParentsByMerge(serializer, store, normalized);
        put(store, normalized);
    }

    @Override
    public final <D extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<D> path,
            final D data) {
        merge(store, toNormalized("merge", path, data));
    }

    private void merge(final LogicalDatastoreType store, final NormalizedResult normalized) {
        final var delegate = getDelegate();
        final var domPath = normalized.path();

        if (normalized instanceof AugmentationResult augment) {
            // Augmentation: merge individual children
            for (var child : augment.children()) {
                delegate.merge(store, domPath.node(child.name()), child);
            }
        } else if (normalized instanceof NodeResult node) {
            delegate.merge(store, domPath, node.node());
        } else {
            throw new VerifyException("Unhandled result " + normalized);
        }
    }

    @Deprecated
    @Override
    public final <U extends DataObject> void mergeParentStructureMerge(final LogicalDatastoreType store,
            final InstanceIdentifier<U> path, final U data) {
        final var serializer = adapterContext().currentSerializer();
        final var normalized = toNormalized(serializer, "merge", path, data);
        ensureParentsByMerge(serializer, store, normalized);
        merge(store, normalized);
    }

    @Override
    public final void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        checkArgument(!path.isWildcarded(), "Cannot delete wildcarded path %s", path);
        final var serializer = adapterContext().currentSerializer();

        // Lookup the codec and the corresponding path
        final var codecWithPath = serializer.getSubtreeCodecWithPath(path);
        final var domPath = codecWithPath.path();
        final var delegate = getDelegate();
        if (codecWithPath.codec() instanceof BindingAugmentationCodecTreeNode<?> augmentCodec) {
            // Deletion of an augmentation: issue a delete on all potential children of the augmentation
            for (var childPath : augmentCodec.childPathArguments()) {
                delegate.delete(store, domPath.node(childPath));
            }
        } else {
            delegate.delete(store, domPath);
        }
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        return getDelegate().commit();
    }

    @Override
    public final boolean cancel() {
        return getDelegate().cancel();
    }

    @Override
    public FluentFuture<?> completionFuture() {
        return getDelegate().completionFuture();
    }

    /**
     * Subclasses of this class are required to implement creation of parent nodes based on behaviour of their
     * underlying transaction.
     *
     * @param serializer Current serializer
     * @param store an instance of LogicalDatastoreType
     * @param normalized NormalizedResult of the operation
     */
    private void ensureParentsByMerge(final CurrentAdapterSerializer serializer, final LogicalDatastoreType store,
            final NormalizedResult normalized) {
        final var path = normalized.path();
        // AugmentationResult already points to parent path
        final var parentPath = normalized instanceof AugmentationResult ? path : path.getParent();
        if (parentPath != null && !parentPath.isEmpty()) {
            final var parentNode = ImmutableNodes.fromInstanceId(serializer.getRuntimeContext().modelContext(),
                parentPath);
            getDelegate().merge(store, YangInstanceIdentifier.of(parentNode.name()), parentNode);
        }
    }

    private <U extends DataObject> @NonNull NormalizedResult toNormalized(final String operation,
            final InstanceIdentifier<U> path, final U data) {
        return toNormalized(adapterContext().currentSerializer(), operation, path, data);
    }

    private static <U extends DataObject> @NonNull NormalizedResult toNormalized(
            final CurrentAdapterSerializer serializer, final String operation, final InstanceIdentifier<U> path,
            final U data) {
        checkArgument(!path.isWildcarded(), "Cannot %s data into wildcarded path %s", operation, path);
        return serializer.toNormalizedNode(path, data);
    }
}
