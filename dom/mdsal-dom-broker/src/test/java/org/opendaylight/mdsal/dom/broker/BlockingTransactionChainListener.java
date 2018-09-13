/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;

/**
 * Simple implementation of {@link TransactionChainListener} for testing.
 *
 *<p>
 * This transaction chain listener does not contain any logic, only update
 * futures ({@link #getFailFuture()} and {@link #getSuccessFuture()} when
 * transaction chain event is retrieved.
 *
 */
class BlockingTransactionChainListener implements DOMTransactionChainListener {

    private final SettableFuture<Throwable> failFuture = SettableFuture.create();
    private final SettableFuture<Void> successFuture = SettableFuture.create();

    @Override
    public void onTransactionChainFailed(final DOMTransactionChain chain, final DOMDataTreeTransaction transaction,
            final Throwable cause) {
        failFuture.set(cause);
    }

    @Override
    public void onTransactionChainSuccessful(final DOMTransactionChain chain) {
        successFuture.set(null);
    }

    public SettableFuture<Throwable> getFailFuture() {
        return failFuture;
    }

    public SettableFuture<Void> getSuccessFuture() {
        return successFuture;
    }

}
