/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class TransactionChainWriteTransaction implements DOMDataTreeWriteTransaction {
    private final DOMDataTreeWriteTransaction delegateTx;
    private final Object identifier;
    private final ShardedDOMTransactionChainAdapter txChain;

    public TransactionChainWriteTransaction(final Object identifier, final DOMDataTreeWriteTransaction delegateTx,
                                            final ShardedDOMTransactionChainAdapter txChain) {
        this.delegateTx = requireNonNull(delegateTx);
        this.identifier = requireNonNull(identifier);
        this.txChain = requireNonNull(txChain);
    }


    @Override
    public void put(final LogicalDatastoreType store, final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        delegateTx.put(store, path, data);
    }

    @Override
    public void merge(final LogicalDatastoreType store, final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        delegateTx.merge(store, path, data);
    }

    @Override
    public boolean cancel() {
        txChain.closeWriteTransaction(FluentFutures.immediateNullFluentFuture());
        return delegateTx.cancel();
    }

    @Override
    public void delete(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        delegateTx.delete(store, path);
    }

    @Override
    public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
        final FluentFuture<? extends CommitInfo> writeResultFuture = delegateTx.commit();
        writeResultFuture.addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                // NOOP
            }

            @Override
            public void onFailure(final Throwable throwable) {
                txChain.transactionFailed(TransactionChainWriteTransaction.this, throwable);
            }
        }, MoreExecutors.directExecutor());

        txChain.closeWriteTransaction(writeResultFuture);
        return writeResultFuture;
    }

    @Override
    public Object getIdentifier() {
        return identifier;
    }
}
