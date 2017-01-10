/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

public class ShardedDOMDataBrokerAdapter implements DOMDataBroker {

    private final DOMDataTreeService service;
    private final AtomicLong txNum = new AtomicLong();
    private final AtomicLong chainNum = new AtomicLong();

    public ShardedDOMDataBrokerAdapter(final DOMDataTreeService service) {
        this.service = service;
    }

    @Override
    public Map<Class<? extends DOMDataBrokerExtension>, DOMDataBrokerExtension> getSupportedExtensions() {
        return Collections.emptyMap();
    }

    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        return new ShardedDOMReadTransactionAdapter(newTransactionIdentifier(), service);
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        return new ShardedDOMWriteTransactionAdapter(newTransactionIdentifier(), service);
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        return new ShardedDOMReadWriteTransactionAdapter(newTransactionIdentifier(), service);
    }

    @Override
    public DOMTransactionChain createTransactionChain(final TransactionChainListener listener) {
        return new ShardedDOMTransactionChainAdapter(newChainIdentifier(), service, listener);
    }

    private Object newTransactionIdentifier() {
        return "DOM-" + txNum.getAndIncrement();
    }

    private Object newChainIdentifier() {
        return "DOM-CHAIN-" + chainNum;
    }
}
