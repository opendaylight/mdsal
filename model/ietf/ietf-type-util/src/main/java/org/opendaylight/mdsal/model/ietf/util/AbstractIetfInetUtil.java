/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.util.StringValueObjectFactory;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 */
@Beta
public abstract class AbstractIetfInetUtil<A4, P4, A6, P6, A> {
    private final StringValueObjectFactory<A4> address4Factory;
    private final StringValueObjectFactory<P4> prefix4Factory;
    private final StringValueObjectFactory<A6> address6Factory;
    private final StringValueObjectFactory<P6> prefix6Factory;

    protected AbstractIetfInetUtil(final Class<A4> addr4Class, final Class<P4> prefix4Class,
            final Class<A6> addr6Class, final Class<P6> prefix6Class) {
        this.address4Factory = StringValueObjectFactory.create(addr4Class, "0.0.0.0");
        this.prefix4Factory = StringValueObjectFactory.create(prefix4Class, "0.0.0.0/0");
        this.address6Factory = StringValueObjectFactory.create(addr6Class, "::0");
        this.prefix6Factory = StringValueObjectFactory.create(prefix6Class, "::0/0");
    }

    protected abstract A ipv4Address(A4 addr);
    protected abstract A ipv6Address(A6 addr);
    protected abstract String ipv4AddressString(A4 addr);
    protected abstract String ipv6AddressString(A6 addr);
    protected abstract String ipv4PrefixString(P4 prefix);
    protected abstract String ipv6PrefixString(P6 prefix);

    @Nonnull public final A ipAddressFor(@Nonnull final byte[] bytes) {
        switch (bytes.length) {
            case 4:
                return ipv4Address(ipv4AddressFor(bytes));
            case 16:
                return ipv6Address(ipv6AddressFor(bytes));
            default:
                throw new IllegalArgumentException("Invalid array length " + bytes.length);
        }
    }

    @Nonnull public final A ipAddressFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        if (addr instanceof Inet4Address) {
            return ipv4Address(ipv4AddressFor(addr));
        } else if (addr instanceof Inet6Address) {
            return ipv6Address(ipv6AddressFor(addr));
        } else {
            throw new IllegalArgumentException("Unhandled address " + addr);
        }
    }

    /**
     * Create an Ipv4Address by interpreting input bytes as an IPv4 address.
     *
     * @param bytes 4-byte array
     * @return An Ipv4Address object
     * @throws IllegalArgumentException if bytes has length different from 4
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public final A4 ipv4AddressFor(@Nonnull final byte[] bytes) {
        return address4Factory.newInstance(addressStringV4(bytes));
    }

    /**
     * Create an Ipv4Address by interpreting an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4Address object
     * @throws IllegalArgumentException if addr is not an {@link Inet4Address}
     * @throws NullPointerException if addr is null
     */
    @Nonnull public final A4 ipv4AddressFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        return address4Factory.newInstance(addr.getHostAddress());
    }

    @Nonnull public final A4 ipv4AddressFrom(@Nonnull final P4 prefix) {
        return prefixToAddress(address4Factory, ipv4PrefixString(prefix));
    }

    /**
     * Create a /32 Ipv4Prefix by interpreting input bytes as an IPv4 address.
     *
     * @param bytes four-byte array
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if bytes has length different from 4
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public final P4 ipv4PrefixFor(@Nonnull final byte[] bytes) {
        return prefix4Factory.newInstance(prefixStringV4(bytes));
    }

    /**
     * Create a Ipv4Prefix by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked, hence input <code>address = { 1, 2, 3, 4 }</code>
     * and <code>mask=24</code> will result in <code>1.2.3.4/24</code>.
     *
     * @param address Input address as a 4-byte array
     * @param mask Prefix mask
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if bytes has length different from 4 or if mask is not in range 0-32
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public final P4 ipv4PrefixFor(@Nonnull final byte[] address, final int mask) {
        return prefix4Factory.newInstance(prefixStringV4(address, mask));
    }

    /**
     * Create a /32 Ipv4Prefix for an {@link Inet4Address}
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if addr is not an Inet4Address
     * @throws NullPointerException if adds is null
     */
    @Nonnull public final P4 ipv4PrefixFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        return prefix4Factory.newInstance(addr.getHostAddress() + "/32");
    }

    /**
     * Create a Ipv4Prefix by combining the address with a mask. The address bytes are not masked.
     *
     * @param addr An {@link Inet4Address}
     * @param mask Prefix mask
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if addr is not an Inet4Address or if mask is not in range 0-32
     * @throws NullPointerException if addr is null
     */
    @Nonnull public final P4 ipv4PrefixFor(@Nonnull final InetAddress addr, final int mask) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        Preconditions.checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);
        return prefix4Factory.newInstance(addr.getHostAddress() + '/' + mask);
    }

    @Nonnull public final P4 ipv4PrefixFor(@Nonnull final A4 addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        return prefix4Factory.newInstance(ipv4AddressString(addr) + "/32");
    }

    @Nonnull public final P4 ipv4PrefixFor(@Nonnull final A4 addr, final int mask) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);
        return prefix4Factory.newInstance(ipv4AddressString(addr) + '/' + mask);
    }

    @Nonnull public final Entry<A4, Integer> splitIpv4Prefix(@Nonnull final P4 prefix) {
        final String str = ipv4PrefixString(prefix);
        final int slash = str.lastIndexOf('/');
        final A4 addr = address4Factory.newInstance(str.substring(0, slash));
        return new SimpleImmutableEntry<>(addr, Integer.valueOf(str.substring(slash + 1)));
    }

    /**
     * Create an Ipv6Address by interpreting input bytes as an IPv6 address.
     *
     * @param bytes 16-byte array
     * @return An Ipv6Address object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public final A6 ipv6AddressFor(@Nonnull final byte[] bytes) {
        return address6Factory.newInstance(addressStringV6(bytes));
    }

    /**
     * Create an Ipv6Address by interpreting an {@link Inet6Address}.
     *
     * @param addr An {@link Inet6Address}
     * @return An Ipv6Address object
     * @throws IllegalArgumentException if addr is not an {@link Inet6Address}
     * @throws NullPointerException if addr is null
     */
    @Nonnull public final A6 ipv6AddressFor(@Nonnull final InetAddress addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet6Address, "Address has to be an Inet6Address");
        return address6Factory.newInstance(addressStringV6(addr));
    }

    @Nonnull public final A6 ipv6AddressFrom(@Nonnull final P6 prefix) {
        return prefixToAddress(address6Factory, ipv6PrefixString(prefix));
    }

    /**
     * Create a /128 Ipv6Prefix by interpreting input bytes as an IPv6 address.
     *
     * @param bytes four-byte array
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public final P6 ipv6PrefixFor(@Nonnull final byte[] bytes) {
        return prefix6Factory.newInstance(addressStringV6(bytes) + "/128");
    }

    /**
     * Create a Ipv6Prefix by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked.
     *
     * @param address Input address as a 4-byte array
     * @param mask Prefix mask
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if bytes has length different from 16 or if mask is not in range 0-128
     * @throws NullPointerException if bytes is null
     */
    @Nonnull public final P6 ipv6PrefixFor(@Nonnull final byte[] address, final int mask) {
        Preconditions.checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return prefix6Factory.newInstance(addressStringV6(address) + '/' + mask);
    }

    /**
     * Create a /128 Ipv6Prefix by interpreting input bytes as an IPv4 address.
     *
     * @param addr an {@link Inet6Address}
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if addr is not an Inet6Address or if mask is not in range 0-128
     * @throws NullPointerException if addr is null
     */
    @Nonnull public final P6 ipv6PrefixFor(@Nonnull final InetAddress addr) {
        return prefix6Factory.newInstance(addressStringV6(addr) + "/128");
    }

    /**
     * Create a Ipv6Prefix by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked.
     *
     * @param addr Input address
     * @param mask Prefix mask
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if addr is not an Inet6Address or if mask is not in range 0-128
     * @throws NullPointerException if addr is null
     */
    @Nonnull public final P6 ipv6PrefixFor(@Nonnull final InetAddress addr, final int mask) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(addr instanceof Inet6Address, "Address has to be an Inet6Address");
        Preconditions.checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return prefix6Factory.newInstance(addressStringV6(addr) + '/' + mask);
    }

    @Nonnull public final P6 ipv6PrefixFor(@Nonnull final A6 addr) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        return prefix6Factory.newInstance(ipv6AddressString(addr) + "/128");
    }

    @Nonnull public final P6 ipv6PrefixFor(@Nonnull final A6 addr, final int mask) {
        Preconditions.checkNotNull(addr, "Address must not be null");
        Preconditions.checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return prefix6Factory.newInstance(ipv6AddressString(addr) + '/' + mask);
    }

    @Nonnull public final Entry<A6, Integer> splitIpv6Prefix(@Nonnull final P6 prefix) {
        final String str = ipv6PrefixString(prefix);
        final int slash = str.lastIndexOf('/');
        final A6 addr = address6Factory.newInstance(str.substring(0, slash));
        return new SimpleImmutableEntry<>(addr, Integer.valueOf(str.substring(slash + 1)));
    }

    private static <T> T prefixToAddress(final StringValueObjectFactory<T> factory, final String str) {
        return factory.newInstance(str.substring(0, str.lastIndexOf('/')));
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
            return addressStringV6(Inet6Address.getByAddress(bytes));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(String.format("Invalid input %s", bytes), e);
        }
    }

    private static String addressStringV6(final InetAddress addr) {
        return InetAddresses.toAddrString(addr);
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
