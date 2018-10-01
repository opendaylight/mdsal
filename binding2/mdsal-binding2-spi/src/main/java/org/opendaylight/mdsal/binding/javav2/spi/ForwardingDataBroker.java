/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.spi;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataBroker;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeListener;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeLoopException;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.javav2.api.ReadTransaction;
import org.opendaylight.mdsal.binding.javav2.api.TransactionChain;
import org.opendaylight.mdsal.binding.javav2.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
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
    public WriteTransaction newWriteOnlyTransaction() {
        return delegate().newWriteOnlyTransaction();
    }

    @Nonnull
    @Override
    public <T extends DataTreeListener> ListenerRegistration<T> registerListener(@Nonnull final T listener,
            @Nonnull final Collection<DataTreeIdentifier<?>> subtrees, final boolean allowRxMerges,
            @Nonnull final Collection<DataTreeProducer> producers) throws DataTreeLoopException {
        return delegate().registerListener(listener, subtrees, allowRxMerges, producers);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        return delegate().createProducer(subtrees);
    }

    @Override
    public TransactionChain createTransactionChain(final TransactionChainListener listener) {
        return delegate().createTransactionChain(listener);
    }
}
