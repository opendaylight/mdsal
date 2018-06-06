/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class StringTypeUtils {
    private static final int[] MASKS = { 0,
        0x80000000, 0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000, 0xFE000000, 0xFF000000,
        0xFF800000, 0xFFC00000, 0xFFE00000, 0xFFF00000, 0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000,
        0xFFFF8000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFF800, 0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00,
        0xFFFFFF80, 0xFFFFFFC0, 0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC, 0xFFFFFFFE, 0xFFFFFFFF,
    };

    private StringTypeUtils() {

    }

    static int findSingleHash(final String str) {
        final int slash = str.indexOf('/');
        checkArgument(slash != -1, "Value \"%s\" does not contain a slash", str);
        checkArgument(str.indexOf('/', slash + 1) == -1, "Value \"%s\" contains multiple slashes", str);
        return slash;
    }

    static int maskBits(final int intBits, final int length) {
        return intBits & MASKS[length];
    }

    static byte first(final short shortBits) {
        return (byte)(shortBits >>> 8);
    }

    static byte first(final int intBits) {
        return (byte) (intBits >>> 24);
    }

    static byte first(final long longBits) {
        return (byte) (longBits >>> 56);
    }

    static byte second(final int intBits) {
        return (byte) (intBits >>> 16);
    }

    static byte second(final short shortBits) {
        return (byte) shortBits;
    }

    static byte second(final long longBits) {
        return (byte) (longBits >>> 48);
    }

    static byte third(final int intBits) {
        return (byte) (intBits >>> 8);
    }

    static byte third(final long longBits) {
        return (byte) (longBits >>> 40);
    }

    static byte fourth(final int intBits) {
        return (byte) intBits;
    }

    static byte fourth(final long longBits) {
        return (byte) (longBits >>> 32);
    }

    static byte fifth(final long longBits) {
        return (byte) (longBits >>> 24);
    }

    static byte sixth(final long longBits) {
        return (byte) (longBits >>> 16);
    }

    static byte seventh(final long longBits) {
        return (byte) (longBits >>> 8);
    }

    static byte eighth(final long longBits) {
        return (byte) longBits;
    }
}
