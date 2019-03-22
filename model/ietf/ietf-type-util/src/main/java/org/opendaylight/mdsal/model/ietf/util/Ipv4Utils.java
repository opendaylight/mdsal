/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import org.eclipse.jdt.annotation.NonNull;

/**
 * IPv4 address parsing for ietf-inet-types ipv4-address. This is an internal implementation class, not meant to be
 * exposed in any shape or form to the outside world, as the code relies on the fact that the strings presented to it
 * have been previously validated to conform to the regular expressions defined in the YANG model.
 */
final class Ipv4Utils {
    private Ipv4Utils() {

    }

    static void fillIpv4Bytes(final byte @NonNull[] bytes, final int byteStart, final String str, final int strStart,
            final int strLimit) {
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
}
