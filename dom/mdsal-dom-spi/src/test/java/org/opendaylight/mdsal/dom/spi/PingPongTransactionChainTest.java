/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PingPongTransactionChainTest {
    @Mock
    public Function<DOMTransactionChainListener, DOMTransactionChain> delegateFactory;
    @Mock
    public DOMTransactionChainListener listener;
    @Mock
    public DOMTransactionChain chain;
    @Mock
    public DOMDataTreeReadWriteTransaction rwTx;

    public DOMTransactionChainListener pingPongListener;
    public PingPongTransactionChain pingPong;

    @Before
    public void before() {
        // Slightly complicated bootstrap
        doAnswer(invocation -> {
            pingPongListener = invocation.getArgument(0);
            return chain;
        }).when(delegateFactory).apply(any());
        pingPong = new PingPongTransactionChain(delegateFactory, listener);
        verify(delegateFactory).apply(any());

        doReturn(rwTx).when(chain).newReadWriteTransaction();
    }

    @Test
    public void testIdleClose() {
        doNothing().when(chain).close();
        pingPong.close();
        verify(chain).close();

        doNothing().when(listener).onTransactionChainSuccessful(pingPong);
        pingPongListener.onTransactionChainSuccessful(chain);
        verify(listener).onTransactionChainSuccessful(pingPong);
    }

    @Test
    public void testIdleFailure() {
        final var cause = new Throwable();
        doNothing().when(listener).onTransactionChainFailed(pingPong, null, cause);
        doReturn("mock").when(chain).toString();
        pingPongListener.onTransactionChainFailed(chain, rwTx, cause);
        verify(listener).onTransactionChainFailed(pingPong, null, cause);
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
            LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        final var exists = tx.exists(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        verify(rwTx).exists(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        assertEquals(Boolean.TRUE, assertDone(exists));

        doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(rwTx).read(
            LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        final var read = tx.read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        verify(rwTx).read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        assertEquals(Optional.empty(), assertDone(read));
    }

    private void assertWriteOperations(final DOMDataTreeWriteOperations tx) {
        doNothing().when(rwTx).delete(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        tx.delete(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        verify(rwTx).delete(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());

        final var data = mock(NormalizedNode.class);
        doNothing().when(rwTx).merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty(), data);
        tx.merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty(), data);
        verify(rwTx).merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty(), data);

        doNothing().when(rwTx).put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty(), data);
        tx.put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty(), data);
        verify(rwTx).put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty(), data);
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

    private static <T> T assertDone(final FluentFuture<T> future) {
        try {
            return Futures.getDone(future);
        } catch (ExecutionException e) {
            throw new AssertionError(e);
        }
    }
}
