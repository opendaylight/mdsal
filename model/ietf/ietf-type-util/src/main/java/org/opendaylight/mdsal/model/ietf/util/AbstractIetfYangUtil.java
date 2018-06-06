/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.util.StringValueObjectFactory;

/**
 * Abstract utility class for dealing with MAC addresses as defined in the ietf-yang-types model. This class is
 * used by revision-specific classes.
 */
@Beta
public abstract class AbstractIetfYangUtil<T> {
    private static final int MAC_BYTE_LENGTH = 6;
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final byte[] HEX_VALUES;
    static {
        final byte[] b = new byte['f' + 1];
        Arrays.fill(b, (byte)-1);

        for (char c = '0'; c <= '9'; ++c) {
            b[c] = (byte)(c - '0');
        }
        for (char c = 'A'; c <= 'F'; ++c) {
            b[c] = (byte)(c - 'A' + 10);
        }
        for (char c = 'a'; c <= 'f'; ++c) {
            b[c] = (byte)(c - 'a' + 10);
        }

        HEX_VALUES = b;
    }

    private final StringValueObjectFactory<T> factory;

    protected AbstractIetfYangUtil(final Class<T> clazz) {
        this.factory = StringValueObjectFactory.create(clazz, "00:00:00:00:00:00");
    }

    static StringBuilder appendHexByte(final StringBuilder sb, final byte b) {
        final int v = Byte.toUnsignedInt(b);
        return sb.append(HEX_CHARS[v >>> 4]).append(HEX_CHARS[v & 15]);
    }

    /**
     * Make sure an array of characters does not include capital letters. This method assumes input conforms to
     * MAC address format, e.g. it is composed of 6 groups of hexadecimal digits separated by colons. Behavior is
     * undefined if the input does not meet this criteria.
     *
     * @param chars Input characters, may not be null
     * @return True if the array has been modified
     * @throws NullPointerException if input is null
     */
    private static boolean ensureLowerCase(@Nonnull final char[] chars) {
        boolean ret = false;

        for (int i = 0; i < chars.length; ++i) {
            final char c = chars[i];
            if (c >= 'A' && c <= 'F') {
                chars[i] = Character.toLowerCase(c);
                ret = true;
            }
        }

        return ret;
    }

    /**
     * Convert an array of 6 bytes into canonical MAC address representation, that is 6 groups of two hexadecimal
     * lower-case digits each, separated by colons.
     *
     * @param bytes Input bytes, may not be null
     * @return Canonical MAC address string
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if length of input is not 6 bytes
     */
    @Nonnull private static String bytesToString(@Nonnull final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == MAC_BYTE_LENGTH, "MAC address should have 6 bytes, not %s",
                bytes.length);

        final StringBuilder sb = new StringBuilder(17);
        appendHexByte(sb, bytes[0]);
        for (int i = 1; i < MAC_BYTE_LENGTH; ++i) {
            sb.append(':');
            appendHexByte(sb, bytes[i]);
        }

        return sb.toString();
    }

    /**
     * Convert the value of a MacAddress into the canonical representation.
     *
     * @param macAddress Input MAC address
     * @return A MacAddress containing the canonical representation.
     * @throws NullPointerException if macAddress is null
     */
    @Nonnull public final T canonizeMacAddress(@Nonnull final T macAddress) {
        final char[] input = getValue(macAddress).toCharArray();
        if (ensureLowerCase(input)) {
            return factory.newInstance(input.toString());
        }

        return macAddress;
    }

    /**
     * Create a MacAddress object holding the canonical representation of the 6 bytes
     * passed in as argument.
     * @param bytes 6-byte input array
     * @return MacAddress with canonical string derived from input bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if length of input is not 6 bytes
     */
    @Nonnull public final T macAddressFor(@Nonnull final byte[] bytes) {
        return factory.newInstance(bytesToString(bytes));
    }

    static byte hexValue(final char c) {
        byte v;
        try {
            // Performance optimization: access the array and rely on the VM for catching
            // illegal access (which boils down to illegal character, which should never happen.
            v = HEX_VALUES[c];
        } catch (IndexOutOfBoundsException e) {
            v = -1;
        }

        if (v < 0) {
            throw new IllegalArgumentException("Invalid character '" + c + "' encountered");
        }

        return v;
    }

    @Nonnull public final byte[] bytesFor(@Nonnull final T macAddress) {
        final String mac = getValue(macAddress);
        final byte[] ret = new byte[MAC_BYTE_LENGTH];

        for (int i = 0, base = 0; i < MAC_BYTE_LENGTH; ++i, base += 3) {
            ret[i] = (byte) (hexValue(mac.charAt(base)) << 4 | hexValue(mac.charAt(base + 1)));
        }

        return ret;
    }

    protected abstract String getValue(T macAddress);
}
