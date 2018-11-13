/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class BindingDOMReadTransactionAdapter extends AbstractForwardedTransaction<DOMDataTreeReadTransaction> implements
        ReadTransaction {

    protected BindingDOMReadTransactionAdapter(final DOMDataTreeReadTransaction delegate,
            final BindingToNormalizedNodeCodec codec) {
        super(delegate, codec);
    }

    @Override
    public <T extends DataObject> FluentFuture<Optional<T>> read(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path) {
        return doRead(getDelegate(), store, path);
    }

    @Override
    public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        return doExists(getDelegate(), store, path);
    }

    @Override
    public void close() {
        getDelegate().close();
    }

}
