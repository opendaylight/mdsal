/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.yangtools.yang.common.Empty;

class TracingTransactionChain extends AbstractCloseTracked<TracingTransactionChain> implements DOMTransactionChain {

    private final DOMTransactionChain delegate;
    private final TracingBroker tracingBroker;
    private final CloseTrackedRegistry<TracingReadOnlyTransaction> readOnlyTransactionsRegistry;
    private final CloseTrackedRegistry<TracingWriteTransaction> writeTransactionsRegistry;
    private final CloseTrackedRegistry<TracingReadWriteTransaction> readWriteTransactionsRegistry;

    TracingTransactionChain(final DOMTransactionChain delegate, final TracingBroker tracingBroker,
            final CloseTrackedRegistry<TracingTransactionChain> transactionChainsRegistry) {
        super(transactionChainsRegistry);
        this.delegate = requireNonNull(delegate);
        this.tracingBroker = requireNonNull(tracingBroker);

        final boolean isDebug = transactionChainsRegistry.isDebugContextEnabled();
        String anchor = "TransactionChain@" + Integer.toHexString(hashCode());
        readOnlyTransactionsRegistry  = new CloseTrackedRegistry<>(anchor, "newReadOnlyTransaction()", isDebug);
        writeTransactionsRegistry     = new CloseTrackedRegistry<>(anchor, "newWriteOnlyTransaction()", isDebug);
        readWriteTransactionsRegistry = new CloseTrackedRegistry<>(anchor, "newReadWriteTransaction()", isDebug);
    }

    @Override
    public ListenableFuture<Empty> future() {
        return delegate.future();
    }

    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        return new TracingReadOnlyTransaction(delegate.newReadOnlyTransaction(), readOnlyTransactionsRegistry);
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        return new TracingReadWriteTransaction(delegate.newReadWriteTransaction(), tracingBroker,
            readWriteTransactionsRegistry);
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        return new TracingWriteTransaction(delegate.newWriteOnlyTransaction(), tracingBroker,
            writeTransactionsRegistry);
    }

    @Override
    public void close() {
        delegate.close();
        super.removeFromTrackedRegistry();
    }

    public CloseTrackedRegistry<TracingReadOnlyTransaction> getReadOnlyTransactionsRegistry() {
        return readOnlyTransactionsRegistry;
    }

    public CloseTrackedRegistry<TracingReadWriteTransaction> getReadWriteTransactionsRegistry() {
        return readWriteTransactionsRegistry;
    }

    public CloseTrackedRegistry<TracingWriteTransaction> getWriteTransactionsRegistry() {
        return writeTransactionsRegistry;
    }

    // https://jira.opendaylight.org/browse/CONTROLLER-1792

    @Override
    public final boolean equals(final Object object) {
        return object == this || delegate.equals(object);
    }

    @Override
    public final int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public final String toString() {
        return getClass().getName() + "; delegate=" + delegate;
    }
}
