/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.first;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.fourth;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.second;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * Abstract base class for {@link Ipv6AddressString} and {@link Ipv6PrefixString}
 *
 * @param <T> derived string representation
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
@NonNullByDefault
public abstract class AbstractIpv6Address<T extends AbstractIpv6Address<T>> extends DerivedString<T> {
    @FunctionalInterface
    private interface HextetFunction {
        int extractHextet(int intBits);
    }

    private static final long serialVersionUID = 1L;

    /**
     * MethodHandle to invoke the equivalent of com.google.common.net.InetAddresses.compressLongestRunOfZeroes(), which
     * is not part of a part of the public API, but does exactly what we need.
     */
    private static final MethodHandle COMPRESS_LONGEST_RUN_OF_ZEROES = lookupGuava("compressLongestRunOfZeroes");

    /**
     * MethodHandle to invoke the equivalent of com.google.common.net.InetAddresses.hextetsToIPv6String(), which is not
     *  part of a part of the public API, but does <i>almost</i> exactly what we need.
     */
    private static final MethodHandle HEXTETS_TO_IPV6_STRING = lookupGuava("hextetsToIPv6String");

    @SuppressWarnings("null")
    private static MethodHandle lookupGuava(final String methodName) {
        return AccessController.doPrivileged((PrivilegedAction<MethodHandle>) () -> {
            final Method m;
            try {
                m = InetAddresses.class.getDeclaredMethod(methodName, int[].class);
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }

            m.setAccessible(true);

            try {
                // We could use lookup(), but that does not buy us anything, as the method is in a different package
                return MethodHandles.publicLookup().unreflect(m);
            } catch (IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        });
    }

    // Due to Java object alignment we store the 128 bits as 4 integers rather than 8 longs, hence not wasting
    // the 4-byte shadow present after object header in most cases.
    private final int intBits0;
    private final int intBits1;
    private final int intBits2;
    private final int intBits3;

    AbstractIpv6Address(final int intBits0, final int intBits1, final int intBits2, final int intBits3) {
        this.intBits0 = intBits0;
        this.intBits1 = intBits1;
        this.intBits2 = intBits2;
        this.intBits3 = intBits3;
    }

    AbstractIpv6Address(final AbstractIpv6Address<?> other) {
        this.intBits0 = other.intBits0;
        this.intBits1 = other.intBits1;
        this.intBits2 = other.intBits2;
        this.intBits3 = other.intBits3;
    }

    public final int getIntBits0() {
        return intBits0;
    }

    public final int getIntBits1() {
        return intBits1;
    }

    public final int getIntBits2() {
        return intBits2;
    }

    public final int getIntBits3() {
        return intBits3;
    }

    final int hashBits() {
        return intBits0 ^ intBits1 ^ intBits2 ^ intBits3;
    }

    final byte[] bitsAsArray() {
        return new byte[] { first(intBits0), second(intBits0), third(intBits0), fourth(intBits0),
                first(intBits1), second(intBits1), third(intBits1), fourth(intBits1), first(intBits2),
                second(intBits2), third(intBits2), fourth(intBits2), first(intBits3), second(intBits3),
                third(intBits3), fourth(intBits3)
        };
    }

    final int compareBits(final AbstractIpv6Address<?> other) {
        int cmp = Integer.compareUnsigned(intBits0, other.intBits0);
        if (cmp == 0) {
            cmp = Integer.compareUnsigned(intBits1, other.intBits1);
            if (cmp == 0) {
                cmp = Integer.compareUnsigned(intBits2, other.intBits2);
                if (cmp == 0) {
                    cmp = Integer.compareUnsigned(intBits3, other.intBits3);
                }
            }
        }
        return cmp;
    }

    final boolean equalsBits(final AbstractIpv6Address<?> other) {
        return intBits0 == other.intBits0 && intBits1 == other.intBits1 && intBits2 == other.intBits2
                && intBits3 == other.intBits3;
    }

    StringBuilder appendIpv6Address(final StringBuilder sb, final short mask) {
        boolean wasPresent = appendHextet(sb, false, mask & 0x01, intBits0, AbstractIpv6Address::firstHextet);
        wasPresent = appendHextet(sb, wasPresent, mask & 0x02, intBits0, AbstractIpv6Address::secondHextet);
        wasPresent = appendHextet(sb, wasPresent, mask & 0x04, intBits1, AbstractIpv6Address::firstHextet);
        wasPresent = appendHextet(sb, wasPresent, mask & 0x08, intBits1, AbstractIpv6Address::secondHextet);
        wasPresent = appendHextet(sb, wasPresent, mask & 0x10, intBits2, AbstractIpv6Address::firstHextet);
        wasPresent = appendHextet(sb, wasPresent, mask & 0x20, intBits2, AbstractIpv6Address::secondHextet);
        wasPresent = appendHextet(sb, wasPresent, mask & 0x40, intBits3, AbstractIpv6Address::firstHextet);
        appendHextet(sb, wasPresent, mask & 0x80, intBits3, AbstractIpv6Address::secondHextet);
        return sb;
    }

    int[] createHextets() {
        final int[] hextets = new int[] {
                firstHextet(intBits0), secondHextet(intBits0), firstHextet(intBits1), secondHextet(intBits1),
                firstHextet(intBits2), secondHextet(intBits2), firstHextet(intBits3), secondHextet(intBits3)
        };

        compressLongestRunOfZeroes(hextets);
        return hextets;
    }

    static String hextetsToIPv6String(final int[] hextets) {
        try {
            return (@NonNull String) HEXTETS_TO_IPV6_STRING.invokeExact(hextets);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new LinkageError("Failed to invoke InetAddresses.hextetsToIPv6String()", e);
        }
    }

    private static boolean appendHextet(final StringBuilder sb, final boolean wasPresent, final int bit,
            final int intBits, final HextetFunction decode) {
        if (bit == 0) {
            if (wasPresent) {
                sb.append("::");
            }
            return false;
        }

        if (wasPresent) {
            sb.append(':');
        }
        sb.append(Integer.toHexString(decode.extractHextet(intBits)));
        return true;
    }

    private static int firstHextet(final int intBits) {
        return intBits >>> 16;
    }

    private static int secondHextet(final int intBits) {
        return intBits & 0xFFFF;
    }

    private static void compressLongestRunOfZeroes(final int[] hextets) {
        try {
            COMPRESS_LONGEST_RUN_OF_ZEROES.invokeExact(hextets);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new LinkageError("Failed to invoke InetAddresses.compressLongestRunOfZeroes()", e);
        }
    }
}
