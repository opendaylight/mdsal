/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.spi.ForwardingWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Write transaction which keeps track of writes.
 */
class WriteTrackingWriteTransaction extends ForwardingWriteTransaction implements WriteTrackingTransaction {
    // This is only ever read *after* changes to the transaction are complete
    private boolean written;

    WriteTrackingWriteTransaction(final WriteTransaction delegate) {
        super(delegate);
    }

    @Override
    public <T extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data) {
        super.put(store, path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void put(final LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path, final T data) {
        super.put(store, path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final T data) {
        super.merge(store, path, data);
        written = true;
    }

    @Override
    public <T extends DataObject> void merge(final LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path, final T data) {
        super.merge(store, path, data);
        written = true;
    }

    @Override
    public void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        super.delete(store, path);
        written = true;
    }

    @Override
    public void delete(final LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.InstanceIdentifier<?> path) {
        super.delete(store, path);
        written = true;
    }

    @Override
    public boolean isWritten() {
        return written;
    }
}
