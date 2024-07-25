/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.util.concurrent.FluentFuture;
import org.opendaylight.mdsal.binding.api.QueryOperations;
import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.spi.ForwardingTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Operational;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Running;
import org.opendaylight.yangtools.binding.DataObject;

abstract class TypedTransaction<D extends Datastore, X extends Transaction> extends ForwardingTransaction {
    private final LogicalDatastoreType datastoreType;
    private final X delegate;

    TypedTransaction(final D datastore, final X delegate) {
        datastoreType = typeOf(datastore);
        this.delegate = delegate;
    }

    static final LogicalDatastoreType typeOf(final Datastore datastore) {
        if (datastore.equals(Running.VALUE)) {
            return LogicalDatastoreType.CONFIGURATION;
        } else if (datastore.equals(Operational.VALUE)) {
            return LogicalDatastoreType.OPERATIONAL;
        } else {
            throw new IllegalArgumentException("Unsupported datastore " + datastore);
        }
    }

    @Override
    protected final X delegate() {
        return delegate;
    }

    final LogicalDatastoreType getDatastoreType() {
        return datastoreType;
    }

    final <T extends DataObject> FluentFuture<QueryResult<T>> doExecute(final QueryExpression<T> query) {
        if (delegate instanceof QueryOperations queryOps) {
            return queryOps.execute(datastoreType, query);
        }
        throw new UnsupportedOperationException("Query execution requires backend support");
    }
}
