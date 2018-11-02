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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract Base Transaction for transactions which are backed by {@link DOMDataTreeWriteTransaction}.
 */
// FIXME: 4.0.0: hide this class and merge it with BindingDOMWriteTransactionAdapter
public abstract class AbstractWriteTransaction<T extends DOMDataTreeWriteTransaction> extends
        AbstractForwardedTransaction<T> {

    protected AbstractWriteTransaction(final T delegate, final BindingToNormalizedNodeCodec codec) {
        super(delegate, codec);
    }

    public final <U extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data, final boolean createParents) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = toNormalized("put", path, data);
        if (createParents) {
            ensureParentsByMerge(store, normalized.getKey(), path);
        }

        getDelegate().put(store, normalized.getKey(), normalized.getValue());
    }

    public final <U extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data,final boolean createParents) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = toNormalized("merge", path, data);
        if (createParents) {
            ensureParentsByMerge(store, normalized.getKey(), path);
        }

        getDelegate().merge(store, normalized.getKey(), normalized.getValue());
    }

    private <U extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalized(
            final String operation, final InstanceIdentifier<U> path, final U data) {
        checkArgument(!path.isWildcarded(), "Cannot %s data into wildcarded path %s", operation, path);
        return getCodec().toNormalizedNode(path, data);
    }

    /**
     * Use {@link YangInstanceIdentifier#getParent()} instead.
     */
    @Deprecated
    protected static Optional<YangInstanceIdentifier> getParent(final YangInstanceIdentifier child) {
        return Optional.ofNullable(child.getParent());
    }

    /**
     * Subclasses of this class are required to implement creation of parent nodes based on behaviour of their
     * underlying transaction.
     *
     * @param store an instance of LogicalDatastoreType
     * @param domPath an instance of YangInstanceIdentifier
     * @param path an instance of InstanceIdentifier
     */
    protected final void ensureParentsByMerge(final LogicalDatastoreType store, final YangInstanceIdentifier domPath,
            final InstanceIdentifier<?> path) {
        final YangInstanceIdentifier parentPath = domPath.getParent();
        if (parentPath != null) {
            final NormalizedNode<?, ?> parentNode = getCodec().instanceIdentifierToNode(parentPath);
            getDelegate().merge(store, YangInstanceIdentifier.create(parentNode.getIdentifier()), parentNode);
        }
    }

    protected final void doDelete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        checkArgument(!path.isWildcarded(), "Cannot delete wildcarded path %s", path);

        final YangInstanceIdentifier normalized = getCodec().toYangInstanceIdentifierBlocking(path);
        getDelegate().delete(store, normalized);
    }

    protected final @NonNull FluentFuture<? extends @NonNull CommitInfo> doCommit() {
        return getDelegate().commit();
    }

    protected final boolean doCancel() {
        return getDelegate().cancel();
    }
}
