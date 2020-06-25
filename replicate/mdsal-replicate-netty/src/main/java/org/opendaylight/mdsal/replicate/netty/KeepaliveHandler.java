/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Preconditions.checkNotNull;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepaliveHandler extends ChannelDuplexHandler {
    private static final Logger LOG = LoggerFactory.getLogger(KeepaliveHandler.class);

    private final Runnable reconnect;

    public KeepaliveHandler(final Runnable reconnectCallback) {
        checkNotNull(reconnectCallback, "Reconnect callback should not be null");
        this.reconnect = reconnectCallback;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(Constants.PING);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.warn("There was an exception on the channel. Reconnecting. Cause: {}", cause.getMessage());
        this.reconnect.run();
    }
}
