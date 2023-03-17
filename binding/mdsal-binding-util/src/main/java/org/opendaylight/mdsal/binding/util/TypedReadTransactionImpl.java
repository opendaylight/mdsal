/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation of {@link TypedReadTransaction}.
 *
 * @param <D> The datastore which the transaction targets.
 */
final class TypedReadTransactionImpl<D extends Datastore> extends TypedTransaction<D, ReadTransaction>
        implements TypedReadTransaction<D> {
    TypedReadTransactionImpl(final D datastore, final ReadTransaction realTx) {
        super(datastore, realTx);
    }

    @Override
    public <T extends DataObject> FluentFuture<Optional<T>> read(final InstanceIdentifier<T> path) {
        return delegate().read(getDatastoreType(), path);
    }

    @Override
    public <T extends DataObject> FluentFuture<Optional<T>> read(
            org.opendaylight.mdsal.binding.api.InstanceIdentifier<T> path) {
        return delegate().read(getDatastoreType(), path);
    }

    @Override
    public FluentFuture<Boolean> exists(final InstanceIdentifier<?> path) {
        return delegate().exists(getDatastoreType(), path);
    }

    @Override
    public FluentFuture<Boolean> exists(
            org.opendaylight.mdsal.binding.api.InstanceIdentifier<?> path) {
        return delegate().exists(getDatastoreType(), path);
    }

    @Override
    public <T extends DataObject> FluentFuture<QueryResult<T>> execute(final QueryExpression<T> query) {
        return doExecute(query);
    }
}
