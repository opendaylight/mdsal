/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.mdsal.common.api.AsyncTransaction;
import org.opendaylight.mdsal.common.api.TransactionChain;
import org.opendaylight.mdsal.common.api.TransactionChainListener;

/**
 * Simple implementation of {@link TransactionChainListener} for testing.
 *
 * This transaction chain listener does not contain any logic, only update
 * futures ({@link #getFailFuture()} and {@link #getSuccessFuture()} when
 * transaction chain event is retrieved.
 *
 */
class BlockingTransactionChainListener implements TransactionChainListener {

    private final SettableFuture<Throwable> failFuture = SettableFuture.create();
    private final SettableFuture<Void> successFuture = SettableFuture.create();

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain, final AsyncTransaction<?, ?> transaction,
            final Throwable cause) {
        failFuture.set(cause);
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        successFuture.set(null);
    }

    public SettableFuture<Throwable> getFailFuture() {
        return failFuture;
    }

    public SettableFuture<Void> getSuccessFuture() {
        return successFuture;
    }

}
