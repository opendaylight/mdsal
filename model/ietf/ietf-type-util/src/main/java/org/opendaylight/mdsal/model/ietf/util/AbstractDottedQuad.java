/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static java.lang.Byte.toUnsignedInt;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.first;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.fourth;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.second;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.third;

import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * Abstract base class for {@link DerivedString}s, whose internal representation is a 32bit unsigned integer and their
 * canonical string is a dotted quad -- like {@link DottedQuadString} and {@link Ipv4AddressNoZoneString}.
 *
 * @param <T> derived string representation
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Deprecated
@NonNullByDefault
abstract class AbstractDottedQuad<T extends AbstractDottedQuad<T>> extends DerivedString<T>
        implements ByteArrayLike, InetAddressLike {
    private static final long serialVersionUID = 1L;

    private final int intBits;

    AbstractDottedQuad(final int intBits) {
        this.intBits = intBits;
    }

    AbstractDottedQuad(final AbstractDottedQuad<?> other) {
        this(other.intBits);
    }

    @Override
    public final byte getByteAt(final int offset) {
        switch (offset) {
            case 0:
                return first(intBits);
            case 1:
                return second(intBits);
            case 2:
                return third(intBits);
            case 3:
                return fourth(intBits);
            default:
                throw new IndexOutOfBoundsException("Invalid offset " + offset);
        }
    }

    @Override
    public final int getLength() {
        return 4;
    }

    @Override
    public final byte[] toByteArray() {
        return Ints.toByteArray(getIntBits());
    }

    @Override
    public final Inet4Address toInetAddress() {
        try {
            return (Inet4Address) Inet4Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    static StringBuilder toStringBuilder(final int allocSize, final int intBits) {
        return new StringBuilder(allocSize).append(toUnsignedInt(first(intBits))).append('.')
                .append(toUnsignedInt(second(intBits))).append('.').append(toUnsignedInt(third(intBits))).append('.')
                .append(toUnsignedInt(fourth(intBits)));
    }

    final int getIntBits() {
        return intBits;
    }

    final int compareBits(final AbstractDottedQuad<?> other) {
        return Integer.compareUnsigned(intBits, other.intBits);
    }

    final boolean equalsBits(final AbstractDottedQuad<?> other) {
        return intBits == other.intBits;
    }
}
