/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

public class AbstractDOMForwardedCompositeTransactionTest {

    private static final DOMStoreTransaction FAIL_TX1 = mock(DOMStoreTransaction.class);
    private static final DOMStoreTransaction FAIL_TX2 = mock(DOMStoreTransaction.class);

    @Test(expected = IllegalStateException.class)
    public void closeSubtransactionsTest() throws Exception {
        doThrow(UnsupportedOperationException.class).when(FAIL_TX1).close();
        doThrow(UnsupportedOperationException.class).when(FAIL_TX2).close();

        final AbstractDOMForwardedCompositeTransaction<?, ?> domForwardedCompositeTransaction =
                new DOMForwardedCompositeTransactionTestImpl("testIdent",
                        ImmutableMap.of("testKey1", FAIL_TX1, "testKey2", FAIL_TX2));

        domForwardedCompositeTransaction.closeSubtransactions();
    }

    private static final class DOMForwardedCompositeTransactionTestImpl
            extends AbstractDOMForwardedCompositeTransaction<String, DOMStoreTransaction> {

        DOMForwardedCompositeTransactionTestImpl(final Object identifier,
                                                 final Map<String, DOMStoreTransaction> backingTxs) {
            super(identifier, backingTxs);
        }
    }
}