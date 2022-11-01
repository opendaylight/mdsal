/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractDOMForwardedCompositeTransactionTest {
    @Mock
    public DOMStoreTransaction configTx;
    @Mock
    public DOMStoreTransaction operationalTx;

    @Test
    public void closeSubtransactionsTest() throws Exception {
        doThrow(UnsupportedOperationException.class).when(configTx).close();
        doThrow(UnsupportedOperationException.class).when(operationalTx).close();

        final var domForwardedCompositeTransaction = new DOMForwardedCompositeTransactionTestImpl("test",
                Map.of(CONFIGURATION, configTx, OPERATIONAL, operationalTx));

        final var ex = assertThrows(IllegalStateException.class,
                domForwardedCompositeTransaction::closeSubtransactions);
        assertEquals("Uncaught exception occurred during closing transaction", ex.getMessage());
        assertThat(ex.getCause(), instanceOf(UnsupportedOperationException.class));
    }

    @Test
    public void datastoreMismatchOnGetSubtransaction() {
        final var compositeTx1 = new DOMForwardedCompositeTransactionTestImpl("test",
                Map.of(CONFIGURATION, configTx, OPERATIONAL, operationalTx));

        assertEquals(configTx, compositeTx1.getSubtransaction(CONFIGURATION));
        final var exception = assertThrows(TransactionDatastoreMismatchException.class,
                () -> compositeTx1.getSubtransaction(OPERATIONAL));
        assertEquals(CONFIGURATION, exception.expected());
        assertEquals(OPERATIONAL, exception.encountered());
    }

    private static final class DOMForwardedCompositeTransactionTestImpl
            extends AbstractDOMForwardedCompositeTransaction<DOMStoreTransaction> {
        DOMForwardedCompositeTransactionTestImpl(final Object identifier,
                final Map<LogicalDatastoreType, DOMStoreTransaction> backingTxs) {
            super(identifier, backingTxs);
        }
    }
}