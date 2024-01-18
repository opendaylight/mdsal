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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Final inbound handler on source side. Handles requests coming from sink and reacts to them.
 */
final class SourceRequestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(SourceRequestHandler.class);

    private final DataTreeChangeExtension dtcs;

    private Registration reg;

    SourceRequestHandler(final DataTreeChangeExtension dtcs) {
        this.dtcs = requireNonNull(dtcs);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        LOG.info("Channel {} going inactive", ctx.channel());
        if (reg != null) {
            reg.close();
            reg = null;
        }
        ctx.fireChannelInactive();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf msg) throws IOException {
        verify(msg.isReadable(), "Empty message received");

        final short msgType = msg.readUnsignedByte();
        final Channel channel = ctx.channel();
        LOG.trace("Channel {} received message type {}", channel, msgType);
        switch (msgType) {
            case Constants.MSG_SUBSCRIBE_REQ -> subscribe(channel, msg);
            case Constants.MSG_PONG -> {
                // No-op
            }
            default -> throw new IllegalStateException("Unexpected message type " + msgType);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        LOG.warn("Closing channel {} due to an error", ctx.channel(), cause);
        ctx.close();
    }

    private void subscribe(final Channel channel, final ByteBuf msg) throws IOException {
        verify(reg == null, "Unexpected subscription when already subscribed");

        final DOMDataTreeIdentifier dataTree;
        try (var input = new ByteBufInputStream(msg)) {
            final var normalizedInput = NormalizedNodeDataInput.newDataInput(input);

            dataTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.readFrom(normalizedInput),
                normalizedInput.readYangInstanceIdentifier());
        }

        LOG.info("Channel {} subscribing to {}", channel, dataTree);
        reg = dtcs.registerDataTreeChangeListener(dataTree, new ClusteredDOMDataTreeChangeListener() {
            @Override
            public void onInitialData() {
                LOG.debug("Channel {} tree {} has empty data", channel, dataTree);
                channel.writeAndFlush(AbstractSourceMessage.empty());
            }

            @Override
            public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
                LOG.debug("Channel {} tree {} has {} changes", channel, dataTree, changes.size());
                channel.writeAndFlush(AbstractSourceMessage.of(changes));
            }
        });
    }
}
