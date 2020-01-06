/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.uint24.netty;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.uint24.rev200104.Uint24;
import org.opendaylight.yangtools.yang.common.Uint32;

public class Uint24ByteBufUtilsTest {
    private static final Uint24 ONE_TWO_THREE = new Uint24(Uint32.valueOf(0x010203));

    @Test
    public void testRead() {
        final ByteBuf buf = Unpooled.buffer().writeMedium(0x010203);
        assertEquals(ONE_TWO_THREE, Uint24ByteBufUtils.readUint24(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    public void testWrite() {
        final ByteBuf buf = Unpooled.buffer();
        Uint24ByteBufUtils.writeUint24(buf, ONE_TWO_THREE);
        assertMedium(buf);
    }

    @Test
    public void testWriteMandatory() {
        final ByteBuf buf = Unpooled.buffer();
        Uint24ByteBufUtils.writeMandatoryUint24(buf, ONE_TWO_THREE, "foo");
        assertMedium(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryNull() {
        Uint24ByteBufUtils.writeMandatoryUint24(Unpooled.buffer(), null, "foo");
    }

    @Test
    public void testWriteOptional() {
        final ByteBuf buf = Unpooled.buffer();
        Uint24ByteBufUtils.writeOptionalUint24(buf, ONE_TWO_THREE);
        assertMedium(buf);
    }

    @Test
    public void testWriteOptionalNull() {
        final ByteBuf buf = Unpooled.buffer();
        Uint24ByteBufUtils.writeOptionalUint24(buf, null);
        assertEquals(0, buf.readableBytes());
    }

    @Test
    public void testWriteOrZero() {
        final ByteBuf buf = Unpooled.buffer();
        Uint24ByteBufUtils.writeUint24OrZero(buf, ONE_TWO_THREE);
        assertMedium(buf);
    }

    @Test
    public void testWriteOrZeroNull() {
        final ByteBuf buf = Unpooled.buffer();
        Uint24ByteBufUtils.writeUint24OrZero(buf, null);
        assertMedium(buf, 0);
    }

    private static void assertMedium(final ByteBuf buf) {
        assertMedium(buf, 0x010203);
    }

    private static void assertMedium(final ByteBuf buf, final int value) {
        assertEquals(3, buf.readableBytes());
        assertEquals(value, buf.readMedium());
    }
}
