/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import java.util.Arrays;
import java.util.HexFormat;
import org.eclipse.jdt.annotation.NonNull;

/**
 * IPv6 address parsing for {@code ietf-inet-types} ipv6-address and ipv6-prefix. This is an internal implementation
 * class, not meant to be used directly in any shape or form to the outside world, as the code relies on the fact that
 * the strings presented to it have been previously validated to conform to the regular expressions defined in the YANG
 *  model.
/*
 * v6 routines added by Anton Ivanov on 14.6.2015
 * revised by Robert Varga
 *
 * BIG FAT WARNING!!!
 * Read all of the following before you touch any v6 code or decide to
 * optimize it by invoking a "simple" Guava call
 *
 * Java IPv6 is fundamentally broken and Google libraries do not fix it.
 * 1. Java will allways implicitly rewrite v4 mapped into v6 as a v4 address
 *      and there is absolutely no way to override this behaviour
 * 2. Guava libraries cannot parse non-canonical IPv6. They will throw an
 *      exception. Even if they did, they re-use the same broken java code
 *      underneath.
 *
 * This is why we have to parse v6 by ourselves.
 *
 * The following conversion code is based on inet_cidr_pton_ipv6 in NetBSD
 *
 * The original BSD code is licensed under standard BSD license. While we
 * are not obliged to provide an attribution, credit where credit is due.
 * As far as why it is similar to Sun's sun.net.util please ask Sun why
 * their code has the same variable names, comments and code flow.
 */
public final class Ipv6Utils {
    public static final int INET6_LENGTH = 16;

    private Ipv6Utils() {
        // Hidden on purpose
    }

    /**
     * Convert Ipv6Address object to a valid Canonical v6 address in byte format.
     *
     * @param bytes Byte array for output
     * @param str String representation
     * @param strLimit String offset which should not be processed
     * @throws NullPointerException if ipv6address is null
     */
    @SuppressWarnings("checkstyle:localVariableName")
    public static void fillIpv6Bytes(final byte @NonNull[] bytes, final String str, final int strLimit) {
        // Leading :: requires some special handling.
        int i = 0;
        if (str.charAt(i) == ':') {
            // Note ++i side-effect in check
            checkArgument(str.charAt(++i) == ':', "Invalid v6 address '%s'", str);
        }

        boolean haveVal = false;
        int val = 0;
        int colonp = -1;
        int j = 0;
        int curtok = i;
        while (i < strLimit) {
            final char ch = str.charAt(i++);

            // v6 separator
            if (ch == ':') {
                curtok = i;
                if (haveVal) {
                    // removed overrun check - the regexp checks for valid data
                    bytes[j++] = (byte) (val >>> 8 & 0xff);
                    bytes[j++] = (byte) (val & 0xff);
                    haveVal = false;
                    val = 0;
                } else {
                    // no need to check separator position validity - regexp does that
                    colonp = j;
                }

                continue;
            }

            // frankenstein - v4 attached to v6, mixed notation
            if (ch == '.' && j + Ipv4Utils.INET4_LENGTH <= INET6_LENGTH) {
                /*
                 * This has passed the regexp so it is fairly safe to parse it
                 * straight away. Use the Ipv4Utils for that.
                 */
                Ipv4Utils.fillIpv4Bytes(bytes, j, str, curtok, strLimit);
                j += Ipv4Utils.INET4_LENGTH;
                haveVal = false;
                break;
            }

            /*
             * Business as usual - ipv6 address digit.
             * We can remove all checks from the original BSD code because
             * the regexp has already verified that we are not being fed
             * anything bigger than 0xffff between the separators.
             */
            val = (val << 4) + HexFormat.fromHexDigit(ch);
            haveVal = true;
        }

        if (haveVal) {
            verifySize(j + Short.BYTES <= INET6_LENGTH, str);
            bytes[j++] = (byte) (val >> 8 & 0xff);
            bytes[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            verifySize(j != INET6_LENGTH, str);
            expandZeros(bytes, colonp, j);
        } else {
            verifySize(j == INET6_LENGTH, str);
        }
    }

    private static void verifySize(final boolean expression, final String str) {
        verify(expression, "Overrun in parsing of '%s', should not occur", str);
    }

    private static void expandZeros(final byte[] bytes, final int where, final int filledBytes) {
        final int tailLength = filledBytes - where;
        final int tailOffset = INET6_LENGTH - tailLength;
        System.arraycopy(bytes, where, bytes, tailOffset, tailLength);
        Arrays.fill(bytes, where, tailOffset, (byte)0);
    }
}
