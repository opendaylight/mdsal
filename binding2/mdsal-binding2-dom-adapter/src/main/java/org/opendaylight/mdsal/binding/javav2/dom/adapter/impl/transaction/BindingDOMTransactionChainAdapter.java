/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.javav2.api.ReadTransaction;
import org.opendaylight.mdsal.binding.javav2.api.TransactionChain;
import org.opendaylight.mdsal.binding.javav2.api.TransactionChainClosedException;
import org.opendaylight.mdsal.binding.javav2.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainClosedException;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.yangtools.concepts.Delegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction chain adapter.
 */
@Beta
public final class BindingDOMTransactionChainAdapter implements TransactionChain, Delegator<DOMTransactionChain> {

    private static final Logger LOG = LoggerFactory.getLogger(BindingDOMTransactionChainAdapter.class);

    private final DOMTransactionChain delegate;
    private final BindingToNormalizedNodeCodec codec;
    private final TransactionChainListener bindingListener;

    public BindingDOMTransactionChainAdapter(final DOMDataBroker chainFactory, final BindingToNormalizedNodeCodec codec,
            final TransactionChainListener listener) {
        requireNonNull(chainFactory, "DOM Transaction chain factory must not be null");
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
        return new BindingDOMReadTransactionAdapter(createTransaction(delegate::newReadOnlyTransaction), codec);
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        final DOMDataTreeWriteTransaction delegateTx = createTransaction(delegate::newWriteOnlyTransaction);
        return new BindingDOMWriteTransactionAdapter<DOMDataTreeWriteTransaction>(delegateTx, codec) {

            @Override
            public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
                return listenForFailure(this, super.commit());
            }

        };
    }

    private <T, F extends FluentFuture<T>> F listenForFailure(final WriteTransaction tx, final F future) {
        future.addCallback(new FutureCallback<T>() {
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

    private static <T> T createTransaction(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (DOMTransactionChainClosedException e) {
            throw new TransactionChainClosedException("Transaction chain already closed", e);
        }
    }

    private final class DelegateChainListener implements DOMTransactionChainListener {
        @Override
        public void onTransactionChainFailed(final DOMTransactionChain chain,
                final DOMDataTreeTransaction transaction, final Throwable cause) {
            checkState(delegate.equals(chain), "Listener for %s was invoked for incorrect chain %s.", delegate, chain);
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
        public void onTransactionChainSuccessful(final DOMTransactionChain chain) {
            checkState(delegate.equals(chain), "Listener for %s was invoked for incorrect chain %s.", delegate, chain);
            bindingListener.onTransactionChainSuccessful(BindingDOMTransactionChainAdapter.this);
        }
    }
}
