/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class TransactionChainReadWriteTransaction extends TransactionChainWriteTransaction
        implements DOMDataTreeReadWriteTransaction {

    private final TransactionChainReadTransaction readTx;

    TransactionChainReadWriteTransaction(final Object identifier,
            final DOMDataTreeReadWriteTransaction delegateWriteTx, final DOMDataTreeReadTransaction delegateReadTx,
            final ListenableFuture<Void> previousWriteTxFuture, final ShardedDOMTransactionChainAdapter txChain) {
        super(identifier, delegateWriteTx, txChain);
        readTx = new TransactionChainReadTransaction(identifier, delegateReadTx, previousWriteTxFuture, txChain);
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(LogicalDatastoreType store,
            YangInstanceIdentifier path) {
        return readTx.read(store, path);
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(LogicalDatastoreType store, YangInstanceIdentifier path) {
        return readTx.exists(store, path);
    }

    @Override
    public void close() {
    }
}
