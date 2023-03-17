/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Utility {@link WriteTransaction} implementation which forwards all interface method
 * invocation to a delegate instance.
 */
public class ForwardingWriteTransaction extends ForwardingTransaction implements WriteTransaction {
    private final WriteTransaction delegate;

    public ForwardingWriteTransaction(final WriteTransaction delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected WriteTransaction delegate() {
        return delegate;
    }

    @Override
    public <T extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data) {
        delegate.put(store, path, data);
    }

    @Override
    public <T extends DataObject> void put(final @NonNull LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path, final @NonNull T data) {
        delegate.put(store, path, data);
    }

    @Deprecated
    @Override
    public <T extends DataObject> void mergeParentStructurePut(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        delegate.mergeParentStructurePut(store, path, data);
    }

    @Override
    public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data) {
        delegate.merge(store, path, data);
    }

    @Override
    public <T extends DataObject> void merge(final @NonNull LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path, final @NonNull T data) {
        delegate.merge(store, path, data);
    }

    @Deprecated
    @Override
    public <T extends DataObject> void mergeParentStructureMerge(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        delegate.mergeParentStructureMerge(store, path, data);
    }

    @Override
    public void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        delegate.delete(store, path);
    }

    @Override
    public void delete(final @NonNull LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<?> path) {
        delegate.delete(store, path);
    }

    @Override
    public boolean cancel() {
        return delegate.cancel();
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        return delegate.commit();
    }
}
