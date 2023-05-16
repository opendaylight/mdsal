/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.invoke.MethodHandles;
import java.util.HexFormat;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.reflect.StringValueObjectFactory;
import org.opendaylight.mdsal.model.ietf.type.util.Ipv4Utils;

/**
 * Utility methods for working with types defined in {@code ietf-yang-types}.
 */
public final class IetfYangUtil {
    private static final int MAC_BYTE_LENGTH = 6;
    private static final HexFormat COLON_HEXFORMAT = HexFormat.ofDelimiter(":");
    private static final byte @NonNull[] EMPTY_BYTES = new byte[0];

    private static final StringValueObjectFactory<MacAddress> MAC_FACTORY;
    private static final StringValueObjectFactory<PhysAddress> PHYS_FACTORY;
    private static final StringValueObjectFactory<HexString> HEX_FACTORY;
    private static final StringValueObjectFactory<DottedQuad> QUAD_FACTORY;
    private static final StringValueObjectFactory<Uuid> UUID_FACTORY;

    static {
        final var lookup = MethodHandles.lookup();
        MAC_FACTORY = StringValueObjectFactory.create(lookup, MacAddress.class, "00:00:00:00:00:00");
        PHYS_FACTORY = StringValueObjectFactory.create(lookup, PhysAddress.class, "00:00");
        HEX_FACTORY = StringValueObjectFactory.create(lookup, HexString.class, "00");
        QUAD_FACTORY = StringValueObjectFactory.create(lookup, DottedQuad.class, "0.0.0.0");
        UUID_FACTORY = StringValueObjectFactory.create(lookup, Uuid.class, "f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
    }

    private IetfYangUtil() {
        // Hidden on purpose
    }

    /**
     * Convert the value of a MacAddress into the canonical representation.
     *
     * @param macAddress Input MAC address
     * @return A MacAddress containing the canonical representation.
     * @throws NullPointerException if macAddress is null
     */
    public static @NonNull MacAddress canonizeMacAddress(final @NonNull MacAddress macAddress) {
        final char[] input = macAddress.getValue().toCharArray();
        return ensureLowerCase(input) ? MAC_FACTORY.newInstance(String.valueOf(input)) : macAddress;
    }

    /**
     * Create a MacAddress object holding the canonical representation of the 6 bytes
     * passed in as argument.
     * @param bytes 6-byte input array
     * @return MacAddress with canonical string derived from input bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if length of input is not 6 bytes
     */
    public static @NonNull MacAddress macAddressFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == MAC_BYTE_LENGTH, "MAC address should have 6 bytes, not %s", bytes.length);
        return MAC_FACTORY.newInstance(COLON_HEXFORMAT.formatHex(bytes));
    }

    public static byte @NonNull[] macAddressBytes(final @NonNull MacAddress macAddress) {
        return stringToBytes(macAddress.getValue(), MAC_BYTE_LENGTH);
    }

    /**
     * Convert the value of a PhysAddress into the canonical representation.
     *
     * @param physAddress Input MAC address
     * @return A PhysAddress containing the canonical representation.
     * @throws NullPointerException if physAddress is null
     */
    public static @NonNull PhysAddress canonizePhysAddress(final @NonNull PhysAddress physAddress) {
        final char[] input = physAddress.getValue().toCharArray();
        return ensureLowerCase(input) ? PHYS_FACTORY.newInstance(String.valueOf(input)) : physAddress;
    }

    /**
     * Create a PhysAddress object holding the canonical representation of the bytes passed in as argument.
     *
     * @param bytes input array
     * @return PhysAddress with canonical string derived from input bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if length of input is not at least 1 byte
     */
    public static @NonNull PhysAddress physAddressFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length > 0, "Physical address should have at least one byte");
        return PHYS_FACTORY.newInstance(COLON_HEXFORMAT.formatHex(bytes));
    }

    public static byte @NonNull[] physAddressBytes(final @NonNull PhysAddress physAddress) {
        final String str = physAddress.getValue();
        return str.isEmpty() ? EMPTY_BYTES : stringToBytes(str, str.length() / 3 + 1);
    }

    public static @NonNull HexString hexStringFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length > 0, "Hex string should have at least one byte");
        return HEX_FACTORY.newInstance(COLON_HEXFORMAT.formatHex(bytes));
    }

    public static byte @NonNull[] hexStringBytes(final @NonNull HexString hexString) {
        final String str = hexString.getValue();
        return stringToBytes(str, str.length() / 3 + 1);
    }

    public static @NonNull DottedQuad dottedQuadFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == 4, "Dotted-quad should have 4 bytes");
        return QUAD_FACTORY.newInstance(Ipv4Utils.addressString(bytes));
    }

    public static @NonNull DottedQuad dottedQuadFor(final int bits) {
        return QUAD_FACTORY.newInstance(Ipv4Utils.addressString(bits));
    }

    public static int dottedQuadBits(final @NonNull DottedQuad dottedQuad) {
        final String str = dottedQuad.getValue();
        return Ipv4Utils.addressBits(str, str.length());
    }

    public static byte @NonNull[] dottedQuadBytes(final @NonNull DottedQuad dottedQuad) {
        final String str = dottedQuad.getValue();
        return Ipv4Utils.addressBytes(str, str.length());
    }

    public static @NonNull Uuid uuidFor(final @NonNull UUID uuid) {
        return UUID_FACTORY.newInstance(uuid.toString());
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
                // Weird notation to ensure constant folding to '(char) (c + 32)', a well-known property of ASCII
                chars[i] = (char) (c + ('a' - 'A'));
                ret = true;
            }
        }

        return ret;
    }

    private static byte @NonNull[] stringToBytes(final String str, final int length) {
        final byte[] ret = new byte[length];
        for (int i = 0, base = 0; i < length; ++i, base += 3) {
            ret[i] = (byte) ((HexFormat.fromHexDigit(str.charAt(base)) << 4)
                + HexFormat.fromHexDigit(str.charAt(base + 1)));
        }
        return ret;
    }
}
