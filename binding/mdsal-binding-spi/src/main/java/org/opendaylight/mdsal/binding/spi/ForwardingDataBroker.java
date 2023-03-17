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
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceWildcard;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

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
    public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
            registerDataTreeChangeListener(final DataTreeIdentifier<T> treeId, final L listener) {
        return delegate().registerDataTreeChangeListener(treeId, listener);
    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> @NonNull ListenerRegistration<L>
        registerDataTreeChangeListener(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull L listener) {
        return delegate().registerDataTreeChangeListener(store, path, listener);
    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> @NonNull ListenerRegistration<L>
        registerDataTreeChangeListener(@NonNull LogicalDatastoreType store, @NonNull InstanceWildcard<T> path,
            @NonNull L listener) {
        return delegate().registerDataTreeChangeListener(store, path, listener);
    }

    @Override
    public TransactionChain createTransactionChain(final TransactionChainListener listener) {
        return delegate().createTransactionChain(listener);
    }

    @Override
    public TransactionChain createMergingTransactionChain(final TransactionChainListener listener) {
        return delegate().createMergingTransactionChain(listener);
    }
}
