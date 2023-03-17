/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.QueryReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class BindingDOMReadWriteTransactionAdapter extends BindingDOMWriteTransactionAdapter<DOMDataTreeReadWriteTransaction>
        implements QueryReadWriteTransaction {
    BindingDOMReadWriteTransactionAdapter(final AdapterContext adapterContext,
            final DOMDataTreeReadWriteTransaction delegateTx) {
        super(adapterContext, delegateTx);
    }

    @Override
    public final <T extends DataObject> FluentFuture<Optional<T>> read(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path) {
        return doRead(getDelegate(), store, path);
    }

    @Override
    public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(
            @NonNull LogicalDatastoreType store,
            org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path) {
        return doRead(getDelegate(), store, path);
    }

    @Override
    public final FluentFuture<Boolean> exists(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        return doExists(getDelegate(), store, path);
    }

    @Override
    public @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store,
            org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<?> path) {
        return doExists(getDelegate(), store, path);
    }

    @Override
    public final <T extends @NonNull DataObject> FluentFuture<QueryResult<T>> execute(final LogicalDatastoreType store,
            final QueryExpression<T> query) {
        return doExecute(getDelegate(), store, query);
    }
}
