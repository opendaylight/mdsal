/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

/**
 * Utility {@link DOMDataBroker} implementation which forwards all interface method invocation to a delegate instance.
 */
public abstract class ForwardingDOMDataBroker extends ForwardingDOMService<DOMDataBroker, DOMDataBroker.Extension>
        implements DOMDataBroker {
    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        return delegate().newReadOnlyTransaction();
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        return delegate().newWriteOnlyTransaction();
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        return delegate().newReadWriteTransaction();
    }

    @Override
    public DOMTransactionChain createTransactionChain() {
        return delegate().createTransactionChain();
    }

    @Override
    public DOMTransactionChain createMergingTransactionChain() {
        return delegate().createMergingTransactionChain();
    }
}
