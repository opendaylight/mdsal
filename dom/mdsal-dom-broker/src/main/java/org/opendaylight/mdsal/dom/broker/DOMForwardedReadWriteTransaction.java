/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.Map;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Read-Write Transaction, which is composed of several
 * {@link DOMStoreReadWriteTransaction} transactions. Subtransaction is selected by
 * {@link LogicalDatastoreType} type parameter in:
 *
 * <ul>
 * <li>{@link #read(LogicalDatastoreType, YangInstanceIdentifier)}
 * <li>{@link #put(LogicalDatastoreType, YangInstanceIdentifier, NormalizedNode)}
 * <li>{@link #delete(LogicalDatastoreType, YangInstanceIdentifier)}
 * <li>{@link #merge(LogicalDatastoreType, YangInstanceIdentifier, NormalizedNode)}
 * </ul>
 *
 * <p>
 * {@link #submit()} will result in invocation of
 * {@link DOMDataCommitImplementation#submit(org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction, Iterable)}
 * invocation with all
 * {@link org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort} for
 * underlying transactions.
 */
final class DOMForwardedReadWriteTransaction extends DOMForwardedWriteTransaction<DOMStoreReadWriteTransaction>
        implements DOMDataTreeReadWriteTransaction {

    protected DOMForwardedReadWriteTransaction(final Object identifier,
            final Map<LogicalDatastoreType, DOMStoreReadWriteTransaction> backingTxs,
            final AbstractDOMForwardedTransactionFactory<?> commitImpl) {
        super(identifier, backingTxs, commitImpl);
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(final LogicalDatastoreType store,
            final YangInstanceIdentifier path) {
        return getSubtransaction(store).read(path);
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(final LogicalDatastoreType store,
            final YangInstanceIdentifier path) {
        return getSubtransaction(store).exists(path);
    }

    @Override
    public void close() {
        closeSubtransactions();
    }
}
