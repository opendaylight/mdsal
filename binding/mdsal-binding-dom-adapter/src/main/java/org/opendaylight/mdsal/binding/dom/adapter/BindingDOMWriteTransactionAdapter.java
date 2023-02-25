/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.codec.impl.AugmentationNodeContext;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

class BindingDOMWriteTransactionAdapter<T extends DOMDataTreeWriteTransaction> extends AbstractForwardedTransaction<T>
        implements WriteTransaction {
    BindingDOMWriteTransactionAdapter(final AdapterContext adapterContext, final T delegateTx) {
        super(adapterContext, delegateTx);
    }

    @Override
    public final <U extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data) {
        if (isAugmentation(path)) {
            /* augmentation only compatibility case:
             put child nodes provided with augmentation, remove those having no data
             */
            final var parentNodeIdentifier = adapterContext().currentSerializer().toYangInstanceIdentifier(path);
            final var codec = adapterContext().currentSerializer().getSubtreeCodec(path);
            if (data instanceof Augmentation<?> && codec instanceof AugmentationNodeContext augmCodec) {
                final var augmNode = (ContainerNode) augmCodec.serializeToContainerNode(data,
                    parentNodeIdentifier.getLastPathArgument());
                final Set<YangInstanceIdentifier.PathArgument> childPaths = augmCodec.getChildPaths();
                final var childMap = augmNode.body().stream().collect(
                    ImmutableMap.toImmutableMap(node -> node.getIdentifier(), node -> node));
                for (var childPath : childPaths) {
                    if (!childMap.containsKey(childPath)) {
                        getDelegate().delete(store, parentNodeIdentifier.node(childPath));
                    }
                }
                for (var entry : childMap.entrySet()) {
                    getDelegate().put(store, parentNodeIdentifier.node(entry.getKey()), entry.getValue());
                }
            } else {
                throw new IllegalStateException("Unexpected augmentation codec " + codec.getBindingClass());
            }
        } else {
            final Entry<YangInstanceIdentifier, NormalizedNode> entry = toNormalized("put", path, data);
            getDelegate().put(store, entry.getKey(), entry.getValue());
        }
    }

    @Deprecated
    @Override
    public final <U extends DataObject> void mergeParentStructurePut(final LogicalDatastoreType store,
            final InstanceIdentifier<U> path, final U data) {
        final CurrentAdapterSerializer serializer = adapterContext().currentSerializer();
        final Entry<YangInstanceIdentifier, NormalizedNode> normalized = toNormalized(serializer, "put", path, data);
        ensureParentsByMerge(serializer, store, normalized.getKey(), path);
        getDelegate().put(store, normalized.getKey(), normalized.getValue());
    }

    @Override
    public final <D extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<D> path,
            final D data) {
        final Entry<YangInstanceIdentifier, NormalizedNode> normalized = toNormalized("merge", path, data);
        getDelegate().merge(store, normalized.getKey(), normalized.getValue());
    }

    @Deprecated
    @Override
    public final <U extends DataObject> void mergeParentStructureMerge(final LogicalDatastoreType store,
            final InstanceIdentifier<U> path, final U data) {
        final CurrentAdapterSerializer serializer = adapterContext().currentSerializer();
        final Entry<YangInstanceIdentifier, NormalizedNode> normalized = toNormalized(serializer, "merge", path, data);
        ensureParentsByMerge(serializer, store, normalized.getKey(), path);
        getDelegate().merge(store, normalized.getKey(), normalized.getValue());
    }

    @Override
    public final void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        checkArgument(!path.isWildcarded(), "Cannot delete wildcarded path %s", path);
        if (isAugmentation(path)) {
            // augmentation only compatibility case: remove all children belonging to augmentation
            final var parentNodeIdentifier = adapterContext().currentSerializer().toYangInstanceIdentifier(path);
            final var codec = adapterContext().currentSerializer().getSubtreeCodec(path);
            if (codec instanceof AugmentationNodeContext augmCodec) {
                final Set<YangInstanceIdentifier.PathArgument> childPaths = augmCodec.getChildPaths();
                for (var childPath : childPaths) {
                    getDelegate().delete(store, parentNodeIdentifier.node(childPath));
                }
            } else {
                throw new IllegalStateException("Unexpected augmentation codec " + codec.getBindingClass());
            }
        } else {
            getDelegate().delete(store, adapterContext().currentSerializer().toYangInstanceIdentifier(path));
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

    /**
     * Subclasses of this class are required to implement creation of parent nodes based on behaviour of their
     * underlying transaction.
     *
     * @param store an instance of LogicalDatastoreType
     * @param domPath an instance of YangInstanceIdentifier
     * @param path an instance of InstanceIdentifier
     */
    private void ensureParentsByMerge(final CurrentAdapterSerializer serializer, final LogicalDatastoreType store,
            final YangInstanceIdentifier domPath, final InstanceIdentifier<?> path) {
        final YangInstanceIdentifier parentPath = domPath.getParent();
        if (parentPath != null && !parentPath.isEmpty()) {
            final NormalizedNode parentNode = ImmutableNodes.fromInstanceId(
                serializer.getRuntimeContext().getEffectiveModelContext(), parentPath);
            getDelegate().merge(store, YangInstanceIdentifier.create(parentNode.getIdentifier()), parentNode);
        }
    }

    private <U extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode> toNormalized(final String operation,
            final InstanceIdentifier<U> path, final U data) {
        return toNormalized(adapterContext().currentSerializer(), operation, path, data);
    }

    private static <U extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode> toNormalized(
            final CurrentAdapterSerializer serializer, final String operation, final InstanceIdentifier<U> path,
            final U data) {
        checkArgument(!path.isWildcarded(), "Cannot %s data into wildcarded path %s", operation, path);
        return serializer.toNormalizedNode(path, data);
    }
}
