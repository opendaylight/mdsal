/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import com.google.common.collect.ForwardingObject;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Utility {@link ReadWriteTransaction} implementation which forwards all interface method
 * invocation to a delegate instance.
 */
public class ForwardingReadWriteTransaction extends ForwardingObject implements ReadWriteTransaction {

    private final ReadWriteTransaction delegate;

    protected ForwardingReadWriteTransaction(ReadWriteTransaction delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ReadWriteTransaction delegate() {
        return delegate;
    }

    @Override
    public <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
        delegate.put(store, path, data);
    }

    @Override
    public <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents) {
        delegate.put(store, path, data, createMissingParents);
    }

    @Override
    public <T extends DataObject> FluentFuture<Optional<T>> read(LogicalDatastoreType store,
            InstanceIdentifier<T> path) {
        return delegate.read(store, path);
    }

    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public boolean cancel() {
        return delegate.cancel();
    }

    @Override
    public <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
        delegate.merge(store, path, data);
    }

    @Override
    public <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents) {
        delegate.merge(store, path, data, createMissingParents);
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        return delegate.commit();
    }

    @Override
    public void delete(LogicalDatastoreType store, InstanceIdentifier<?> path) {
        delegate.delete(store, path);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
