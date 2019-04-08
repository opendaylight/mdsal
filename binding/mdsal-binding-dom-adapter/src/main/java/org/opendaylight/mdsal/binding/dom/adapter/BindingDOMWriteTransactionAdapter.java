/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class BindingDOMWriteTransactionAdapter<T extends DOMDataTreeWriteTransaction> extends AbstractForwardedTransaction<T>
        implements WriteTransaction {
    BindingDOMWriteTransactionAdapter(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        super(delegateTx, codec);
    }

    @Override
    public final <U extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = toNormalized("put", path, data);
        getDelegate().put(store, normalized.getKey(), normalized.getValue());
    }

    @Override
    public final <U extends DataObject> void mergeParentStructurePut(@NonNull LogicalDatastoreType store,
            @NonNull InstanceIdentifier<U> path, @NonNull U data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = toNormalized("put",
            path, data);
        ensureParentsByMerge(store, normalized.getKey(), path);
        getDelegate().put(store, normalized.getKey(), normalized.getValue());
    }

    @Override
    public final <D extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<D> path,
            final D data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = toNormalized("merge", path, data);
        getDelegate().merge(store, normalized.getKey(), normalized.getValue());
    }

    @Override
    public final <U extends DataObject> void mergeParentStructureMerge(@NonNull LogicalDatastoreType store,
            @NonNull InstanceIdentifier<U> path, @NonNull U data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = toNormalized("merge", path, data);
        ensureParentsByMerge(store, normalized.getKey(), path);
        getDelegate().merge(store, normalized.getKey(), normalized.getValue());
    }

    @Override
    public final void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        checkArgument(!path.isWildcarded(), "Cannot delete wildcarded path %s", path);

        final YangInstanceIdentifier normalized = getCodec().toYangInstanceIdentifierBlocking(path);
        getDelegate().delete(store, normalized);
    }

    @Override
    public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
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
    private void ensureParentsByMerge(final LogicalDatastoreType store, final YangInstanceIdentifier domPath,
            final InstanceIdentifier<?> path) {
        final YangInstanceIdentifier parentPath = domPath.getParent();
        if (parentPath != null) {
            final NormalizedNode<?, ?> parentNode = getCodec().instanceIdentifierToNode(parentPath);
            getDelegate().merge(store, YangInstanceIdentifier.create(parentNode.getIdentifier()), parentNode);
        }
    }

    private <U extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalized(
            final String operation, final InstanceIdentifier<U> path, final U data) {
        checkArgument(!path.isWildcarded(), "Cannot %s data into wildcarded path %s", operation, path);
        return getCodec().toNormalizedNode(path, data);
    }
}
