/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionChainListener;

import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.mdsal.dom.api.DOMDataChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import com.google.common.collect.ForwardingObject;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Utility {@link DOMDataBroker} implementation which forwards all interface
 * method invocation to a delegate instance.
 */
public abstract class ForwardingDOMDataBroker extends ForwardingObject implements DOMDataBroker {
    @Override
    protected abstract @Nonnull DOMDataBroker delegate();

    @Override
    public ListenerRegistration<DOMDataChangeListener> registerDataChangeListener(final LogicalDatastoreType store,
            final YangInstanceIdentifier path, final DOMDataChangeListener listener,
            final DataChangeScope triggeringScope) {
        return delegate().registerDataChangeListener(store, path, listener, triggeringScope);
    }

    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        return delegate().newReadOnlyTransaction();
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        return delegate().newWriteOnlyTransaction();
    }

    @Override
    public DOMTransactionChain createTransactionChain(final TransactionChainListener listener) {
        return delegate().createTransactionChain(listener);
    }

    @Override
    public Map<Class<? extends DOMDataBrokerExtension>, DOMDataBrokerExtension> getSupportedExtensions() {
        return delegate().getSupportedExtensions();
    }
}
