/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KeepaliveHandler extends ChannelDuplexHandler {
    private static final Logger LOG = LoggerFactory.getLogger(KeepaliveHandler.class);

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        LOG.info("userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            LOG.info("IdleStateEvent received. Closing channel");
            ctx.close();
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
