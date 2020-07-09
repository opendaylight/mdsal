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
import java.util.Collection;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Final inbound handler on source side. Handles requests coming from sink and reacts to them.
 */
final class SourceRequestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(SourceRequestHandler.class);

    private final DOMDataTreeChangeService dtcs;

    private ListenerRegistration<?> reg;

    SourceRequestHandler(final DOMDataTreeChangeService dtcs) {
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
            case Constants.MSG_SUBSCRIBE_REQ:
                subscribe(channel, msg);
                break;
            case Constants.MSG_PONG:
                break;
            default:
                throw new IllegalStateException("Unexpected message type " + msgType);
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
        try (ByteBufInputStream input = new ByteBufInputStream(msg)) {
            final NormalizedNodeDataInput normalizedInput = NormalizedNodeDataInput.newDataInput(input);

            dataTree = new DOMDataTreeIdentifier(LogicalDatastoreType.readFrom(normalizedInput),
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
            public void onDataTreeChanged(final Collection<DataTreeCandidate> changes) {
                LOG.debug("Channel {} tree {} has {} changes", channel, dataTree, changes.size());
                channel.writeAndFlush(AbstractSourceMessage.of(changes));
            }
        });
    }
}
