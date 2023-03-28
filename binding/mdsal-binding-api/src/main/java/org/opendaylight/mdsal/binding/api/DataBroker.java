/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Provides access to a conceptual data tree store and also provides the ability to
 * subscribe for changes to data under a given branch of the tree.
 *
 * <p>
 * All operations on the data tree are performed via one of the transactions:
 * <ul>
 * <li>Read-Only - allocated using {@link #newReadOnlyTransaction()}
 * <li>Write-Only - allocated using {@link #newWriteOnlyTransaction()}
 * </ul>
 *
 * <p>
 * These transactions provide a stable isolated view of data tree, which is guaranteed to be not
 * affected by other concurrent transactions, until transaction is committed.
 *
 * <p>
 * For a detailed explanation of how transaction are isolated and how transaction-local changes are
 * committed to global data tree, see {@link ReadTransaction}, {@link WriteTransaction}
 * and {@link WriteTransaction#commit()}.
 *
 * <p>
 * It is strongly recommended to use the type of transaction, which provides only the minimal
 * capabilities you need. This allows for optimizations at the data broker / data store level. For
 * example, implementations may optimize the transaction for reading if they know ahead of time that
 * you only need to read data - such as not keeping additional meta-data, which may be required for
 * write transactions.
 *
 * <p>
 * <b>Implementation Note:</b> This interface is not intended to be implemented by users of MD-SAL,
 * but only to be consumed by them.
 */
public interface DataBroker extends BindingService, TransactionFactory, DataTreeChangeService {
    /**
     * Create a new transaction chain. The chain will be initialized to read from its backing datastore, with
     * no outstanding transaction. Listener will be registered to handle chain-level events.
     *
     * @param listener Transaction chain event listener
     * @return A new transaction chain.
     */
    @NonNull TransactionChain createTransactionChain(@NonNull TransactionChainListener listener);

    /**
     * Create a new transaction chain. The chain will be initialized to read from its backing datastore, with
     * no outstanding transaction. Listener will be registered to handle chain-level events.
     *
     * <p>
     * Unlike {@link #createTransactionChain(TransactionChainListener)}, the transaction chain returned by this
     * method is allowed to merge individual transactions into larger chunks. When transactions are merged, the results
     * must be indistinguishable from the result of all operations having been performed on a single transaction.
     *
     * <p>
     * When transactions are merged, {@link TransactionChain#newReadOnlyTransaction()} may actually be backed by
     * a read-write transaction, hence an additional restriction on API use is that multiple read-only transactions
     * may not be open at the same time.
     *
     * @param listener Transaction chain event listener
     * @return A new transaction chain.
     */
    @NonNull TransactionChain createMergingTransactionChain(@NonNull TransactionChainListener listener);


    default <T extends DataObject, L extends DataObjectListenerAdapter> @NonNull ListenerRegistration<L>
        registerListener(@NonNull DataTreeIdentifier<T> treeId, @NonNull L listener) {

        InstanceIdentifier<T> instanceIdentifier = treeId.getRootIdentifier();
        if (instanceIdentifier.isWildcarded()) {
            throw new IllegalArgumentException(
                "Cannot register listener for wildcard InstanceIdentifier: " + instanceIdentifier);
        }
        return registerDataTreeChangeListener(treeId, listener);
    }
}
