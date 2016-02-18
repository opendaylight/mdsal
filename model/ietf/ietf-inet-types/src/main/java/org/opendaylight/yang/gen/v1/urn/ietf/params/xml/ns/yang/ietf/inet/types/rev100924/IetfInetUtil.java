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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.util.StringValueObjectFactory;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 *
 * FIXME: IPv6 addresses are not emitted in canonical format as specified by the model.
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

    @Nonnull public static IpAddress ipAddressFor(@Nonnull final byte[] bytes) {
        switch (bytes.length) {
            case 4:
                return new IpAddress(ipv4AddressFor(bytes));
            case 16:
                return new IpAddress(ipv6AddressFor(bytes));
            default:
                throw new IllegalArgumentException("Invalid array length " + bytes.length);
        }
    }

    @Nonnull public static IpAddress ipAddressFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        if (addr instanceof Inet4Address) {
            return new IpAddress(ipv4AddressFor(addr));
        } else if (addr instanceof Inet6Address) {
            return new IpAddress(ipv6AddressFor(addr));
        } else {
            throw new IllegalArgumentException("Unhandled address " + addr);
        }
    }

    /**
     * Create an {@link Ipv4Address} by interpreting input bytes as an IPv4 address.
     *
     * @param bytes 4-byte array
     * @return An {@link Ipv4Address} object
     * @throws IllegalArgumentException if bytes has length different from 4
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public static Ipv4Address ipv4AddressFor(@Nonnull final byte[] bytes) {
        return IPV4_ADDRESS_FACTORY.newInstance(addressStringV4(bytes));
    }

    /**
     * Create an {@link Ipv4Address} by interpreting an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An {@link Ipv4Address} object
     * @throws IllegalArgumentException if addr is not an {@link Inet4Address}
     * @throws NullPointerException if addr is null
     */
    @Nonnull public static Ipv4Address ipv4AddressFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        return IPV4_ADDRESS_FACTORY.newInstance(addr.getHostAddress());
    }

    /**
     * Create a /32 {@link Ipv4Prefix} by interpreting input bytes as an IPv4 address.
     *
     * @param bytes four-byte array
     * @return An {@link Ipv4Prefix} object
     * @throws IllegalArgumentException if bytes has length different from 4
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public static Ipv4Prefix ipv4PrefixFor(@Nonnull final byte[] bytes) {
        return IPV4_PREFIX_FACTORY.newInstance(prefixStringV4(bytes));
    }

    /**
     * Create a {@link Ipv4Prefix} by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked, hence input <code>address = { 1, 2, 3, 4 }</code>
     * and <code>mask=24</code> will result in <code>1.2.3.4/24</code>.
     *
     * @param address Input address as a 4-byte array
     * @param mask Prefix mask
     * @return An {@link Ipv4Prefix} object
     * @throws IllegalArgumentException if bytes has length different from 4 or if mask is not in range 0-32
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public static Ipv4Prefix ipv4PrefixFor(@Nonnull final byte[] address, final int mask) {
        return IPV4_PREFIX_FACTORY.newInstance(prefixStringV4(address, mask));
    }

    /**
     * Create a /32 {@link Ipv4Prefix} for an {@link Inet4Address}
     *
     * @param addr An {@link Inet4Address}
     * @return An {@link Ipv4Prefix} object
     * @throws IllegalArgumentException if addr is not an Inet4Address
     * @throws NullPointerException if adds is null
     */
    @Nonnull public static Ipv4Prefix ipv4PrefixFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        return IPV4_PREFIX_FACTORY.newInstance(addr.getHostAddress() + "/32");
    }

    /**
     * Create a {@link Ipv4Prefix} by combining the address with a mask. The address bytes are not masked.
     *
     * @param addr An {@link Inet4Address}
     * @param mask Prefix mask
     * @return An {@link Ipv4Prefix} object
     * @throws IllegalArgumentException if addr is not an Inet4Address or if mask is not in range 0-32
     * @throws NullPointerException if addr is null
     */
    @Nonnull public static Ipv4Prefix ipv4PrefixFor(@Nonnull final InetAddress addr, final int mask) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        Preconditions.checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);
        return IPV4_PREFIX_FACTORY.newInstance(addr.getHostAddress() + '/' + mask);
    }

    /**
     * Create an {@link Ipv6Address} by interpreting input bytes as an IPv6 address.
     *
     * @param bytes 16-byte array
     * @return An {@link Ipv6Address} object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public static Ipv6Address ipv6AddressFor(@Nonnull final byte[] bytes) {
        return IPV6_ADDRESS_FACTORY.newInstance(addressStringV6(bytes));
    }

    /**
     * Create an {@link Ipv6Address} by interpreting an {@link Inet6Address}.
     *
     * @param addr An {@link Inet6Address}
     * @return An {@link Ipv6Address} object
     * @throws IllegalArgumentException if addr is not an {@link Inet6Address}
     * @throws NullPointerException if addr is null
     */
    @Nonnull public static Ipv6Address ipv6AddressFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet6Address, "Address has to be an Inet6Address");
        return IPV6_ADDRESS_FACTORY.newInstance(addr.getHostAddress());
    }

    /**
     * Create a /128 {@link Ipv6Prefix} by interpreting input bytes as an IPv6 address.
     *
     * @param bytes four-byte array
     * @return An {@link Ipv6Prefix} object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public static Ipv6Prefix ipv6PrefixFor(@Nonnull final byte[] bytes) {
        return IPV6_PREFIX_FACTORY.newInstance(addressStringV6(bytes) + "/128");
    }

    /**
     * Create a {@link Ipv6Prefix} by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked.
     *
     * @param address Input address as a 4-byte array
     * @param mask Prefix mask
     * @return An {@link Ipv6Prefix} object
     * @throws IllegalArgumentException if bytes has length different from 16 or if mask is not in range 0-128
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public static Ipv6Prefix ipv6PrefixFor(@Nonnull final byte[] address, final int mask) {
        Preconditions.checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return IPV6_PREFIX_FACTORY.newInstance(addressStringV6(address) + '/' + mask);
    }

    /**
     * Create a /128 {@link Ipv6Prefix} by interpreting input bytes as an IPv4 address.
     *
     * @param bytes four-byte array
     * @return An {@link Ipv6Prefix} object
     * @throws IllegalArgumentException if addr is not an Inet6Address or if mask is not in range 0-128
     * @throws NullPointerException if addr is null
     */
    @Nonnull public static Ipv6Prefix ipv6PrefixFor(@Nonnull final InetAddress addr) {
        return IPV6_PREFIX_FACTORY.newInstance(addr.getHostAddress() + "/128");
    }

    /**
     * Create a {@link Ipv6Prefix} by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked.
     *
     * @param addr Input address
     * @param mask Prefix mask
     * @return An {@link Ipv6Prefix} object
     * @throws IllegalArgumentException if addr is not an Inet6Address or if mask is not in range 0-128
     * @throws NullPointerException if addr is null
     */
    @Nonnull public static Ipv6Prefix ipv6PrefixFor(@Nonnull final InetAddress addr, final int mask) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet6Address, "Address has to be an Inet6Address");
        Preconditions.checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return IPV6_PREFIX_FACTORY.newInstance(addr.getHostAddress() + '/' + mask);
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

    private static String addressStringV6(final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 16, "IPv6 address length is 16 bytes");

        try {
            return Inet6Address.getByAddress(bytes).getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(String.format("Invalid input %s", bytes), e);
        }
    }

    private static String prefixStringV4(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        sb.append("/32");
        return sb.toString();
    }

    private static String prefixStringV4(final byte[] bytes, final int mask) {
        Preconditions.checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);

        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        sb.append('/');
        sb.append(mask);
        return sb.toString();
    }
}
