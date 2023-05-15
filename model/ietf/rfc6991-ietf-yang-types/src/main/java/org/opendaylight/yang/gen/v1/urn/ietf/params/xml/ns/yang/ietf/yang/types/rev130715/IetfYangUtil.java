/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.util.HexFormat;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.reflect.StringValueObjectFactory;
import org.opendaylight.mdsal.model.ietf.util.Ipv4Utils;

/**
 * Utility methods for working with types defined in {@code ietf-yang-types}.
 */
@Beta
public final class IetfYangUtil {
    public static final IetfYangUtil INSTANCE = new IetfYangUtil();

    private static final int MAC_BYTE_LENGTH = 6;
    private static final HexFormat COLON_HEXFORMAT = HexFormat.ofDelimiter(":");
    private static final byte @NonNull[] EMPTY_BYTES = new byte[0];

    private final StringValueObjectFactory<MacAddress> macFactory;
    private final StringValueObjectFactory<PhysAddress> physFactory;
    private final StringValueObjectFactory<HexString> hexFactory;
    private final StringValueObjectFactory<DottedQuad> quadFactory;
    private final StringValueObjectFactory<Uuid> uuidFactory;

    private IetfYangUtil() {
        macFactory = StringValueObjectFactory.create(MacAddress.class, "00:00:00:00:00:00");
        physFactory = StringValueObjectFactory.create(PhysAddress.class, "00:00");
        hexFactory = StringValueObjectFactory.create(HexString.class, "00");
        quadFactory = StringValueObjectFactory.create(DottedQuad.class, "0.0.0.0");
        uuidFactory = StringValueObjectFactory.create(Uuid.class, "f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
    }

    /**
     * Convert the value of a MacAddress into the canonical representation.
     *
     * @param macAddress Input MAC address
     * @return A MacAddress containing the canonical representation.
     * @throws NullPointerException if macAddress is null
     */
    public @NonNull MacAddress canonizeMacAddress(final @NonNull MacAddress macAddress) {
        final char[] input = macAddress.getValue().toCharArray();
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
    public @NonNull MacAddress macAddressFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == MAC_BYTE_LENGTH, "MAC address should have 6 bytes, not %s", bytes.length);
        return macFactory.newInstance(COLON_HEXFORMAT.formatHex(bytes));
    }

    public byte @NonNull[] macAddressBytes(final @NonNull MacAddress macAddress) {
        return stringToBytes(macAddress.getValue(), MAC_BYTE_LENGTH);
    }

    /**
     * Convert the value of a PhysAddress into the canonical representation.
     *
     * @param physAddress Input MAC address
     * @return A PhysAddress containing the canonical representation.
     * @throws NullPointerException if physAddress is null
     */
    public @NonNull PhysAddress canonizePhysAddress(final @NonNull PhysAddress physAddress) {
        final char[] input = physAddress.getValue().toCharArray();
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
    public @NonNull PhysAddress physAddressFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length > 0, "Physical address should have at least one byte");
        return physFactory.newInstance(COLON_HEXFORMAT.formatHex(bytes));
    }

    public byte @NonNull[] physAddressBytes(final @NonNull PhysAddress physAddress) {
        final String str = physAddress.getValue();
        return str.isEmpty() ? EMPTY_BYTES : stringToBytes(str, str.length() / 3 + 1);
    }

    public @NonNull HexString hexStringFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length > 0, "Hex string should have at least one byte");
        return hexFactory.newInstance(COLON_HEXFORMAT.formatHex(bytes));
    }

    public byte @NonNull[] hexStringBytes(final @NonNull HexString hexString) {
        final String str = hexString.getValue();
        return stringToBytes(str, str.length() / 3 + 1);
    }

    public @NonNull DottedQuad dottedQuadFor(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == 4, "Dotted-quad should have 4 bytes");
        return quadFactory.newInstance(Ipv4Utils.addressString(bytes));
    }

    public @NonNull DottedQuad dottedQuadFor(final int bits) {
        return quadFactory.newInstance(Ipv4Utils.addressString(bits));
    }

    public int dottedQuadBits(final @NonNull DottedQuad dottedQuad) {
        final String str = dottedQuad.getValue();
        return Ipv4Utils.addressBits(str, str.length());
    }

    public byte @NonNull[] dottedQuadBytes(final @NonNull DottedQuad dottedQuad) {
        final String str = dottedQuad.getValue();
        return Ipv4Utils.addressBytes(str, str.length());
    }

    public @NonNull Uuid uuidFor(final @NonNull UUID uuid) {
        return uuidFactory.newInstance(uuid.toString());
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
