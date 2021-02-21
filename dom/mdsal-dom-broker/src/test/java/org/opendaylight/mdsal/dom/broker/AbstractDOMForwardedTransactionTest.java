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

import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractDOMForwardedTransactionTest {
    @Mock
    public DOMStoreTransaction configTx;
    @Mock
    public DOMStoreTransaction operationalTx;

    @Test
    public void closeSubtransactionsTest() throws Exception {
        doThrow(UnsupportedOperationException.class).when(configTx).close();

        final var forwardedTx = new DOMForwardedTransactionTestImpl("test",
            type -> switch (type) {
                case CONFIGURATION -> configTx;
                case OPERATIONAL -> operationalTx;
            });

        forwardedTx.closeSubtransactions(); // no backingTx yet -> close do nothing ignored

        forwardedTx.getSubtransaction(CONFIGURATION); // backingTx created, exception on close is expected

        final var ex = assertThrows(IllegalStateException.class,
            forwardedTx::closeSubtransactions);
        assertEquals("Uncaught exception occurred during closing transaction", ex.getMessage());
        assertThat(ex.getCause(), instanceOf(UnsupportedOperationException.class));
    }

    @Test
    public void datastoreMismatchOnGetSubtransaction() {
        final var forwardedTx = new DOMForwardedTransactionTestImpl("test",
            type -> switch (type) {
                case CONFIGURATION -> configTx;
                case OPERATIONAL -> operationalTx;
            });

        assertEquals(configTx, forwardedTx.getSubtransaction(CONFIGURATION));
        final var exception = assertThrows(TransactionDatastoreMismatchException.class,
                () -> forwardedTx.getSubtransaction(OPERATIONAL));
        assertEquals(CONFIGURATION, exception.expected());
        assertEquals(OPERATIONAL, exception.encountered());
    }

    private static final class DOMForwardedTransactionTestImpl
            extends AbstractDOMForwardedTransaction<DOMStoreTransaction> {
        DOMForwardedTransactionTestImpl(final Object identifier,
                final Function<LogicalDatastoreType, DOMStoreTransaction> backingTxFactory) {
            super(identifier, backingTxFactory);
        }
    }
}
