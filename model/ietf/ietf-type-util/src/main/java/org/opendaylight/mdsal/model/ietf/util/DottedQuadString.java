/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * {@link DerivedString} representation of the {@code dotted-quad} type as defined by ietf-yang-types.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
@NonNullByDefault
public class DottedQuadString extends AbstractDottedQuad<DottedQuadString> {
    private static final long serialVersionUID = 1L;

    protected DottedQuadString(final int intBits) {
        super(intBits);
    }

    protected DottedQuadString(final DottedQuadString other) {
        super(other);
    }

    /**
     * Return a {@link DottedQuadString} corresponding to specified 32 bits.
     *
     * @param intBits 32bit representation
     * @return A {@link DottedQuadString}
     */
    public static DottedQuadString valueOf(final int intBits) {
        return new DottedQuadString(intBits);
    }

    /**
     * Return a {@link DottedQuadString} corresponding to big-endian interpretation of the first four bytes
     * in the supplied byte array.
     *
     * @param bytes byte array
     * @return A {@link DottedQuadString}
     * @throws NullPointerException if {@code bytes} is null
     * @throws IllegalArgumentException if {@code bytes.length} is less than 4
     */
    public static DottedQuadString valueOf(final byte[] bytes) {
        return valueOf(Ints.fromByteArray(bytes));
    }

    /**
     * Return a {@link DottedQuadString} corresponding to the specified {@link Inet4Address}.
     *
     * @param address Inet4Address
     * @return A {@link DottedQuadString}
     * @throws NullPointerException if {@code address} is null
     */
    public static DottedQuadString valueOf(final Inet4Address address) {
        return valueOf(address.getAddress());
    }

    /**
     * Return the internal representation bits, suitable as an argument to {@link #valueOf(int)}.
     *
     * @return Internal representation bits.
     */
    public final int toIntBits() {
        return getIntBits();
    }

    @Override
    public final String toCanonicalString() {
        return toStringBuilder(15, getIntBits()).toString();
    }

    @Override
    public final DottedQuadStringSupport support() {
        return DottedQuadStringSupport.getInstance();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final DottedQuadString o) {
        return compareBits(o);
    }

    @Override
    public final int hashCode() {
        return getIntBits();
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof DottedQuadString && equalsBits((DottedQuadString) obj);
    }
}
