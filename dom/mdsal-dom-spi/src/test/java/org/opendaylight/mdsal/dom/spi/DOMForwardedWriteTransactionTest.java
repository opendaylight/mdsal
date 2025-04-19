/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;

@ExtendWith(MockitoExtension.class)
class DOMForwardedWriteTransactionTest {
    @Mock
    private AbstractDOMForwardedTransactionFactory<?> commitImpl;
    @Mock
    private DOMStoreWriteTransaction domStoreWriteTransaction;

    private Function<LogicalDatastoreType, DOMStoreWriteTransaction> backingTxFactory;

    @BeforeEach
    void beforeEach() {
        backingTxFactory = storeType -> domStoreWriteTransaction;
    }

    @Test
    void readyRuntimeExceptionAndCancel() {
        final RuntimeException thrown = new RuntimeException();
        doThrow(thrown).when(domStoreWriteTransaction).ready();
        final DOMForwardedWriteTransaction<DOMStoreWriteTransaction> domForwardedWriteTransaction =
                new DOMForwardedWriteTransaction<>(new Object(), backingTxFactory, commitImpl);
        domForwardedWriteTransaction.getSubtransaction(CONFIGURATION); // ensure backingTx initialized
        ListenableFuture<?> submitFuture = domForwardedWriteTransaction.commit();

        final var ex = assertThrows(ExecutionException.class, submitFuture::get);
        final var cause = assertInstanceOf(TransactionCommitFailedException.class, ex.getCause());
        assertSame(thrown, cause.getCause());
        domForwardedWriteTransaction.cancel();
    }

    @Test
    void submitRuntimeExceptionAndCancel() {
        RuntimeException thrown = new RuntimeException();
        doReturn(null).when(domStoreWriteTransaction).ready();
        doThrow(thrown).when(commitImpl).commit(any(), any());
        DOMForwardedWriteTransaction<DOMStoreWriteTransaction> domForwardedWriteTransaction =
                new DOMForwardedWriteTransaction<>(new Object(), backingTxFactory, commitImpl);
        domForwardedWriteTransaction.getSubtransaction(CONFIGURATION); // ensure backingTx initialized
        ListenableFuture<?> submitFuture = domForwardedWriteTransaction.commit();
        ExecutionException ex = assertThrows(ExecutionException.class, submitFuture::get);
        final var cause = assertInstanceOf(TransactionCommitFailedException.class, ex.getCause());
        assertSame(thrown, cause.getCause());
        domForwardedWriteTransaction.cancel();
    }
}
