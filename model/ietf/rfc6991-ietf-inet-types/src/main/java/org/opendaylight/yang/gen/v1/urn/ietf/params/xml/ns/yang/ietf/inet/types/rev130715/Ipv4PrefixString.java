/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.model.ietf.type.util.AbstractDottedQuad;
import org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * {@link DerivedString} representation of the {@code ipv4-prefix} type as defined by ietf-inet-types.
 */
@Beta
@NonNullByDefault
public class Ipv4PrefixString extends DerivedString<Ipv4PrefixString> {
    private static final long serialVersionUID = 1L;

    private final int address;
    private final byte length;

    // Required string length, 0 indicates not computed. We use this to minimize StringBuilder allocation size.
    // This field comes for free due to JVM object alignment rules.
    private transient byte strlen;

    protected Ipv4PrefixString(final int address, final byte length) {
        try {
            // No explicit length check, we rely on the JVM to detect illegal access
            this.address = StringTypeUtils.maskBits(address, length);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid prefix length " + length, e);
        }
        this.length = length;
    }

    protected Ipv4PrefixString(final Ipv4PrefixString other) {
        address = other.address;
        length = other.length;
        strlen = other.strlen;
    }

    public static Ipv4PrefixString valueOf(final int address, final byte length) {
        return new Ipv4PrefixString(address, length);
    }

    public static Ipv4PrefixString valueOf(final byte[] address, final byte length) {
        return valueOf(Ints.fromByteArray(address), length);
    }

    public static Ipv4PrefixString valueOf(final Inet4Address address, final byte length) {
        return valueOf(address.getAddress(), length);
    }

    public static Ipv4PrefixString valueOf(final Ipv4AddressString address, final byte length) {
        return new Ipv4PrefixString(address.getIntBits(), length);
    }

    public final int getAddress() {
        return address;
    }

    public final Ipv4AddressString getAddressString() {
        return Ipv4AddressNoZoneString.valueOf(address);
    }

    public final byte getLength() {
        return length;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Ipv4PrefixString o) {
        final int cmp = Integer.compareUnsigned(address, address);
        return cmp != 0 ? cmp : Byte.compare(length, length);
    }

    @Override
    public final String toCanonicalString() {
        final int local = strlen;
        if (local != 0) {
            return AbstractDottedQuad.toStringBuilder(local, address).toString();
        }

        final String ret = AbstractDottedQuad.toStringBuilder(18, address).toString();
        strlen = (byte) ret.length();
        return ret;
    }

    @Override
    public final Ipv4PrefixStringSupport support() {
        return Ipv4PrefixStringSupport.getInstance();
    }

    @Override
    public final int hashCode() {
        return address + 31 * length;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final Ipv4PrefixString other)) {
            return false;
        }
        return address == other.address && length == other.length;
    }

    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        strlen = 0;
    }
}
