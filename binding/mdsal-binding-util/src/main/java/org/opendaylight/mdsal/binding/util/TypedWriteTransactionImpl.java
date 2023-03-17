/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation of {@link TypedWriteTransaction}.
 *
 * @param <D> The datastore which the transaction targets.
 * @param <X> WriteTransaction type
 */
class TypedWriteTransactionImpl<D extends Datastore, X extends WriteTransaction> extends TypedTransaction<D, X>
        implements TypedWriteTransaction<D> {
    TypedWriteTransactionImpl(final D datastore, final X realTx) {
        super(datastore, realTx);
    }

    @Override
    public final <T extends DataObject> void put(final InstanceIdentifier<T> path, final T data) {
        delegate().put(getDatastoreType(), path, data);
        postOperation();
    }

    @Override
    public <T extends DataObject> void put(final org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path,
            final T data) {
        delegate().put(getDatastoreType(), path, data);
        postOperation();
    }

    @Override
    public final <T extends DataObject> void mergeParentStructurePut(final InstanceIdentifier<T> path,
            final T data) {
        delegate().mergeParentStructurePut(getDatastoreType(), path, data);
        postOperation();
    }

    @Override
    public final <T extends DataObject> void merge(final InstanceIdentifier<T> path, final T data) {
        delegate().merge(getDatastoreType(), path, data);
        postOperation();
    }

    @Override
    public <T extends DataObject> void merge(final org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path,
        final T data) {
        delegate().merge(getDatastoreType(), path, data);
        postOperation();

    }

    @Override
    public final <T extends DataObject> void mergeParentStructureMerge(final InstanceIdentifier<T> path,
            final T data) {
        delegate().mergeParentStructureMerge(getDatastoreType(), path, data);
        postOperation();
    }

    @Override
    public final void delete(final InstanceIdentifier<?> path) {
        delegate().delete(getDatastoreType(), path);
        postOperation();
    }

    @Override
    public void delete(final org.opendaylight.mdsal.binding.api.InstanceIdentifier<?> path) {
        delegate().delete(getDatastoreType(), path);

    }

    void postOperation() {
        // Defaults to no-op
    }
}
