/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Listener for transaction chain events.
 */
// FIXME: 6.0.0: remove this in favor of a TransactionChain destiny, available as a FluentFuture from TransactionChain
public interface TransactionChainListener extends EventListener {
    /**
     * Invoked if when a transaction in the chain fails. All transactions submitted after the failed transaction, in the
     * chain, are automatically cancelled by the time this notification is invoked. Open transactions need to be closed
     * or cancelled.
     * Implementations should invoke chain.close() to close the chain.
     *
     * @param chain Transaction chain which failed
     * @param transaction Transaction which caused the chain to fail
     * @param cause The cause of transaction failure
     */
    void onTransactionChainFailed(@NonNull TransactionChain chain, @NonNull Transaction transaction,
            @NonNull Throwable cause);

    /**
     * Invoked when a transaction chain is completed. A transaction chain is considered completed when it has been
     * closed and all its instructions have completed successfully.
     *
     * @param chain Transaction chain which completed
     */
    void onTransactionChainSuccessful(@NonNull TransactionChain chain);
}

