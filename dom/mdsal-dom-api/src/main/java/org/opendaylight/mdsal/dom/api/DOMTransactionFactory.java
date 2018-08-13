/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.mdsal.common.api.AsyncDataTransactionFactory;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A factory which allocates new transactions to operate on the data tree.
 *
 * <p>
 * <b>Note:</b> This interface is not intended to be used directly, but rather via subinterfaces
 * which introduces additional semantics to allocated transactions.
 * <ul>
 * <li> {@link DOMDataBroker}
 * <li> {@link DOMTransactionChain}
 * </ul>
 *
 * <p>
 * All operations on the data tree are performed via one of the transactions:
 * <ul>
 * <li>Read-Only - allocated using {@link #newReadOnlyTransaction()}
 * <li>Write-Only - allocated using {@link #newWriteOnlyTransaction()}
 * </ul>
 *
 * <p>
 * These transactions provides a stable isolated view of the data tree, which is guaranteed to be
 * not affected by other concurrent transactions, until transaction is committed.
 *
 * <p>
 * For a detailed explanation of how transaction are isolated and how transaction-local changes are
 * committed to global data tree, see {@link DOMDataTreeReadTransaction}, {@link DOMDataTreeWriteTransaction}
 * and {@link DOMDataTreeWriteTransaction#commit()}.
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
 *
 * @see DOMDataBroker
 * @see DOMTransactionChain
 */
public interface DOMTransactionFactory
        extends AsyncDataTransactionFactory<YangInstanceIdentifier, NormalizedNode<?, ?>> {

    @Override
    DOMDataTreeReadTransaction newReadOnlyTransaction();

    @Override
    DOMDataTreeWriteTransaction newWriteOnlyTransaction();

    @Override
    DOMDataTreeReadWriteTransaction newReadWriteTransaction();
}
