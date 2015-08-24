/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

class BindingDOMWriteTransactionAdapter<T extends DOMDataWriteTransaction> extends
        AbstractWriteTransaction<T> implements WriteTransaction {

    protected BindingDOMWriteTransactionAdapter(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        super(delegateTx, codec);
    }

    @Override
    public <U extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
                                           final U data) {
        put(store, path, data,false);
    }

    @Override
    public <D extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<D> path,
                                             final D data) {
        merge(store, path, data,false);
    }


    @Override
    public void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        doDelete( store, path);
    }

    @Deprecated
    @Override
    public ListenableFuture<RpcResult<TransactionStatus>> commit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CheckedFuture<Void,TransactionCommitFailedException> submit() {
        return doSubmit();
    }

    @Override
    public boolean cancel() {
        return doCancel();
    }
}