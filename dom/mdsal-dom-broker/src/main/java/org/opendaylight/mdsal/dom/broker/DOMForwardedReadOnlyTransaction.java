/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.mdsal.dom.api.DOMDataTreeQueryReadTransaction;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Read Only Transaction, which is composed of several
 * {@link DOMStoreReadTransaction} transactions. Subtransaction is selected by
 * {@link LogicalDatastoreType} type parameter in
 * {@link #read(LogicalDatastoreType, YangInstanceIdentifier)}.
 */
class DOMForwardedReadOnlyTransaction extends AbstractDOMForwardedTransaction<DOMStoreReadTransaction>
        implements DOMDataTreeQueryReadTransaction {
    DOMForwardedReadOnlyTransaction(final Object identifier,
            final Function<LogicalDatastoreType, DOMStoreReadTransaction> txSupplier) {
        super(identifier, txSupplier);
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

    @Override
    public void close() {
        closeSubtransactions();
    }
}
