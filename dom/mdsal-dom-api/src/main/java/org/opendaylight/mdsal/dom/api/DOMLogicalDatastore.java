/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * A datastore which has which has a {@link LogicalDatastoreType}.
 */
@NonNullByDefault
public sealed interface DOMLogicalDatastore extends DOMTransactionFactory
        permits DOMConfigurationDatastore, DOMOperationalDatastore {
    /**
     * Returns the {@link LogicalDatastoreType}.
     *
     * @return the {@link LogicalDatastoreType}
     */
    LogicalDatastoreType type();

    /**
     * Create a new transaction chain. The chain will be initialized to read from its backing datastore, with
     * no outstanding transaction.
     *
     * @return A new transaction chain.
     */
    DOMTransactionChain createTransactionChain();

    /**
     * Create a new transaction chain. The chain will be initialized to read from its backing datastore, with
     * no outstanding transaction.
     *
     * <p>
     * Unlike {@link #createTransactionChain()}, the transaction chain returned by this method is allowed to merge
     * individual transactions into larger chunks. When transactions are merged, the results must be indistinguishable
     * from the result of all operations having been performed on a single transaction.
     *
     * <p>
     * When transactions are merged, {@link DOMTransactionChain#newReadOnlyTransaction()} may actually be backed by
     * a read-write transaction, hence an additional restriction on API use is that multiple read-only transactions
     * may not be open at the same time.
     *
     * @return A new transaction chain.
     */
    DOMTransactionChain createMergingTransactionChain();
}
