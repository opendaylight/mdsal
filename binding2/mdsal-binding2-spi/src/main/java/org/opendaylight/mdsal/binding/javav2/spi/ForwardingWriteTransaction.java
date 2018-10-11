/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.spi;

import com.google.common.collect.ForwardingObject;
import com.google.common.util.concurrent.FluentFuture;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * Utility {@link WriteTransaction} implementation which forwards all interface method
 * invocation to a delegate instance.
 */
public class ForwardingWriteTransaction extends ForwardingObject implements WriteTransaction {

    private final WriteTransaction delegate;

    protected ForwardingWriteTransaction(WriteTransaction delegate) {
        this.delegate = delegate;
    }

    @Override
    protected WriteTransaction delegate() {
        return delegate;
    }

    @Override
    public <T extends TreeNode> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
        delegate.put(store, path, data);
    }

    @Override
    public <T extends TreeNode> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents) {
        delegate.put(store, path, data, createMissingParents);
    }

    @Override
    public <T extends TreeNode> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
        delegate.merge(store, path, data);
    }

    @Override
    public <T extends TreeNode> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents) {
        delegate.merge(store, path, data, createMissingParents);
    }

    @Override
    public void delete(LogicalDatastoreType store, InstanceIdentifier<?> path) {
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

    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }
}
