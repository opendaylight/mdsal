/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.codec.binfmt.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;

final class SourceChannelHandler extends SimpleChannelInboundHandler<ByteBuf>
        implements ClusteredDOMDataTreeChangeListener {
    private final DOMDataTreeChangeService dtcs;
    private final SocketChannel channel;

    private ListenerRegistration<?> reg;

    SourceChannelHandler(final DOMDataTreeChangeService dtcs, final SocketChannel channel) {
        this.dtcs = requireNonNull(dtcs);
        this.channel = requireNonNull(channel);
    }

    @Override
    public void onInitialData() {
        channel.eventLoop().execute(this::sendInitialData);
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeCandidate> changes) {
        channel.eventLoop().execute(() -> sendCandidates(changes));
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf msg) throws IOException {
        verify(msg.isReadable(), "Empty message received");

        final short msgType = msg.readUnsignedByte();
        switch (msgType) {
            case Constants.MSG_SUBSCRIBE_REQ:
                subscribe(ctx, msg);
                break;
            default:
                throw new IllegalStateException("Unexpected message type " + msgType);
        }
    }

    private void subscribe(final ChannelHandlerContext ctx, final ByteBuf msg) throws IOException {
        verify(reg == null, "Unexpected subscription when already subscribed");

        final DOMDataTreeIdentifier dataTree;
        try (ByteBufInputStream input = new ByteBufInputStream(msg)) {
            final NormalizedNodeDataInput normalizedInput = NormalizedNodeDataInput.newDataInput(input);

            dataTree = new DOMDataTreeIdentifier(LogicalDatastoreType.readFrom(normalizedInput),
                normalizedInput.readYangInstanceIdentifier());
        }

        ctx.writeAndFlush(Constants.SUBSCRIBE_ACK);

        reg = dtcs.registerDataTreeChangeListener(dataTree, this);
    }

    private void sendInitialData() {
        if (channel.isActive()) {
            channel.writeAndFlush(Constants.EMPTY_DATA);
        }
    }

    private void sendCandidates(final Collection<DataTreeCandidate> candidates) {
        if (channel.isActive()) {
            for (DataTreeCandidate candidate : candidates) {
                sendCandidate(candidate);
            }
            channel.flush();
        }
    }

    private void sendCandidate(final DataTreeCandidate candidate) throws IOException {
        try (DataOutputStream stream = new DataOutputStream(new SplittingOutputStream(channel))) {
            try (NormalizedNodeDataOutput output = NormalizedNodeStreamVersion.current().newDataOutput(stream)) {
                DataTreeCandidateInputOutput.writeDataTreeCandidate(output, candidate);
            }
        }
        channel.write(Constants.DTC_APPLY);
    }
}
