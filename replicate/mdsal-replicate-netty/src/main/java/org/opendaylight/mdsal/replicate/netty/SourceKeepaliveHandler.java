/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Verify.verify;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceKeepaliveHandler extends ChannelDuplexHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SourceKeepaliveHandler.class);

    private final AtomicInteger pingsSinceLastContact = new AtomicInteger();
    private final int maxMissedKeepalives;

    public SourceKeepaliveHandler(final int maxMissedKeepalives) {
        this.maxMissedKeepalives = maxMissedKeepalives;
    }

    /**
     * Intercept messages from the Sink. If the message is non-empty (it doesn't matter whether it is PONG or
     * anything else), the connection seems to be alive. Reset the pingsSinceLastContact counter and forward the
     * message further.
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ByteBuf msgBuf = (ByteBuf)msg;
        verify(msgBuf.isReadable(), "Empty message received");
        pingsSinceLastContact.set(0);
        ctx.fireChannelRead(msg);
    }

    /**
     * If the IdleStateEvent was fired, it means Source hasn't written anything to the Sink for the duration specified
     * by the keepalive-interval. PING will be sent and pingsSinceLastContact incremented. If the pingsSinceLastContact
     * reaches max-missed-keepalives exception will be raised and channel closed.
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            LOG.trace("IdleStateEvent received. Sending PING to sink");
            if (pingsSinceLastContact.incrementAndGet() > maxMissedKeepalives) {
                ctx.fireExceptionCaught(new KeepaliveException(maxMissedKeepalives));
            }
            ctx.channel().writeAndFlush(Constants.PING);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        LOG.warn("Closing channel due to an exception", cause);
        ctx.close();
    }
}
