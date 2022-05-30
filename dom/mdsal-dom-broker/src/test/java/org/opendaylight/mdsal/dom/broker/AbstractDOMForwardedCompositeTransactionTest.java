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
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractDOMForwardedCompositeTransactionTest {
    @Mock
    public DOMStoreTransaction failTx1;
    @Mock
    public DOMStoreTransaction failTx2;

    @Test
    public void closeSubtransactionsTest() throws Exception {
        doThrow(UnsupportedOperationException.class).when(failTx1).close();
        doThrow(UnsupportedOperationException.class).when(failTx2).close();

        final var domForwardedCompositeTransaction = new DOMForwardedCompositeTransactionTestImpl("testIdent",
            ImmutableMap.of(LogicalDatastoreType.CONFIGURATION, failTx1, LogicalDatastoreType.OPERATIONAL, failTx2));

        final var ex = assertThrows(IllegalStateException.class,
            domForwardedCompositeTransaction::closeSubtransactions);
        assertEquals("Uncaught exception occured during closing transaction", ex.getMessage());
        assertThat(ex.getCause(), instanceOf(UnsupportedOperationException.class));
    }

    private static final class DOMForwardedCompositeTransactionTestImpl
            extends AbstractDOMForwardedCompositeTransaction<DOMStoreTransaction> {
        DOMForwardedCompositeTransactionTestImpl(final Object identifier,
                                                 final Map<LogicalDatastoreType, DOMStoreTransaction> backingTxs) {
            super(identifier, backingTxs);
        }
    }
}