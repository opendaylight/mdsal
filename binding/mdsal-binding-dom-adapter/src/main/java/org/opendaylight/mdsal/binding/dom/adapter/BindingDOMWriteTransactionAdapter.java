/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class BindingDOMWriteTransactionAdapter<T extends DOMDataTreeWriteTransaction> extends AbstractWriteTransaction<T>
        implements WriteTransaction {
    BindingDOMWriteTransactionAdapter(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        super(delegateTx, codec);
    }

    @Override
    public final <U extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data) {
        put(store, path, data, false);
    }

    @Override
    public final <D extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<D> path,
            final D data) {
        merge(store, path, data, false);
    }

    @Override
    public final void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        doDelete(store, path);
    }

    @Override
    public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
        return doCommit();
    }

    @Override
    public final boolean cancel() {
        return doCancel();
    }
}
