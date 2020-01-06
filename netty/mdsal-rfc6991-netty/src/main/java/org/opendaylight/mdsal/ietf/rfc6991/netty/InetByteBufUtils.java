/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.ietf.rfc6991.netty;

import com.google.common.annotations.Beta;
import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

@Beta
@NonNullByDefault
public final class InetByteBufUtils {
    private InetByteBufUtils() {
        // Hidden on purpose
    }

    public static Ipv4AddressNoZone readIpv4Adress(final ByteBuf buf) {
        return IetfInetUtil.INSTANCE.ipv4AddressNoZoneFor(buf.readInt());
    }

    public static Ipv6AddressNoZone readIpv6Address(final ByteBuf buf) {
        final byte[] bytes = new byte[16];
        buf.readBytes(bytes);
        return IetfInetUtil.INSTANCE.ipv6AddressNoZoneFor(bytes);
    }

    public static void writeIpv4Address(final ByteBuf buf, final Ipv4Address addr) {
        buf.writeInt(IetfInetUtil.INSTANCE.ipv4AddressBits(addr));
    }

    public static void writeIpv4Address(final ByteBuf buf, final Ipv4AddressNoZone addr) {
        buf.writeInt(IetfInetUtil.INSTANCE.ipv4AddressNoZoneBits(addr));
    }

    public static void writeIpv6Address(final ByteBuf buf, final Ipv6Address addr) {
        buf.writeBytes(IetfInetUtil.INSTANCE.ipv6AddressBytes(addr));
    }

    public static void writeIpv6Address(final ByteBuf buf, final Ipv6AddressNoZone addr) {
        buf.writeBytes(IetfInetUtil.INSTANCE.ipv6AddressNoZoneBytes(addr));
    }
}
