/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

/**
 * Composite DOM Transaction backed by {@link DOMStoreTransaction}.
 *
 * <p>
 * Abstract base for composite transaction, which provides access only to common
 * functionality as retrieval of subtransaction, close method and retrieval of
 * identifier.
 *
 * @param <T> {@link DOMStoreTransaction} type
 */
abstract class AbstractDOMForwardedTransaction<T extends DOMStoreTransaction>
        implements DOMDataTreeTransaction {
    private final @NonNull Object identifier;
    private final @NonNull T backingTx;

    /**
     * Creates new composite Transactions.
     *
     * @param identifier Identifier of transaction.
     * @param backingTx backing {@link DOMStoreTransaction}
     */
    protected AbstractDOMForwardedTransaction(final Object identifier, final T backingTx) {
        this.identifier = requireNonNull(identifier, "Identifier should not be null");
        this.backingTx = requireNonNull(backingTx, "Backing transaction should not be null");
    }

    /**
     * Returns the backing {@link DOMStoreTransaction}.
     *
     * @return the backing {@link DOMStoreTransaction}
     */
    protected final @NonNull T backingTx() {
        return backingTx;
    }

    @Override
    public Object getIdentifier() {
        return identifier;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void closeSubtransactions() {
        /*
         * We share one exception for all failures, which are added
         * as supressedExceptions to it.
         */
        try {
            backingTx.close();
        } catch (Exception e) {
            // If we did not allocate failure we allocate it
            throw new IllegalStateException("Uncaught exception occurred during closing transaction", e);
        }
    }
}
