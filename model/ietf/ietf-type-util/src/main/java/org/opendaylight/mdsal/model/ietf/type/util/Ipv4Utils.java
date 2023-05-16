/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.type.util;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.jdt.annotation.NonNull;

/**
 * IPv4 address parsing for {@code ietf-inet-types} ipv4-address. This is an internal implementation class, not meant to
 * be used directly in any shape or form to the outside world, as the code relies on the fact that the strings presented
 * to it have been previously validated to conform to the regular expressions defined in the YANG model.
 */
public final class Ipv4Utils {
    public static final int INET4_LENGTH = 4;

    private Ipv4Utils() {
        // Hidden on purpose
    }

    public static void fillIpv4Bytes(final byte @NonNull[] bytes, final int byteStart, final String str,
            final int strStart, final int strLimit) {
        int out = byteStart;
        int val = 0;
        for (int i = strStart; i < strLimit; ++i) {
            final char c = str.charAt(i);
            if (c == '.') {
                bytes[out++] = (byte) val;
                val = 0;
            } else {
                val = 10 * val + c - '0';
            }
        }

        bytes[out] = (byte) val;
    }

    public static int addressBits(final String str, final int limit) {
        int prev = 0;
        int current = 0;
        for (int i = 0; i < limit; ++i) {
            final char c = str.charAt(i);
            if (c == '.') {
                prev = prev << 8 | current;
                current = 0;
            } else {
                current = 10 * current + c - '0';
            }
        }
        return prev << 8 | current;
    }

    public static byte @NonNull[] addressBytes(final String str, final int limit) {
        final byte[] bytes = new byte[4];
        Ipv4Utils.fillIpv4Bytes(bytes, 0, str, 0, limit);
        return bytes;
    }

    public static String addressString(final int bits) {
        return (bits >>> 24) + "." + (bits >>> 16 & 0xFF) + "." + (bits >>> 8 & 0xFF) + "." + (bits & 0xFF);
    }

    public static String addressString(final byte @NonNull[] bytes) {
        final StringBuilder sb = new StringBuilder(15);
        appendIpv4String(sb, bytes);
        return sb.toString();
    }

    public static void appendIpv4String(final StringBuilder sb, final byte @NonNull[] bytes) {
        checkArgument(bytes.length == INET4_LENGTH, "IPv4 address length is 4 bytes");

        sb.append(Byte.toUnsignedInt(bytes[0]));
        for (int i = 1; i < INET4_LENGTH; ++i) {
            sb.append('.').append(Byte.toUnsignedInt(bytes[i]));
        }
    }
}
