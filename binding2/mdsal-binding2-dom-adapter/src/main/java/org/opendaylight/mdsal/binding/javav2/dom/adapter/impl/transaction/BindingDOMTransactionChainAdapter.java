/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.mdsal.binding.javav2.api.BindingTransactionChain;
import org.opendaylight.mdsal.binding.javav2.api.ReadTransaction;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.common.api.AsyncTransaction;
import org.opendaylight.mdsal.common.api.TransactionChain;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.yangtools.concepts.Delegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction chain adapter.
 */
@Beta
public final class BindingDOMTransactionChainAdapter
        implements BindingTransactionChain, Delegator<DOMTransactionChain> {

    private static final Logger LOG = LoggerFactory.getLogger(BindingDOMTransactionChainAdapter.class);

    private final DOMTransactionChain delegate;
    private final BindingToNormalizedNodeCodec codec;
    private final TransactionChainListener bindingListener;

    public BindingDOMTransactionChainAdapter(final DOMDataBroker chainFactory, final BindingToNormalizedNodeCodec codec,
            final TransactionChainListener listener) {
        Preconditions.checkNotNull(chainFactory, "DOM Transaction chain factory must not be null");
        this.bindingListener = listener;
        this.delegate = chainFactory.createTransactionChain(new DelegateChainListener());
        this.codec = codec;
    }

    @Override
    public DOMTransactionChain getDelegate() {
        return delegate;
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        final DOMDataTreeReadTransaction delegateTx = delegate.newReadOnlyTransaction();
        return new BindingDOMReadTransactionAdapter(delegateTx, codec);
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        final DOMDataTreeWriteTransaction delegateTx = delegate.newWriteOnlyTransaction();
        return new BindingDOMWriteTransactionAdapter<DOMDataTreeWriteTransaction>(delegateTx, codec) {

            @Override
            public CheckedFuture<Void, TransactionCommitFailedException> submit() {
                return listenForFailure(this, super.submit());
            }

        };
    }

    private <T, F extends ListenableFuture<T>> F listenForFailure(final WriteTransaction tx, final F future) {
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onFailure(final Throwable throwable) {
                failTransactionChain(tx, throwable);
            }

            @Override
            public void onSuccess(final T result) {
                // Intentionally NOOP
            }
        }, MoreExecutors.directExecutor());

        return future;
    }

    private void failTransactionChain(final WriteTransaction tx, final Throwable throwable) {
        /*
         * We assume correct state change for underlying transaction
         *
         * chain, so we are not changing any of our internal state to mark that we failed.
         */
        this.bindingListener.onTransactionChainFailed(this, tx, throwable);
    }

    @Override
    public void close() {
        delegate.close();
    }

    private final class DelegateChainListener implements TransactionChainListener {

        @Override
        public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                final AsyncTransaction<?, ?> transaction, final Throwable cause) {
            Preconditions.checkState(delegate.equals(chain),
                    "Illegal state - listener for %s was invoked for incorrect chain %s.", delegate, chain);
            /*
             * Intentionally NOOP, callback for failure, since we are also listening on each transaction
             * future for failure, in order to have reference to Binding Transaction (which was seen by client
             * of this transaction chain), instead of DOM transaction which is known only to this chain,
             * binding transaction implementation and underlying transaction chain.
             *
             */
            LOG.debug("Transaction chain {} failed. Failed DOM Transaction {}", this, transaction, cause);
        }

        @Override
        public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
            Preconditions.checkState(delegate.equals(chain),
                    "Illegal state - listener for %s was invoked for incorrect chain %s.", delegate, chain);
            bindingListener.onTransactionChainSuccessful(BindingDOMTransactionChainAdapter.this);
        }
    }
}
