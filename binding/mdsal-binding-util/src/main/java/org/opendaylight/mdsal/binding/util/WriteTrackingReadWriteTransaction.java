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
class WriteTrackingReadWriteTransaction extends ForwardingReadWriteTransaction implements WriteTrackingTransaction {
    // This is volatile to ensure we get the latest value; transactions aren't supposed to be used in multiple threads,
    // but the cost here is tiny (one read penalty at the end of a transaction) so we play it safe
    private volatile boolean written;

    WriteTrackingReadWriteTransaction(ReadWriteTransaction delegate) {
        super(delegate);
    }

    @Override
    public <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
        super.put(store, path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents) {
        super.put(store, path, data, createMissingParents);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
        super.merge(store, path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
            boolean createMissingParents) {
        super.merge(store, path, data, createMissingParents);
        written = true;
    }

    @Override
    public void delete(LogicalDatastoreType store, InstanceIdentifier<?> path) {
        super.delete(store, path);
        written = true;
    }

    @Override
    public boolean isWritten() {
        return written;
    }
}
