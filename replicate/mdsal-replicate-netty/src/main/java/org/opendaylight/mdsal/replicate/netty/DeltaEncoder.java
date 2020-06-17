/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.io.IOException;
import java.util.List;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;

final class DeltaEncoder extends MessageToMessageEncoder<AbstractSourceMessage> {
    private final NormalizedNodeStreamVersion version;

    DeltaEncoder(final NormalizedNodeStreamVersion version) {
        this.version = requireNonNull(version);
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final AbstractSourceMessage msg, final List<Object> out)
            throws IOException {
        msg.encodeTo(version, out);
    }
}
