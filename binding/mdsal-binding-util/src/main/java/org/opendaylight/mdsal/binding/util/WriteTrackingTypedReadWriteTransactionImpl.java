/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Read-write typed transaction which keeps track of writes.
 */
class WriteTrackingTypedReadWriteTransactionImpl<D extends Datastore> extends TypedReadWriteTransactionImpl<D>
        implements WriteTrackingTransaction {

    // This is volatile to ensure we get the latest value; transactions aren't supposed to be used in multiple threads,
    // but the cost here is tiny (one read penalty at the end of a transaction) so we play it safe
    private volatile boolean written;

    WriteTrackingTypedReadWriteTransactionImpl(Class<D> datastoreType, ReadWriteTransaction realTx) {
        super(datastoreType, realTx);
    }

    @Override
    public <T extends DataObject> void put(InstanceIdentifier<T> path, T data) {
        super.put(path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void put(InstanceIdentifier<T> path, T data, boolean createMissingParents) {
        super.put(path, data, createMissingParents);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(InstanceIdentifier<T> path, T data) {
        super.merge(path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(InstanceIdentifier<T> path, T data, boolean createMissingParents) {
        super.merge(path, data, createMissingParents);
        written = true;
    }

    @Override
    public void delete(InstanceIdentifier<?> path) {
        super.delete(path);
        written = true;
    }

    @Override
    public boolean isWritten() {
        return written;
    }
}
