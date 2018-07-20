/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncReadWriteTransaction;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

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
    public AsyncReadWriteTransaction<InstanceIdentifier<?>, TreeNode> newReadWriteTransaction() {
        return delegate().newReadWriteTransaction();
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return delegate().newWriteOnlyTransaction();
    }

    @Nonnull
    @Override
    public <T extends DataTreeListener> ListenerRegistration<T> registerListener(@Nonnull T listener,
            @Nonnull Collection<DataTreeIdentifier<?>> subtrees, boolean allowRxMerges,
            @Nonnull Collection<DataTreeProducer> producers) throws DataTreeLoopException {
        return delegate().registerListener(listener, subtrees, allowRxMerges, producers);
    }

    @Override
    public DataTreeProducer createProducer(Collection<DataTreeIdentifier<?>> subtrees) {
        return delegate().createProducer(subtrees);
    }

    @Override
    public BindingTransactionChain createTransactionChain(TransactionChainListener listener) {
        return delegate().createTransactionChain(listener);
    }

}
