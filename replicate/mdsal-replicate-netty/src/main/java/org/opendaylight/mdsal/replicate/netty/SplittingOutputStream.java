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
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * An OutputStream which makes sure to slice messages to maximum size.
 */
final class SplittingOutputStream extends OutputStream {
    private static final int INIT_BUF = 4096;

    static {
        verify(INIT_BUF <= Constants.LENGTH_FIELD_MAX);
    }

    private final SocketChannel channel;

    private ByteBuf buf;

    SplittingOutputStream(final SocketChannel channel) {
        this.channel = channel;

        allocBuffer();
    }

    @Override
    public void write(final int b) throws IOException {
        buf.writeByte(b);
        checkSend();
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);

        int left = len;
        int ptr = off;
        while (left > 0) {
            final int chunk = Math.min(Constants.LENGTH_FIELD_MAX - buf.writerIndex(), left);

            buf.writeBytes(b, ptr, chunk);
            ptr += chunk;
            left -= chunk;
            checkSend();
        }
    }

    @Override
    public void close() {
        if (buf.writerIndex() != 0) {
            channel.write(buf);
        }
        buf = null;
    }

    private void allocBuffer() {
        // FIXME: use buffer allocator?
        buf = Unpooled.buffer(INIT_BUF, Constants.LENGTH_FIELD_MAX);
        buf.writeByte(Constants.MSG_DTC_CHUNK);
    }

    private void checkSend() {
        if (buf.writerIndex() == Constants.LENGTH_FIELD_MAX) {
            channel.write(buf);
            allocBuffer();
        }
    }
}
