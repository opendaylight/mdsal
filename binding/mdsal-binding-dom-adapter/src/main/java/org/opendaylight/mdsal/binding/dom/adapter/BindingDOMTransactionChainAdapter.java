/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainClosedException;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainClosedException;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.yangtools.concepts.Delegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BindingDOMTransactionChainAdapter implements TransactionChain, Delegator<DOMTransactionChain> {

    private static final Logger LOG = LoggerFactory.getLogger(BindingDOMTransactionChainAdapter.class);

    private final DOMTransactionChain delegate;
    private final BindingToNormalizedNodeCodec codec;
    private final DelegateChainListener domListener;
    private final TransactionChainListener bindingListener;

    BindingDOMTransactionChainAdapter(final Function<DOMTransactionChainListener, DOMTransactionChain> chainFactory,
            final BindingToNormalizedNodeCodec codec, final TransactionChainListener listener) {
        requireNonNull(chainFactory, "DOM Transaction chain factory must not be null");
        this.domListener = new DelegateChainListener();
        this.bindingListener = listener;
        this.delegate = chainFactory.apply(domListener);
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

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        final DOMDataTreeReadWriteTransaction delegateTx = createTransaction(delegate::newReadWriteTransaction);
        return new BindingDOMReadWriteTransactionAdapter(delegateTx, codec) {
            @Override
            public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
                return listenForFailure(this, super.commit());
            }
        };
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private void failTransactionChain(final WriteTransaction tx, final Throwable throwable) {
        /*
         *  We asume correct state change for underlaying transaction
         *
         * chain, so we are not changing any of our internal state
         * to mark that we failed.
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
        public void onTransactionChainFailed(final DOMTransactionChain chain, final DOMDataTreeTransaction transaction,
                final Throwable cause) {
            checkState(delegate.equals(chain), "Listener for %s was invoked for incorrect chain %s.", delegate, chain);
            /*
             * Intentionally NOOP, callback for failure, since we
             * are also listening on each transaction future for failure,
             * in order to have reference to Binding Transaction (which was seen by client
             * of this transaction chain), instead of DOM transaction
             * which is known only to this chain, binding transaction implementation
             * and underlying transaction chain.
             *
             */
            LOG.debug("Transaction chain {} failed. Failed DOM Transaction {}",this,transaction,cause);
        }

        @Override
        public void onTransactionChainSuccessful(final DOMTransactionChain chain) {
            checkState(delegate.equals(chain), "Listener for %s was invoked for incorrect chain %s.", delegate, chain);
            bindingListener.onTransactionChainSuccessful(BindingDOMTransactionChainAdapter.this);
        }
    }
}
