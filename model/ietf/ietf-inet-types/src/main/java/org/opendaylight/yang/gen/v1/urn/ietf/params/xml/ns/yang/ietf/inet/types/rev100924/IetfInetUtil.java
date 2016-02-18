/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.binding.util.StringValueObjectFactory;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 */
@Beta
public final class IetfInetUtil {
    private static final StringValueObjectFactory<Ipv4Address> IPV4_ADDRESS_FACTORY =
            StringValueObjectFactory.create(Ipv4Address.class, "0.0.0.0");
    private static final StringValueObjectFactory<Ipv4Prefix> IPV4_PREFIX_FACTORY =
            StringValueObjectFactory.create(Ipv4Prefix.class, "0.0.0.0/0");
    private static final StringValueObjectFactory<Ipv6Address> IPV6_ADDRESS_FACTORY =
            StringValueObjectFactory.create(Ipv6Address.class, "::0");
    private static final StringValueObjectFactory<Ipv6Prefix> IPV6_PREFIX_FACTORY =
            StringValueObjectFactory.create(Ipv6Prefix.class, "::0/0");

    private IetfInetUtil() {
        throw new UnsupportedOperationException();
    }

    private static void appendIpv4String(final StringBuilder sb, final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 4, "IPv4 address length is 4 bytes");

        sb.append(Byte.toUnsignedInt(bytes[0]));
        for (int i = 1; i < 4; ++i) {
            sb.append('.');
            sb.append(Byte.toUnsignedInt(bytes[i]));
        }
    }

    private static String addressStringV4(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(15);
        appendIpv4String(sb, bytes);
        return sb.toString();
    }

    public static Ipv4Address ipv4AddressFor(final byte[] bytes) {
        return IPV4_ADDRESS_FACTORY.newInstance(addressStringV4(bytes));
    }

    private static String prefixStringV4(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        sb.append("/32");
        return sb.toString();
    }

    public static Ipv4Prefix ipv4PrefixFor(final byte[] bytes) {
        return IPV4_PREFIX_FACTORY.newInstance(prefixStringV4(bytes));
    }

    private static String prefixStringV4(final byte[] bytes, final int mask) {
        Preconditions.checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);

        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        sb.append('/');
        sb.append(mask);
        return sb.toString();
    }

    public static Ipv4Prefix ipv4PrefixFor(final byte[] bytes, final int mask) {
        return IPV4_PREFIX_FACTORY.newInstance(prefixStringV4(bytes, mask));
    }
}
