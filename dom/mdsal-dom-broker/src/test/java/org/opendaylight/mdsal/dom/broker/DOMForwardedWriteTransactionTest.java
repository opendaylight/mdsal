/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DOMForwardedWriteTransactionTest {
    @Mock
    private AbstractDOMForwardedTransactionFactory<?> abstractDOMForwardedTransactionFactory;

    @Mock
    private DOMStoreWriteTransaction domStoreWriteTransaction;

     @Test
    public void readyRuntimeExceptionAndCancel() throws InterruptedException {
        final RuntimeException thrown = new RuntimeException();
        doThrow(thrown).when(domStoreWriteTransaction).ready();
        final DOMForwardedWriteTransaction<DOMStoreWriteTransaction> domForwardedWriteTransaction =
            new DOMForwardedWriteTransaction<>(new Object(),
                Map.of(LogicalDatastoreType.OPERATIONAL, domStoreWriteTransaction),
                abstractDOMForwardedTransactionFactory);
        ListenableFuture<?> submitFuture = domForwardedWriteTransaction.commit();

        ExecutionException ex = assertThrows(ExecutionException.class, submitFuture::get);
        Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(TransactionCommitFailedException.class));
        assertSame(thrown, cause.getCause());
        domForwardedWriteTransaction.cancel();
    }

    @Test
    public void submitRuntimeExceptionAndCancel() throws InterruptedException {
        RuntimeException thrown = new RuntimeException();
        doReturn(null).when(domStoreWriteTransaction).ready();
        doThrow(thrown).when(abstractDOMForwardedTransactionFactory).commit(any(), any());
        DOMForwardedWriteTransaction<DOMStoreWriteTransaction> domForwardedWriteTransaction =
                new DOMForwardedWriteTransaction<>(
                        new Object(),
                        Map.of(LogicalDatastoreType.OPERATIONAL, domStoreWriteTransaction),
                        abstractDOMForwardedTransactionFactory);
        ListenableFuture<?> submitFuture = domForwardedWriteTransaction.commit();
        ExecutionException ex = assertThrows(ExecutionException.class, submitFuture::get);
        Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(TransactionCommitFailedException.class));
        assertSame(thrown, cause.getCause());
        domForwardedWriteTransaction.cancel();
    }
}
