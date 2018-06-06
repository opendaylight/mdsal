/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.type.util;

import static java.lang.Byte.toUnsignedInt;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.first;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.fourth;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.second;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * Abstract base class for {@link DerivedString}s, whose internal representation is a 32bit unsigned integer and their
 * canonical string is a dotted quad -- like {@code ietf-yang-types.yang}'s {@code dotted-quad} and
 * {@code ietf-inet-types.yang}'s {@code ipv4-adress-no-zone}.
 *
 * @param <T> derived string representation
 */
@Beta
@NonNullByDefault
public abstract class AbstractDottedQuad<T extends AbstractDottedQuad<T>> extends DerivedString<T>
        implements ByteArrayLike, InetAddressLike {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final int intBits;

    protected AbstractDottedQuad(final int intBits) {
        this.intBits = intBits;
    }

    protected AbstractDottedQuad(final AbstractDottedQuad<?> other) {
        this(other.intBits);
    }

    @Override
    public final byte getByteAt(final int offset) {
        return switch (offset) {
            case 0 -> first(intBits);
            case 1 -> second(intBits);
            case 2 -> third(intBits);
            case 3 -> fourth(intBits);
            default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
        };
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

    protected static final StringBuilder toStringBuilder(final int allocSize, final int intBits) {
        return new StringBuilder(allocSize)
            .append(toUnsignedInt(first(intBits))).append('.')
            .append(toUnsignedInt(second(intBits))).append('.')
            .append(toUnsignedInt(third(intBits))).append('.')
            .append(toUnsignedInt(fourth(intBits)));
    }

    protected final int getIntBits() {
        return intBits;
    }

    protected final int compareBits(final AbstractDottedQuad<?> other) {
        return Integer.compareUnsigned(intBits, other.intBits);
    }

    protected final boolean equalsBits(final AbstractDottedQuad<?> other) {
        return intBits == other.intBits;
    }
}
