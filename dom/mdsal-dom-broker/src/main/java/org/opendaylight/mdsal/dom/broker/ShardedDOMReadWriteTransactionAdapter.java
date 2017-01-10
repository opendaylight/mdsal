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
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class ShardedDOMReadWriteTransactionAdapter extends ShardedDOMWriteTransactionAdapter
        implements DOMDataTreeReadWriteTransaction {

    private final ShardedDOMReadTransactionAdapter readAdapter;

    ShardedDOMReadWriteTransactionAdapter(Object identifier, DOMDataTreeService transactionDelegator) {
        super(identifier, transactionDelegator);
        readAdapter = new ShardedDOMReadTransactionAdapter(identifier, transactionDelegator);
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(LogicalDatastoreType store,
            YangInstanceIdentifier path) {
        return readAdapter.read(store, path);
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(LogicalDatastoreType store, YangInstanceIdentifier path) {
        return readAdapter.exists(store, path);
    }

    @Override
    public void close() {
        readAdapter.close();
    }

    ShardedDOMReadTransactionAdapter getReadAdapter() {
        return readAdapter;
    }
}
