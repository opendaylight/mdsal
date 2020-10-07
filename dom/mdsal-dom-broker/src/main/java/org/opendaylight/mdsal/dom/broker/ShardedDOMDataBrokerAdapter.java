/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.mdsal.dom.spi.PingPongMergingDOMDataBroker;

public class ShardedDOMDataBrokerAdapter implements PingPongMergingDOMDataBroker {
    private final AtomicLong chainNum = new AtomicLong();
    private final AtomicLong txNum = new AtomicLong();
    private final DOMDataTreeService service;

    public ShardedDOMDataBrokerAdapter(final DOMDataTreeService service) {
        this.service = service;
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
    public DOMTransactionChain createTransactionChain(final DOMTransactionChainListener listener) {
        return new ShardedDOMTransactionChainAdapter(newChainIdentifier(), service, listener);
    }

    private Object newTransactionIdentifier() {
        return "DOM-" + txNum.getAndIncrement();
    }

    private Object newChainIdentifier() {
        return "DOM-CHAIN-" + chainNum;
    }
}
