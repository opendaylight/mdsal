/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.util.StringValueObjectFactory;

/**
 * Utility methods for working with types defined in ietf-yang-types.
 */
@Beta
public final class IetfYangUtil {
    private static final StringValueObjectFactory<MacAddress> MAC_ADDRESS_FACTORY =
            StringValueObjectFactory.create(MacAddress.class, "00:00:00:00:00:00");
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private IetfYangUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert the value of a {@link MacAddress} into the canonical representation.
     *
     * @param macAddress Input MAC address
     * @return {@link MacAddress} containing the canonical representation.
     * @throws NullPointerException if macAddress is null
     */
    @Nonnull public static MacAddress canonizeMacAddress(@Nonnull final MacAddress macAddress) {
        final char[] input = macAddress.getValue().toCharArray();
        if (toLowerCase(input)) {
            return MAC_ADDRESS_FACTORY.newInstance(input.toString());
        } else {
            return macAddress;
        }
    }

    /**
     * Create a {@link MacAddress} object holding the canonical representation of the 6 bytes
     * passed in as argument.
     * @param bytes 6-byte input array
     * @return {@link MacAddress} with canonical string derived from input bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if length of input is not 6 bytes
     */
    @Nonnull public static MacAddress macAddressFor(@Nonnull final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 6, "MAC address should have 6 bytes, not %s", bytes.length);
        return MAC_ADDRESS_FACTORY.newInstance(macString(bytes));
    }

    private static final boolean toLowerCase(final char[] chars) {
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

    private static final void appendHexByte(final StringBuilder sb, final byte b) {
        final int v = Byte.toUnsignedInt(b);
        sb.append(HEX_CHARS[v >>> 4]);
        sb.append(HEX_CHARS[v &  15]);
    }

    private static String macString(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(17);

        appendHexByte(sb, bytes[0]);
        for (int i = 1; i < bytes.length; i++) {
            sb.append(':');
            appendHexByte(sb, bytes[i]);
        }

        return sb.toString();
    }
}
