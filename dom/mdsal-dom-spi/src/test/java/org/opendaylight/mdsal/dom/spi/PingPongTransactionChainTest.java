/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadOperations;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteOperations;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PingPongTransactionChainTest {
    @Mock
    public FutureCallback<Empty> listener;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    public DOMTransactionChain chain;
    @Mock
    public DOMDataTreeReadWriteTransaction rwTx;
    @Mock
    public DOMDataTreeReadWriteTransaction rwTx1;
    @Mock
    public DOMDataTreeReadWriteTransaction rwTx2;

    private final SettableFuture<Empty> future = SettableFuture.create();

    public PingPongTransactionChain pingPong;

    @Before
    public void before() {
        doReturn(future).when(chain).future();
        pingPong = new PingPongTransactionChain(chain);
        doReturn(rwTx).when(chain).newReadWriteTransaction();
    }

    @Test
    public void testIdleClose() {
        doNothing().when(chain).close();
        pingPong.close();
        verify(chain).close();
        pingPong.addCallback(listener);

        future.set(Empty.value());
        verify(listener).onSuccess(Empty.value());
    }

    @Test
    public void testIdleFailure() {
        final var cause = new Throwable();
        doNothing().when(listener).onFailure(cause);
        doReturn("mock").when(chain).toString();

        future.setException(cause);
        pingPong.addCallback(listener);
        verify(listener).onFailure(cause);
    }

    @Test
    public void testReadOnly() {
        final var tx = pingPong.newReadOnlyTransaction();
        assertGetIdentifier(tx);
        assertReadOperations(tx);
        assertCommit(tx::close);
    }

    @Test
    public void testReadWrite() {
        final var tx = pingPong.newReadWriteTransaction();
        assertGetIdentifier(tx);
        assertReadOperations(tx);
        assertWriteOperations(tx);
        assertCommit(tx::commit);
    }

    @Test
    public void testWriteOnly() {
        final var tx = pingPong.newWriteOnlyTransaction();
        assertGetIdentifier(tx);
        assertWriteOperations(tx);
        assertCommit(tx::commit);
    }

    private void assertGetIdentifier(final DOMDataTreeTransaction tx) {
        final var id = mock(Object.class);
        doReturn(id).when(rwTx).getIdentifier();
        assertSame(id, tx.getIdentifier());
    }

    private void assertReadOperations(final DOMDataTreeReadOperations tx) {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(rwTx).exists(
            LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        final var exists = tx.exists(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        verify(rwTx).exists(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        assertEquals(Boolean.TRUE, assertDone(exists));

        doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(rwTx).read(
            LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        final var read = tx.read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        verify(rwTx).read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        assertEquals(Optional.empty(), assertDone(read));
    }

    private void assertWriteOperations(final DOMDataTreeWriteOperations tx) {
        doNothing().when(rwTx).delete(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        tx.delete(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());
        verify(rwTx).delete(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of());

        final var data = mock(ContainerNode.class);
        doNothing().when(rwTx).merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of(), data);
        tx.merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of(), data);
        verify(rwTx).merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of(), data);

        doNothing().when(rwTx).put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of(), data);
        tx.put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of(), data);
        verify(rwTx).put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.of(), data);
    }

    private void assertCommit(final Runnable commitMethod) {
        doReturn(CommitInfo.emptyFluentFuture()).when(rwTx).commit();
        commitMethod.run();
        verify(rwTx).commit();
    }

    @Test
    public void testCommitFailure() {
        assertCommitFailure(() -> { });
    }

    @Test
    public void testCommitFailureAfterClose() {
        assertCommitFailure(() -> {
            doNothing().when(chain).close();
            pingPong.close();
            verify(chain).close();
        });
    }

    private void assertCommitFailure(final Runnable asyncAction) {
        final var tx = pingPong.newWriteOnlyTransaction();

        final var rwTxFuture = SettableFuture.<CommitInfo>create();
        doReturn(FluentFuture.from(rwTxFuture)).when(rwTx).commit();

        final var txFuture = tx.commit();
        verify(rwTx).commit();
        assertFalse(txFuture.isDone());

        asyncAction.run();

        final var cause = new TransactionCommitFailedException("cause");
        rwTxFuture.setException(cause);
        assertSame(cause, assertThrows(ExecutionException.class, () -> Futures.getDone(txFuture)).getCause());
    }

    @Test
    public void testSimpleCancelFalse() {
        assertSimpleCancel(false);
    }

    @Test
    public void testSimpleCancelTrue() {
        assertSimpleCancel(true);
    }

    private void assertSimpleCancel(final boolean result) {
        final var tx = pingPong.newWriteOnlyTransaction();

        doReturn(result).when(rwTx).cancel();
        assertEquals(result, tx.cancel());
        verify(rwTx).cancel();
    }

    @Test
    public void testNewAfterSuccessfulCancel() {
        doReturn(true).when(rwTx).cancel();
        pingPong.newWriteOnlyTransaction().cancel();
        assertNotNull(pingPong.newWriteOnlyTransaction());
    }

    @Test
    public void testNewAfterNew() {
        assertNotNull(pingPong.newWriteOnlyTransaction());
        doReturn(true).when(rwTx).cancel();
        doReturn("mock").when(rwTx).toString();
        final var ex = assertThrows(IllegalStateException.class, () -> pingPong.newWriteOnlyTransaction());
        assertThat(ex.getMessage(), allOf(
            startsWith("New transaction PingPongTransaction"),
            containsString(" raced with transaction PingPongTransaction")));
    }

    @Test
    public void testReadWriteReuse() {
        final var tx = pingPong.newReadWriteTransaction();
        final var rwTxFuture = SettableFuture.<CommitInfo>create();
        doReturn(FluentFuture.from(rwTxFuture)).when(rwTx).commit();
        // Now rwTx is inflight, but does not commit immediately
        final var txFuture = tx.commit();
        verify(rwTx).commit();

        // Assert identity without delving into details
        final var id = mock(Object.class);
        doReturn(id).when(rwTx).getIdentifier();
        assertSame(tx.getIdentifier(), id);

        doReturn(rwTx1).when(chain).newReadWriteTransaction();
        final var tx1 = pingPong.newWriteOnlyTransaction();
        // now rwTx1 is ready, waiting for inflight to be completed
        final var tx1Future = tx1.commit();

        final var id1 = mock(Object.class);
        doReturn(id1).when(rwTx1).getIdentifier();
        assertSame(tx1.getIdentifier(), id1);

        // Ready transaction is picked up by fast path allocation
        final var tx2 = pingPong.newWriteOnlyTransaction();
        assertSame(tx2.getIdentifier(), id1);

        // Complete inflight transaction...
        rwTxFuture.set(CommitInfo.empty());
        assertDone(txFuture);
        // ... but we are still holding the follow-up frontend transaction ...
        assertFalse(tx1Future.isDone());
        verify(rwTx1, never()).commit();

        // ... and it will commit once we commit tx2 ...
        doReturn(CommitInfo.emptyFluentFuture()).when(rwTx1).commit();
        final var tx2Future = tx2.commit();
        // ... at which point both complete
        assertDone(tx1Future);
        assertDone(tx2Future);
    }

    @Test
    public void commitWhileInflight() {
        final var tx = pingPong.newReadWriteTransaction();

        final var rwTxFuture = SettableFuture.<CommitInfo>create();
        doReturn(FluentFuture.from(rwTxFuture)).when(rwTx).commit();
        // rwTxFuture is inflight
        final var txFuture = tx.commit();
        verify(rwTx).commit();
        assertFalse(txFuture.isDone());

        doReturn(rwTx1).when(chain).newReadWriteTransaction();
        final var rwTxFuture1 = SettableFuture.<CommitInfo>create();
        final var tx1 = pingPong.newWriteOnlyTransaction();
        final var tx1Future = tx1.commit();

        doReturn(FluentFuture.from(rwTxFuture1)).when(rwTx1).commit();
        rwTxFuture.set(CommitInfo.empty());
        assertDone(txFuture);
        verify(rwTx1).commit();

        rwTxFuture1.set(CommitInfo.empty());
        assertDone(tx1Future);
    }

    @Test
    public void testNewAfterAsyncShutdown() {
        // Setup inflight transaction
        final var tx = pingPong.newReadWriteTransaction();
        final var rwTxFuture = SettableFuture.<CommitInfo>create();
        doReturn(FluentFuture.from(rwTxFuture)).when(rwTx).commit();
        final var txFuture = tx.commit();
        assertFalse(txFuture.isDone());

        // Setup ready transaction
        doReturn(rwTx1).when(chain).newReadWriteTransaction();
        final var rwTx1Future = SettableFuture.<CommitInfo>create();
        doReturn(FluentFuture.from(rwTx1Future)).when(rwTx1).commit();

        final var tx1Future = pingPong.newReadWriteTransaction().commit();
        assertFalse(tx1Future.isDone());

        pingPong.close();

        final var ex = assertThrows(IllegalStateException.class, pingPong::newWriteOnlyTransaction);
        assertThat(ex.getMessage(), allOf(startsWith("Transaction chain "), endsWith(" has been shut down")));
        doNothing().when(chain).close();
        rwTxFuture.set(CommitInfo.empty());
        assertDone(txFuture);
        verify(chain).close();

        rwTx1Future.set(CommitInfo.empty());
        assertDone(tx1Future);
    }

    @Test
    public void testIdempotentClose() {
        doNothing().when(chain).close();
        pingPong.close();
        verify(chain).close();
        pingPong.close();
//        verifyNoMoreInteractions(chain);
    }

    private static <T> T assertDone(final FluentFuture<T> future) {
        try {
            return Futures.getDone(future);
        } catch (ExecutionException e) {
            throw new AssertionError(e);
        }
    }
}
