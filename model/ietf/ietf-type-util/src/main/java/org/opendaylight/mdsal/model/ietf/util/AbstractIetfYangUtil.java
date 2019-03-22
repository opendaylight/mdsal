/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.reflect.StringValueObjectFactory;

/**
 * Abstract utility class for dealing with MAC addresses as defined in the ietf-yang-types model. This class is
 * used by revision-specific classes.
 *
 * @param <M> mac-address type
 * @param <P> phys-address type
 */
@Beta
public abstract class AbstractIetfYangUtil<M, P> {
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

    private final StringValueObjectFactory<M> macFactory;
    private final StringValueObjectFactory<P> physFactory;

    protected AbstractIetfYangUtil(final Class<M> macClass, final Class<P> physClass) {
        this.macFactory = StringValueObjectFactory.create(macClass, "00:00:00:00:00:00");
        this.physFactory = StringValueObjectFactory.create(physClass, "00:00");
    }


    /**
     * Convert the value of a MacAddress into the canonical representation.
     *
     * @param macAddress Input MAC address
     * @return A MacAddress containing the canonical representation.
     * @throws NullPointerException if macAddress is null
     */
    public final @NonNull M canonizeMacAddress(final @NonNull M macAddress) {
        final char[] input = getValue(macAddress).toCharArray();
        return ensureLowerCase(input) ? macFactory.newInstance(String.valueOf(input)) : macAddress;
    }

    /**
     * Create a MacAddress object holding the canonical representation of the 6 bytes
     * passed in as argument.
     * @param bytes 6-byte input array
     * @return MacAddress with canonical string derived from input bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if length of input is not 6 bytes
     */
    public final @NonNull M macAddressFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == MAC_BYTE_LENGTH, "MAC address should have 6 bytes, not %s",
                bytes.length);
        return macFactory.newInstance(bytesToString(bytes, 17));
    }

    /**
     * Convert the value of a PhysAddress into the canonical representation.
     *
     * @param physAddress Input MAC address
     * @return A PhysAddress containing the canonical representation.
     * @throws NullPointerException if physAddress is null
     */
    public final @NonNull P canonizePhysAddress(final @NonNull P physAddress) {
        final char[] input = getPhysValue(physAddress).toCharArray();
        return ensureLowerCase(input) ? physFactory.newInstance(String.valueOf(input)) : physAddress;
    }

    /**
     * Create a PhysAddress object holding the canonical representation of the bytes passed in as argument.
     *
     * @param bytes input array
     * @return PhysAddress with canonical string derived from input bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if length of input is not at least 1 byte
     */
    public final @NonNull P physAddressFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length > 0, "Physical address should have at least one byte");
        return physFactory.newInstance(bytesToString(bytes, (bytes.length + 1) / 3));
    }

    public final byte @NonNull[] bytesFor(final @NonNull M macAddress) {
        final String mac = getValue(macAddress);
        final byte[] ret = new byte[MAC_BYTE_LENGTH];

        for (int i = 0, base = 0; i < MAC_BYTE_LENGTH; ++i, base += 3) {
            ret[i] = (byte) (hexValue(mac.charAt(base)) << 4 | hexValue(mac.charAt(base + 1)));
        }

        return ret;
    }

    protected abstract String getValue(M macAddress);

    protected abstract String getPhysValue(P physAddress);

    static byte hexValue(final char ch) {
        byte value;
        try {
            // Performance optimization: access the array and rely on the VM for catching
            // illegal access (which boils down to illegal character, which should never happen.
            value = HEX_VALUES[ch];
        } catch (IndexOutOfBoundsException e) {
            value = -1;
        }

        if (value < 0) {
            throw new IllegalArgumentException("Invalid character '" + ch + "' encountered");
        }

        return value;
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
    private static boolean ensureLowerCase(final char @NonNull[] chars) {
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
     * @param charHint Hint at how many characters are needed
     * @return Canonical MAC address string
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if length of input is not 6 bytes
     */
    private static @NonNull String bytesToString(final byte @NonNull[] bytes, final int charHint) {
        final StringBuilder sb = new StringBuilder(charHint);
        appendHexByte(sb, bytes[0]);
        for (int i = 1; i < bytes.length; ++i) {
            appendHexByte(sb.append(':'), bytes[i]);
        }

        return sb.toString();
    }

    private static void appendHexByte(final StringBuilder sb, final byte byteVal) {
        final int intVal = Byte.toUnsignedInt(byteVal);
        sb.append(HEX_CHARS[intVal >>> 4]).append(HEX_CHARS[intVal & 15]);
    }
}
