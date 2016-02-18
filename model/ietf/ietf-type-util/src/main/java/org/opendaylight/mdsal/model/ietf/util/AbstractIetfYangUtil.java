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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.util.StringValueObjectFactory;

/**
 * Abstract utility class for dealing with MAC addresses as defined in the ietf-yang-types model. This class is
 * used by revision-specific classes.
 */
@Beta
public abstract class AbstractIetfYangUtil<T> {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private final StringValueObjectFactory<T> factory;

    protected AbstractIetfYangUtil(final Class<T> clazz) {
        this.factory = StringValueObjectFactory.create(clazz, "00:00:00:00:00:00");
    }

    private static final void appendHexByte(final StringBuilder sb, final byte b) {
        final int v = Byte.toUnsignedInt(b);
        sb.append(HEX_CHARS[v >>> 4]);
        sb.append(HEX_CHARS[v &  15]);
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
        Preconditions.checkArgument(bytes.length == 6, "MAC address should have 6 bytes, not %s", bytes.length);

        final StringBuilder sb = new StringBuilder(17);
        appendHexByte(sb, bytes[0]);
        for (int i = 1; i < bytes.length; ++i) {
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
        } else {
            return macAddress;
        }
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

    protected abstract String getValue(T macAddress);
}
