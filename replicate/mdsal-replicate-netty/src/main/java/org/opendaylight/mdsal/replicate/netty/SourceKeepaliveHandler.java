/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SourceKeepaliveHandler extends AbstractKeepaliveHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SourceKeepaliveHandler.class);

    private final int maxMissedKeepalives;

    private int pingsSinceLastContact;

    SourceKeepaliveHandler(final int maxMissedKeepalives) {
        this.maxMissedKeepalives = maxMissedKeepalives;
    }

    /**
     * Intercept messages from the Sink. Reset the pingsSinceLastContact counter and forward the message.
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        pingsSinceLastContact = 0;
        ctx.fireChannelRead(msg);
    }

    /**
     * If the IdleStateEvent was fired, it means the Source has not written anything to the Sink for the duration
     * specified by the keepalive-interval. PING will be sent and pingsSinceLastContact incremented.
     * If pingsSinceLastContact reaches max-missed-keepalives a KeepaliveException will be raised and channel closed.
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            LOG.trace("IdleStateEvent received. Sending PING to sink");
            if (pingsSinceLastContact > maxMissedKeepalives) {
                ctx.fireExceptionCaught(new KeepaliveException(maxMissedKeepalives));
            }
            ctx.channel().writeAndFlush(Constants.PING);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
