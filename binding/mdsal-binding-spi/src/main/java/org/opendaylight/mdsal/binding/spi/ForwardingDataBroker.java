/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import com.google.common.collect.ForwardingObject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Utility {@link DataBroker} implementation which forwards all interface method invocation to a delegate instance.
 */
public abstract class ForwardingDataBroker extends ForwardingObject implements DataBroker {
    @Override
    protected abstract @NonNull DataBroker delegate();

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return delegate().newReadOnlyTransaction();
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return delegate().newReadWriteTransaction();
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return delegate().newWriteOnlyTransaction();
    }

    @Override
    public TransactionChain createTransactionChain() {
        return delegate().createTransactionChain();
    }

    @Override
    public TransactionChain createMergingTransactionChain() {
        return delegate().createMergingTransactionChain();
    }

    @Override
    public <T extends DataObject> Registration registerTreeChangeListener(final LogicalDatastoreType datastore,
            final DataObjectReference<T> subtrees, final DataTreeChangeListener<T> listener) {
        return delegate().registerTreeChangeListener(datastore, subtrees, listener);
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public <T extends DataObject> @NonNull Registration registerLegacyTreeChangeListener(
            final LogicalDatastoreType datastore, final DataObjectReference<T> subtrees,
            final DataTreeChangeListener<T> listener) {
        return delegate().registerLegacyTreeChangeListener(datastore, subtrees, listener);
    }
}
