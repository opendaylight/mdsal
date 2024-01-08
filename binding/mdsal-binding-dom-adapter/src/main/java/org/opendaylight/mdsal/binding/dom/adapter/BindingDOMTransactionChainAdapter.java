/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainClosedException;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainClosedException;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.common.Empty;

final class BindingDOMTransactionChainAdapter implements TransactionChain, Delegator<DOMTransactionChain> {
    private final @NonNull DOMTransactionChain delegate;
    private final @NonNull AdapterContext adapterContext;

    BindingDOMTransactionChainAdapter(final DOMTransactionChain delegate, final AdapterContext adapterContext) {
        this.delegate = requireNonNull(delegate);
        this.adapterContext = requireNonNull(adapterContext);
    }

    @Override
    public DOMTransactionChain getDelegate() {
        return delegate;
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return new BindingDOMReadTransactionAdapter(adapterContext,
            createTransaction(delegate::newReadOnlyTransaction));
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new BindingDOMWriteTransactionAdapter<>(adapterContext,
            createTransaction(delegate::newWriteOnlyTransaction));
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return new BindingDOMReadWriteTransactionAdapter(adapterContext,
            createTransaction(delegate::newReadWriteTransaction));
    }

    @Override
    public ListenableFuture<Empty> future() {
        return delegate.future();
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
}
