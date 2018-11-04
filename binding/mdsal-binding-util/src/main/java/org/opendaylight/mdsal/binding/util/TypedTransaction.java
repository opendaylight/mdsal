/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

abstract class TypedTransaction<D extends Datastore, X extends Transaction> implements Transaction {
    private final LogicalDatastoreType datastoreType;
    private final X delegate;

    TypedTransaction(final Class<D> datastoreType, final X delegate) {
        this.datastoreType = Datastore.toType(datastoreType);
        this.delegate = delegate;
    }

    @Override
    public final Object getIdentifier() {
        return delegate.getIdentifier();
    }

    final LogicalDatastoreType getDatastoreType() {
        return this.datastoreType;
    }

    final X delegate() {
        return delegate;
    }
}
