/*
 * Copyright (c) 2026 SmartOptics AS and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class SinkRequestHandlerTest {

    @Test
    void testEmptyDataCommitsRootPut() {
        final DOMTransactionChain chain = mock(DOMTransactionChain.class);
        final DOMDataTreeWriteTransaction tx = mock(DOMDataTreeWriteTransaction.class);
        when(chain.newWriteOnlyTransaction()).thenReturn(tx);

        final DOMDataTreeIdentifier tree = DOMDataTreeIdentifier.of(
                LogicalDatastoreType.CONFIGURATION,
                YangInstanceIdentifier.of());

        final SinkRequestHandler handler = new SinkRequestHandler(tree, chain);
        final EmbeddedChannel channel = new EmbeddedChannel(handler);

        final ByteBuf msg = Unpooled.buffer();
        msg.writeByte(Constants.MSG_EMPTY_DATA);

        channel.writeInbound(msg);

        verify(chain).newWriteOnlyTransaction();
        verify(tx).put(eq(tree.datastore()), eq(YangInstanceIdentifier.of()), any());
        verify(tx).commit(any(), any(Executor.class));

        channel.finishAndReleaseAll();
    }

    @Test
    void testChunkReleasedOnChannelClose() {
        final DOMTransactionChain chain = mock(DOMTransactionChain.class);
        final DOMDataTreeIdentifier tree = DOMDataTreeIdentifier.of(
                LogicalDatastoreType.CONFIGURATION,
                YangInstanceIdentifier.of());

        final SinkRequestHandler handler = new SinkRequestHandler(tree, chain);
        final EmbeddedChannel channel = new EmbeddedChannel(handler);

        final ByteBuf msg = Unpooled.buffer();
        msg.writeByte(Constants.MSG_DTC_CHUNK);
        msg.writeByte(1);

        channel.writeInbound(msg);

        // The handler retains the chunk, while SimpleChannelInboundHandler releases the original read.
        assertEquals(1, msg.refCnt());

        channel.close();

        assertEquals(0, msg.refCnt());
        channel.finishAndReleaseAll();
    }

    @Test
    void testChunkReleasedOnHandlerRemoved() {
        final DOMTransactionChain chain = mock(DOMTransactionChain.class);
        final DOMDataTreeIdentifier tree = DOMDataTreeIdentifier.of(
                LogicalDatastoreType.CONFIGURATION,
                YangInstanceIdentifier.of());

        final SinkRequestHandler handler = new SinkRequestHandler(tree, chain);
        final EmbeddedChannel channel = new EmbeddedChannel(handler);

        final ByteBuf msg = Unpooled.buffer();
        msg.writeByte(Constants.MSG_DTC_CHUNK);
        msg.writeByte(1);

        channel.writeInbound(msg);

        // The retained chunk should still be owned by the handler at this point.
        assertEquals(1, msg.refCnt());

        channel.pipeline().remove(handler);

        assertEquals(0, msg.refCnt());
        channel.finishAndReleaseAll();
    }
}