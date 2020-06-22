/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.LengthFieldPrepender;

@Sharable
final class MessageFrameEncoder extends LengthFieldPrepender {
    static final MessageFrameEncoder INSTANCE = new MessageFrameEncoder();

    private MessageFrameEncoder() {
        super(Constants.LENGTH_FIELD_LENGTH);
    }
}
