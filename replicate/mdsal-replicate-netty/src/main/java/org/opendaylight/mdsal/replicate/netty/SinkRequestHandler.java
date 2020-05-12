/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.replicate.common.DataTreeCandidateUtils;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ReusableStreamReceiver;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.codec.binfmt.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SinkRequestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(SinkRequestHandler.class);

    private final ReusableStreamReceiver receiver = ReusableImmutableNormalizedNodeStreamWriter.create();
    private final List<ByteBuf> chunks = new ArrayList<>();
    private final DOMDataTreeIdentifier tree;
    private final DOMTransactionChain chain;

    SinkRequestHandler(final DOMDataTreeIdentifier tree, final DOMTransactionChain chain) {
        this.tree = requireNonNull(tree);
        this.chain = requireNonNull(chain);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf msg) throws IOException {
        verify(msg.isReadable(), "Empty message received");

        final short msgType = msg.readUnsignedByte();
        final Channel channel = ctx.channel();
        LOG.trace("Channel {} received message type {}", channel, msgType);
        switch (msgType) {
            case Constants.MSG_EMPTY_DATA:
                handleEmptyData();
                break;
            case Constants.MSG_DTC_CHUNK:
                chunks.add(msg);
                break;
            case Constants.MSG_DTC_APPLY:
                handleDtcApply();
                break;
            default:
                throw new IllegalStateException("Unexpected message type " + msgType);
        }
    }

    private void handleEmptyData() {
        final DOMDataTreeWriteTransaction tx = chain.newWriteOnlyTransaction();
        tx.delete(tree.getDatastoreType(), tree.getRootIdentifier());
        tx.commit();
    }

    private void handleDtcApply() throws IOException {
        checkState(!chunks.isEmpty(), "No chunks to apply");

        final ByteBuf bufs = Unpooled.wrappedBuffer(chunks.toArray(new ByteBuf[0]));
        chunks.clear();

        final DataTreeCandidate candidate;
        try (ByteBufInputStream stream = new ByteBufInputStream(bufs)) {
            candidate = DataTreeCandidateInputOutput.readDataTreeCandidate(NormalizedNodeDataInput.newDataInput(stream),
                receiver);
        }

        final DOMDataTreeWriteTransaction tx = chain.newWriteOnlyTransaction();
        DataTreeCandidateUtils.applyToTransaction(tx, tree.getDatastoreType(), candidate);
        tx.commit();
    }
}
