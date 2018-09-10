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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation of {@link TypedReadTransaction}.
 *
 * @param <D> The datastore which the transaction targets.
 */
class TypedReadTransactionImpl<D extends Datastore> extends TypedTransaction<D>
        implements TypedReadTransaction<D> {
    private final ReadTransaction delegate;

    TypedReadTransactionImpl(Class<D> datastoreType, ReadTransaction realTx) {
        super(datastoreType);
        this.delegate = realTx;
    }

    @Override
    public <T extends DataObject> FluentFuture<Optional<T>> read(InstanceIdentifier<T> path) {
        return FluentFuture.from(delegate.read(getDatastoreType(), path));
    }

    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }
}
