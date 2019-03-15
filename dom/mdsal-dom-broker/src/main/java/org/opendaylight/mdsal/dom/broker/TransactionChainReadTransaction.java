/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class TransactionChainReadTransaction implements DOMDataTreeReadTransaction {

    private final DOMDataTreeReadTransaction delegateReadTx;
    private final FluentFuture<? extends CommitInfo> previousWriteTxFuture;
    private final Object identifier;
    private final ShardedDOMTransactionChainAdapter txChain;

    TransactionChainReadTransaction(final Object txIdentifier, final DOMDataTreeReadTransaction delegateReadTx,
                                    final FluentFuture<? extends CommitInfo> previousWriteTxFuture,
                                    final ShardedDOMTransactionChainAdapter txChain) {
        this.delegateReadTx = delegateReadTx;
        this.previousWriteTxFuture = previousWriteTxFuture;
        this.identifier = txIdentifier;
        this.txChain = txChain;
    }

    @Override
    public FluentFuture<Optional<NormalizedNode<?, ?>>> read(final LogicalDatastoreType store,
            final YangInstanceIdentifier path) {
        final SettableFuture<Optional<NormalizedNode<?, ?>>> readResult = SettableFuture.create();

        previousWriteTxFuture.addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                delegateReadTx.read(store, path).addCallback(new FutureCallback<Optional<NormalizedNode<?, ?>>>() {
                    @Override
                    public void onSuccess(final Optional<NormalizedNode<?, ?>> result) {
                        readResult.set(result);
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        txChain.transactionFailed(TransactionChainReadTransaction.this, throwable);
                        readResult.setException(throwable);
                    }
                }, MoreExecutors.directExecutor());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                // we don't have to notify txchain about this failure
                // failed write transaction should do this
                readResult.setException(throwable);
            }
        }, MoreExecutors.directExecutor());

        return FluentFuture.from(readResult);
    }

    @Override
    public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        return read(store, path).transform(Optional::isPresent, MoreExecutors.directExecutor());
    }

    @Override
    public void close() {
        delegateReadTx.close();
        txChain.closeReadTransaction();
    }

    @Override
    public Object getIdentifier() {
        return identifier;
    }
}
