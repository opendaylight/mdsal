/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.collect.ForwardingObject;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Utility {@link DataBroker} implementation which forwards all interface method
 * invocation to a delegate instance.
 */
public abstract class ForwardingDataBroker extends ForwardingObject implements DataBroker {

    @Override
    protected abstract @Nonnull DataBroker delegate();

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
            registerDataTreeChangeListener(DataTreeIdentifier<T> treeId, L listener) {
        return delegate().registerDataTreeChangeListener(treeId, listener);
    }

    @Override
    public BindingTransactionChain createTransactionChain(TransactionChainListener listener) {
        return delegate().createTransactionChain(listener);
    }

}
