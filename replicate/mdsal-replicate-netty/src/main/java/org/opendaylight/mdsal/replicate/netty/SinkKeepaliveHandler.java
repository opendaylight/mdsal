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

final class SinkKeepaliveHandler extends AbstractKeepaliveHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SinkKeepaliveHandler.class);

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            LOG.debug("IdleStateEvent received. Closing channel {}.", ctx.channel());
            ctx.close();
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
