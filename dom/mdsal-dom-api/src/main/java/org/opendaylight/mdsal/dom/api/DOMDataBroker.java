/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.mdsal.common.api.AsyncDataBroker;
import org.opendaylight.mdsal.common.api.TransactionChainFactory;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Data Broker which provides data transaction and data change listener functionality
 * using {@link NormalizedNode} data format. This interface is type capture of generic
 * interfaces and returns type captures of results for client-code convenience.
 */
public interface DOMDataBroker extends
        AsyncDataBroker<YangInstanceIdentifier, NormalizedNode<?, ?>>,
        TransactionChainFactory<YangInstanceIdentifier, NormalizedNode<?, ?>>,
        DOMExtensibleService<DOMDataBroker, DOMDataBrokerExtension> {

    @Override
    DOMDataTreeReadTransaction newReadOnlyTransaction();

    @Override
    DOMDataTreeWriteTransaction newWriteOnlyTransaction();

    @Override
    DOMDataTreeReadWriteTransaction newReadWriteTransaction();

    @Override
    DOMTransactionChain createTransactionChain(TransactionChainListener listener);
}
