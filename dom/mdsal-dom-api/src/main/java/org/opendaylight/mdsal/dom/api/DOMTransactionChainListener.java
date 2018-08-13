/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.mdsal.common.api.TransactionChainListener;

/**
 * Listener for {@link DOMTransactionChain} events.
 */
public interface DOMTransactionChainListener
        extends TransactionChainListener<DOMDataTreeTransaction, DOMTransactionChain> {
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
    @Override
    void onTransactionChainFailed(DOMTransactionChain chain, DOMDataTreeTransaction transaction, Throwable cause);

    /**
     * Invoked when a transaction chain is completed. A transaction chain is considered completed when it has been
     * closed and all its instructions have completed successfully.
     *
     * @param chain Transaction chain which completed
     */
    @Override
    void onTransactionChainSuccessful(DOMTransactionChain chain);
}

