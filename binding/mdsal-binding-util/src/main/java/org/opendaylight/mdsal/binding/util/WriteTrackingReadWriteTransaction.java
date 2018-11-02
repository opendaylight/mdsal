/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.spi.ForwardingReadWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Read-write transaction which keeps track of writes.
 */
final class WriteTrackingReadWriteTransaction extends ForwardingReadWriteTransaction
        implements WriteTrackingTransaction {
    // This is volatile to ensure we get the latest value; transactions aren't supposed to be used in multiple threads,
    // but the cost here is tiny (one read penalty at the end of a transaction) so we play it safe
    private volatile boolean written;

    WriteTrackingReadWriteTransaction(final ReadWriteTransaction delegate) {
        super(delegate);
    }

    @Override
    public <T extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data) {
        super.put(store, path, data);
        written = true;
    }

    @Override
    @Deprecated
    public <T extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data, final boolean createMissingParents) {
        super.put(store, path, data, createMissingParents);
        written = true;
    }

    @Override
    public <T extends DataObject> void createSignificantParentsAndPut(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        super.createSignificantParentsAndPut(store, path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data) {
        super.merge(store, path, data);
        written = true;
    }

    @Override
    @Deprecated
    public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data, final boolean createMissingParents) {
        super.merge(store, path, data, createMissingParents);
        written = true;
    }

    @Override
    public <T extends DataObject> void createSignificantParentsAndMerge(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        super.createSignificantParentsAndMerge(store, path, data);
        written = true;
    }

    @Override
    public void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        super.delete(store, path);
        written = true;
    }

    @Override
    public boolean isWritten() {
        return written;
    }
}
