/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

@ExtendWith(MockitoExtension.class)
class AbstractDOMForwardedTransactionTest {
    @Mock
    private DOMStoreTransaction configTx;
    @Mock
    private DOMStoreTransaction operationalTx;

    @Test
    void closeSubtransactionsTest() {
        doThrow(UnsupportedOperationException.class).when(configTx).close();

        final var forwardedTx = new DOMForwardedTransactionTestImpl("test", type ->
            switch (type) {
                case CONFIGURATION -> configTx;
                case OPERATIONAL -> operationalTx;
            });

        forwardedTx.closeSubtransactions(); // no backingTx yet -> close do nothing ignored

        forwardedTx.getSubtransaction(CONFIGURATION); // backingTx created, exception on close is expected

        final var ex = assertThrows(IllegalStateException.class,
            forwardedTx::closeSubtransactions);
        assertEquals("Uncaught exception occurred during closing transaction", ex.getMessage());
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    @Test
    void datastoreMismatchOnGetSubtransaction() {
        final var forwardedTx = new DOMForwardedTransactionTestImpl("test", type ->
            switch (type) {
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
