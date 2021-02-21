/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import java.util.function.Function;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeQueryReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
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
        implements DOMDataTreeQueryReadWriteTransaction {

    DOMForwardedReadWriteTransaction(final Object identifier,
        final Function<LogicalDatastoreType, DOMStoreReadWriteTransaction> txSupplier,
            final AbstractDOMForwardedTransactionFactory<?> commitImpl) {
        super(identifier, txSupplier, commitImpl);
    }

    @Override
    public FluentFuture<Optional<NormalizedNode>> read(final LogicalDatastoreType store,
            final YangInstanceIdentifier path) {
        return getSubtransaction(store).read(path);
    }

    @Override
    public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        return getSubtransaction(store).exists(path);
    }

    @Override
    public FluentFuture<DOMQueryResult> execute(final LogicalDatastoreType store, final DOMQuery query) {
        return getSubtransaction(store).execute(query);
    }
}
