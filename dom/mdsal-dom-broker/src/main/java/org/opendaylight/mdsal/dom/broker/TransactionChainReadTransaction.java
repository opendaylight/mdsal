/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class TransactionChainReadTransaction implements DOMDataTreeReadTransaction {

    private final DOMDataTreeReadTransaction delegateReadTx;
    private final ListenableFuture<Void> previousWriteTxFuture;
    private final Object identifier;
    private final ShardedDOMTransactionChainAdapter txChain;

    TransactionChainReadTransaction(final Object txIdentifier, final DOMDataTreeReadTransaction delegateReadTx,
                                    final ListenableFuture<Void> previousWriteTxFuture,
                                    final ShardedDOMTransactionChainAdapter txChain) {
        this.delegateReadTx = delegateReadTx;
        this.previousWriteTxFuture = previousWriteTxFuture;
        this.identifier = txIdentifier;
        this.txChain = txChain;
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(final LogicalDatastoreType store,
            final YangInstanceIdentifier path) {
        final SettableFuture<Optional<NormalizedNode<?, ?>>> readResult = SettableFuture.create();

        Futures.addCallback(previousWriteTxFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void result) {
                Futures.addCallback(delegateReadTx.read(store, path),
                    new FutureCallback<Optional<NormalizedNode<?, ?>>>() {

                        @Override
                        public void onSuccess(@Nullable final Optional<NormalizedNode<?, ?>> result) {
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

        return Futures.makeChecked(readResult, ReadFailedException.MAPPER);
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(final LogicalDatastoreType store,
                                                              final YangInstanceIdentifier path) {
        final Function<Optional<NormalizedNode<?, ?>>, Boolean> transform =
            optionalNode -> optionalNode.isPresent() ? Boolean.TRUE : Boolean.FALSE;
        final ListenableFuture<Boolean> existsResult = Futures.transform(read(store, path), transform,
            MoreExecutors.directExecutor());
        return Futures.makeChecked(existsResult, ReadFailedException.MAPPER);
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
