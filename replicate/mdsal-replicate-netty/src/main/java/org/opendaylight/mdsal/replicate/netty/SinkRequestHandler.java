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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.replicate.common.DataTreeCandidateUtils;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ReusableStreamReceiver;
import org.opendaylight.yangtools.yang.data.codec.binfmt.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SinkRequestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(SinkRequestHandler.class);
    private static final ContainerNode EMPTY_ROOT = ImmutableNodes.newContainerBuilder()
        .withNodeIdentifier(NodeIdentifier.create(SchemaContext.NAME))
        .build();

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
            case Constants.MSG_EMPTY_DATA -> handleEmptyData();
            case Constants.MSG_DTC_CHUNK -> chunks.add(msg.retain());
            case Constants.MSG_DTC_APPLY -> handleDtcApply();
            case Constants.MSG_PING -> {
                LOG.trace("Received PING from Source, sending PONG");
                ctx.channel().writeAndFlush(Constants.PONG);
            }
            default -> throw new IllegalStateException("Unexpected message type " + msgType);
        }
    }

    private void handleEmptyData() {
        final var tx = chain.newWriteOnlyTransaction();

        if (tree.path().isEmpty()) {
            tx.put(tree.datastore(), YangInstanceIdentifier.of(), EMPTY_ROOT);
        } else {
            tx.delete(tree.datastore(), tree.path());
        }
        commit(tx);
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
        DataTreeCandidateUtils.applyToTransaction(tx, tree.datastore(), candidate);
        commit(tx);
    }

    private static void commit(final DOMDataTreeWriteTransaction tx) {
        tx.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                LOG.trace("Transaction committed with {}", result);
            }

            @Override
            public void onFailure(final Throwable cause) {
                // Handled by transaction chain listener
            }
        }, MoreExecutors.directExecutor());
    }
}
