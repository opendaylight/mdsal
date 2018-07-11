/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;

public class ShardedDOMDataBrokerAdapterTest {

    @Test
    public void basicTest() throws Exception {
        DOMDataTreeService dataTreeService = mock(DOMDataTreeService.class);
        final ShardedDOMDataBrokerAdapter shardedDOMDataBrokerAdapter =
                new ShardedDOMDataBrokerAdapter(dataTreeService);

        assertTrue(shardedDOMDataBrokerAdapter.getExtensions().isEmpty());
        assertNotNull(shardedDOMDataBrokerAdapter.newWriteOnlyTransaction());
        assertNotNull(shardedDOMDataBrokerAdapter.newReadOnlyTransaction());
        assertNotNull(shardedDOMDataBrokerAdapter.createTransactionChain(null));
    }
}