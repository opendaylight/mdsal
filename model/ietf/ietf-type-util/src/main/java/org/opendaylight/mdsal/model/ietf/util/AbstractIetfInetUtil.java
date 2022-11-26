/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.spec.reflect.StringValueObjectFactory;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 */
@Beta
@SuppressWarnings("checkstyle:classTypeParameterName")
public abstract class AbstractIetfInetUtil<A4, A4NZ extends A4, P4, A6, A6NZ extends A6, P6, A, ANZ, P> {
    private static final int INET4_LENGTH = 4;
    private static final int INET6_LENGTH = 16;

    private final StringValueObjectFactory<A4NZ> address4NoZoneFactory;
    private final StringValueObjectFactory<P4> prefix4Factory;
    private final StringValueObjectFactory<A6NZ> address6NoZoneFactory;
    private final StringValueObjectFactory<P6> prefix6Factory;
    private final Class<A4NZ> addr4nzClass;
    private final Class<A6NZ> addr6nzClass;

    protected AbstractIetfInetUtil(final Class<A4NZ> addr4nzClass, final Class<P4> prefix4Class,
            final Class<A6NZ> addr6nzClass, final Class<P6> prefix6Class) {
        this.addr4nzClass = requireNonNull(addr4nzClass);
        this.addr6nzClass = requireNonNull(addr6nzClass);
        this.address4NoZoneFactory = StringValueObjectFactory.create(addr4nzClass, "0.0.0.0");
        this.prefix4Factory = StringValueObjectFactory.create(prefix4Class, "0.0.0.0/0");
        this.address6NoZoneFactory = StringValueObjectFactory.create(addr6nzClass, "::0");
        this.prefix6Factory = StringValueObjectFactory.create(prefix6Class, "::0/0");
    }

    protected abstract @NonNull A ipv4Address(@NonNull A4NZ addr);

    protected abstract @NonNull ANZ ipv4AddressNoZone(@NonNull A4NZ addr);

    protected abstract @NonNull A ipv6Address(@NonNull A6NZ addr);

    protected abstract @NonNull ANZ ipv6AddressNoZone(@NonNull A6NZ addr);

    protected abstract @Nullable A4 maybeIpv4Address(@NonNull A addr);

    protected abstract @Nullable A4NZ maybeIpv4AddressNoZone(@NonNull ANZ addr);

    protected abstract @Nullable A6 maybeIpv6Address(@NonNull A addr);

    protected abstract @Nullable A6NZ maybeIpv6AddressNoZone(@NonNull ANZ addr);

    protected abstract @NonNull P ipv4Prefix(@NonNull P4 addr);

    protected abstract @NonNull P ipv6Prefix(@NonNull P6 addr);

    protected abstract @NonNull String ipv4AddressString(@NonNull A4 addr);

    protected abstract @NonNull String ipv6AddressString(@NonNull A6 addr);

    protected abstract @NonNull String ipv4PrefixString(@NonNull P4 prefix);

    protected abstract @NonNull String ipv6PrefixString(@NonNull P6 prefix);

    /**
     * Create an IpAddress by interpreting input bytes as an IPv4 or IPv6 address, based on array length.
     *
     * @param bytes 4-byte (IPv4) or 6-byte (IPv6) array
     * @return An IpAddress object
     * @throws IllegalArgumentException if bytes has length different from 4 or 6
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull A ipAddressFor(final byte @NonNull[] bytes) {
        return switch (bytes.length) {
            case INET4_LENGTH -> ipv4Address(ipv4AddressFor(bytes));
            case INET6_LENGTH -> ipv6Address(ipv6AddressFor(bytes));
            default -> throwInvalidArray(bytes);
        };
    }

    public final @NonNull A ipAddressFor(final @NonNull InetAddress addr) {
        requireAddress(addr);
        if (addr instanceof Inet4Address) {
            return ipv4Address(ipv4AddressFor(addr));
        } else if (addr instanceof Inet6Address) {
            return ipv6Address(ipv6AddressFor(addr));
        } else {
            throw unhandledAddress(addr);
        }
    }

    private static <T> @NonNull T requireAddress(final T addr) {
        return requireNonNull(addr, "Address must not be null");
    }

    /**
     * Create an IpAddress by interpreting input bytes as an IPv4 or IPv6 address, based on array length.
     *
     * @param bytes 4-byte (IPv4) or 6-byte (IPv6) array
     * @return A no-zone IpAddress object
     * @throws IllegalArgumentException if bytes has length different from 4 or 6
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull ANZ ipAddressNoZoneFor(final byte @NonNull[] bytes) {
        return switch (bytes.length) {
            case INET4_LENGTH -> ipv4AddressNoZone(ipv4AddressFor(bytes));
            case INET6_LENGTH -> ipv6AddressNoZone(ipv6AddressFor(bytes));
            default -> throwInvalidArray(bytes);
        };
    }

    public final @NonNull ANZ ipAddressNoZoneFor(final @NonNull InetAddress addr) {
        requireAddress(addr);
        if (addr instanceof Inet4Address) {
            return ipv4AddressNoZone(ipv4AddressFor(addr));
        } else if (addr instanceof Inet6Address) {
            return ipv6AddressNoZone(ipv6AddressFor(addr));
        } else {
            throw unhandledAddress(addr);
        }
    }

    private static <T> T throwInvalidArray(final byte[] bytes) {
        throw new IllegalArgumentException("Invalid array length " + bytes.length);
    }

    private static IllegalArgumentException unhandledAddress(final InetAddress addr) {
        return new IllegalArgumentException("Unhandled address " + addr);
    }

    /**
     * Create an IpPrefix by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked.
     *
     * @param bytes Input address as a 4-byte (IPv4) or 16-byte (IPv6) array
     * @param mask Prefix mask
     * @return An IpPrefix object
     * @throws IllegalArgumentException if bytes has length different from 4 or 16 or if mask is not
     *         in range 0-32 or 0-128 respectively
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull P ipPrefixFor(final byte @NonNull[] bytes, final int mask) {
        return switch (bytes.length) {
            case INET4_LENGTH -> ipv4Prefix(ipv4PrefixFor(bytes, mask));
            case INET6_LENGTH -> ipv6Prefix(ipv6PrefixFor(bytes, mask));
            default -> throwInvalidArray(bytes);
        };
    }

    public final @NonNull P ipPrefixFor(final @NonNull InetAddress addr, final int mask) {
        requireAddress(addr);
        if (addr instanceof Inet4Address) {
            return ipv4Prefix(ipv4PrefixFor(addr, mask));
        } else if (addr instanceof Inet6Address) {
            return ipv6Prefix(ipv6PrefixFor(addr, mask));
        } else {
            throw unhandledAddress(addr);
        }
    }

    public final @NonNull P ipPrefixFor(final @NonNull A addr) {
        final A4 v4 = maybeIpv4Address(addr);
        return v4 != null ? ipv4Prefix(ipv4PrefixFor(v4)) : ipv6Prefix(ipv6PrefixFor(coerceIpv6Address(addr)));
    }

    public final @NonNull P ipPrefixForNoZone(final @NonNull ANZ addr) {
        final A4NZ v4 = maybeIpv4AddressNoZone(addr);
        return v4 != null ? ipv4Prefix(ipv4PrefixFor(inet4AddressForNoZone(v4)))
            : ipv6Prefix(ipv6PrefixFor(coerceIpv6AddressNoZone(addr)));
    }

    public final @NonNull InetAddress inetAddressFor(final @NonNull A addr) {
        final A4 v4 = maybeIpv4Address(addr);
        return v4 != null ? inet4AddressFor(v4) : inet6AddressFor(coerceIpv6Address(addr));
    }

    public final @NonNull InetAddress inetAddressForNoZone(final @NonNull ANZ addr) {
        final A4NZ v4 = maybeIpv4AddressNoZone(addr);
        return v4 != null ? inet4AddressForNoZone(v4) : inet6AddressForNoZone(coerceIpv6AddressNoZone(addr));
    }

    public final @NonNull Inet4Address inet4AddressFor(final @NonNull A4 addr) {
        try {
            return (Inet4Address) InetAddress.getByAddress(ipv4AddressBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    public final @NonNull Inet4Address inet4AddressForNoZone(final @NonNull A4NZ addr) {
        try {
            return (Inet4Address) InetAddress.getByAddress(ipv4AddressNoZoneBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    public final @NonNull Inet6Address inet6AddressFor(final @NonNull A6 addr) {
        try {
            return (Inet6Address) InetAddress.getByAddress(ipv6AddressBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    public final @NonNull Inet6Address inet6AddressForNoZone(final @NonNull A6NZ addr) {
        try {
            return (Inet6Address) InetAddress.getByAddress(ipv6AddressNoZoneBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting input bytes as an IPv4 address.
     *
     * @param bytes 4-byte array
     * @return An Ipv4AddressNoZone object
     * @throws IllegalArgumentException if bytes has length different from 4
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull A4NZ ipv4AddressFor(final byte @NonNull[] bytes) {
        return address4NoZoneFactory.newInstance(addressStringV4(bytes));
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4AddressNoZone object
     * @throws IllegalArgumentException if addr is not an {@link Inet4Address}
     * @throws NullPointerException if addr is null
     */
    public final @NonNull A4NZ ipv4AddressFor(final @NonNull InetAddress addr) {
        return address4NoZoneFactory.newInstance(addressStringV4(addr));
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting input 32 bits as an IPv4 address in big-endian format.
     *
     * @param bits 32 bits, big endian
     * @return An Ipv4AddressNoZone object
     */
    public final @NonNull A4NZ ipv4AddressFor(final int bits) {
        return address4NoZoneFactory.newInstance(Ipv4Utils.addressString(bits));
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting an Ipv4Address.
     *
     * @param addr An Ipv4Address
     * @return An Ipv4AddressNoZone object
     * @throws NullPointerException if addr is null
     */
    public final @NonNull A4NZ ipv4AddressNoZoneFor(final @NonNull A4 addr) {
        requireAddress(addr);
        return addr4nzClass.isInstance(addr) ? addr4nzClass.cast(addr)
                : address4NoZoneFactory.newInstance(stripZone(ipv4AddressString(addr)));
    }

    public final @NonNull A4NZ ipv4AddressFrom(final @NonNull P4 prefix) {
        return prefixToAddress(address4NoZoneFactory, ipv4PrefixString(prefix));
    }

    public final byte @NonNull[] ipv4AddressBytes(final @NonNull A4 addr) {
        /*
         * This implementation relies heavily on the input string having been validated to comply with
         * the Ipv4Address pattern, which may include a zone index.
         */
        final String str = ipv4AddressString(addr);
        final int percent = str.indexOf('%');
        return Ipv4Utils.addressBytes(str, percent == -1 ? str.length() : percent);
    }

    public final int ipv4AddressBits(final @NonNull A4 addr) {
        final String str = ipv4AddressString(addr);
        final int percent = str.indexOf('%');
        return Ipv4Utils.addressBits(str, percent == -1 ? str.length() : percent);
    }

    public final byte @NonNull[] ipv4AddressNoZoneBytes(final @NonNull A4NZ addr) {
        /*
         * This implementation relies heavily on the input string having been validated to comply with
         * the Ipv4AddressNoZone pattern, which must not include a zone index.
         */
        final String str = ipv4AddressString(addr);
        return Ipv4Utils.addressBytes(str, str.length());
    }

    public final int ipv4AddressNoZoneBits(final @NonNull A4NZ addr) {
        final String str = ipv4AddressString(addr);
        return Ipv4Utils.addressBits(str, str.length());
    }

    /**
     * Create a /32 Ipv4Prefix by interpreting input bytes as an IPv4 address.
     *
     * @param bytes four-byte array
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if bytes has length different from 4
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull P4 ipv4PrefixFor(final byte @NonNull[] bytes) {
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
    public final @NonNull P4 ipv4PrefixFor(final byte @NonNull[] address, final int mask) {
        return prefix4Factory.newInstance(prefixStringV4(address, mask));
    }

    /**
     * Create a /32 Ipv4Prefix for an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if addr is not an Inet4Address
     * @throws NullPointerException if addr is null
     */
    public final @NonNull P4 ipv4PrefixFor(final @NonNull InetAddress addr) {
        return prefix4Factory.newInstance(addressStringV4(addr) + "/32");
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
    public final @NonNull P4 ipv4PrefixFor(final @NonNull InetAddress addr, final int mask) {
        return newIpv4Prefix(addressStringV4(addr), mask);
    }

    public final @NonNull P4 ipv4PrefixFor(final @NonNull A4 addr) {
        return prefix4Factory.newInstance(stripZone(ipv4AddressString(requireAddress(addr))) + "/32");
    }

    public final @NonNull P4 ipv4PrefixFor(final @NonNull A4 addr, final int mask) {
        return newIpv4Prefix(stripZone(ipv4AddressString(requireAddress(addr))), mask);
    }

    public final @NonNull P4 ipv4PrefixForNoZone(final @NonNull A4NZ addr) {
        return prefix4Factory.newInstance(ipv4AddressString(requireAddress(addr)) + "/32");
    }

    public final @NonNull P4 ipv4PrefixForNoZone(final @NonNull A4NZ addr, final int mask) {
        return newIpv4Prefix(ipv4AddressString(requireAddress(addr)), mask);
    }

    public final @NonNull P4 ipv4PrefixForShort(final byte @NonNull[] address, final int mask) {
        if (mask == 0) {
            // Easy case, reuse the template
            return prefix4Factory.getTemplate();
        }

        return v4PrefixForShort(address, 0, mask / Byte.SIZE + (mask % Byte.SIZE == 0 ? 0 : 1), mask);
    }

    public final @NonNull P4 ipv4PrefixForShort(final byte @NonNull[] array, final int startOffset, final int mask) {
        if (mask == 0) {
            // Easy case, reuse the template
            return prefix4Factory.getTemplate();
        }

        return v4PrefixForShort(array, startOffset, mask / Byte.SIZE + (mask % Byte.SIZE == 0 ? 0 : 1), mask);
    }

    private static String stripZone(final String str) {
        final int percent = str.indexOf('%');
        return percent == -1 ? str : str.substring(0, percent);
    }

    private @NonNull P4 newIpv4Prefix(final String addr, final int mask) {
        checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);
        return prefix4Factory.newInstance(addr + '/' + mask);
    }

    public final @NonNull Entry<A4NZ, Integer> splitIpv4Prefix(final @NonNull P4 prefix) {
        return splitPrefix(address4NoZoneFactory, ipv4PrefixString(prefix));
    }

    public final byte @NonNull[] ipv4PrefixToBytes(final @NonNull P4 prefix) {
        final String str = ipv4PrefixString(prefix);
        final int slash = str.lastIndexOf('/');

        final byte[] bytes = new byte[INET4_LENGTH + 1];
        Ipv4Utils.fillIpv4Bytes(bytes, 0, str, 0, slash);
        bytes[INET4_LENGTH] = (byte)Integer.parseInt(str.substring(slash + 1), 10);
        return bytes;
    }

    /**
     * Create an Ipv6Address by interpreting input bytes as an IPv6 address.
     *
     * @param bytes 16-byte array
     * @return An Ipv6Address object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull A6NZ ipv6AddressFor(final byte @NonNull[] bytes) {
        return address6NoZoneFactory.newInstance(addressStringV6(bytes));
    }

    /**
     * Create an Ipv6Address by interpreting an {@link Inet6Address}.
     *
     * @param addr An {@link Inet6Address}
     * @return An Ipv6Address object
     * @throws IllegalArgumentException if addr is not an {@link Inet6Address}
     * @throws NullPointerException if addr is null
     */
    public final @NonNull A6NZ ipv6AddressFor(final @NonNull InetAddress addr) {
        return address6NoZoneFactory.newInstance(addressStringV6(addr));
    }

    /**
     * Create an Ipv6AddressNoZone by interpreting an Ipv6Address.
     *
     * @param addr An Ipv6Address
     * @return An Ipv6AddressNoZone object
     * @throws NullPointerException if addr is null
     */
    public final @NonNull A6NZ ipv6AddressNoZoneFor(final @NonNull A6 addr) {
        requireAddress(addr);
        return addr6nzClass.isInstance(addr) ? addr6nzClass.cast(addr)
                : address6NoZoneFactory.newInstance(stripZone(ipv6AddressString(addr)));
    }

    public final @NonNull A6NZ ipv6AddressFrom(final @NonNull P6 prefix) {
        return prefixToAddress(address6NoZoneFactory, ipv6PrefixString(prefix));
    }

    public final byte @NonNull[] ipv6AddressBytes(final @NonNull A6 addr) {
        final String str = ipv6AddressString(addr);
        final int percent = str.indexOf('%');
        return ipv6StringBytes(str, percent == -1 ? str.length() : percent);
    }

    public final byte @NonNull[] ipv6AddressNoZoneBytes(final @NonNull A6NZ addr) {
        final String str = ipv6AddressString(addr);
        return ipv6StringBytes(str, str.length());
    }

    private static byte @NonNull[] ipv6StringBytes(final @NonNull String str, final int limit) {
        final byte[] bytes = new byte[INET6_LENGTH];
        Ipv6Utils.fillIpv6Bytes(bytes, str, limit);
        return bytes;
    }

    /**
     * Create a /128 Ipv6Prefix by interpreting input bytes as an IPv6 address.
     *
     * @param bytes four-byte array
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull P6 ipv6PrefixFor(final byte @NonNull[] bytes) {
        return prefix6Factory.newInstance(addressStringV6(bytes) + "/128");
    }

    /**
     * Create a Ipv6Prefix by combining the address with a mask. The address
     * bytes are interpreted as an address and the specified mask is concatenated to
     * it. The address bytes are not masked.
     *
     * @param address Input address as a 16-byte array
     * @param mask Prefix mask
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if bytes has length different from 16 or if mask is not in range 0-128
     * @throws NullPointerException if bytes is null
     */
    public final @NonNull P6 ipv6PrefixFor(final byte @NonNull[] address, final int mask) {
        checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return prefix6Factory.newInstance(addressStringV6(address) + '/' + mask);
    }

    /**
     * Create a /128 Ipv6Prefix by interpreting input bytes as an IPv6 address.
     *
     * @param addr an {@link Inet6Address}
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if addr is not an Inet6Address
     * @throws NullPointerException if addr is null
     */
    public final @NonNull P6 ipv6PrefixFor(final @NonNull InetAddress addr) {
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
    public final @NonNull P6 ipv6PrefixFor(final @NonNull InetAddress addr, final int mask) {
        checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return prefix6Factory.newInstance(addressStringV6(addr) + '/' + mask);
    }

    public final @NonNull P6 ipv6PrefixFor(final @NonNull A6 addr) {
        return prefix6Factory.newInstance(stripZone(ipv6AddressString(requireAddress(addr))) + "/128");
    }

    public final @NonNull P6 ipv6PrefixFor(final @NonNull A6 addr, final int mask) {
        return newIpv6Prefix(stripZone(ipv6AddressString(requireAddress(addr))), mask);
    }

    public final @NonNull P6 ipv6PrefixForNoZone(final @NonNull A6NZ addr) {
        return prefix6Factory.newInstance(ipv6AddressString(requireAddress(addr)) + "/128");
    }

    public final @NonNull P6 ipv6PrefixForNoZone(final @NonNull A6NZ addr, final int mask) {
        return newIpv6Prefix(ipv6AddressString(requireAddress(addr)), mask);
    }

    public final @NonNull P6 ipv6PrefixForShort(final byte @NonNull[] address, final int mask) {
        return ipv6PrefixForShort(address, 0, mask);
    }

    public final @NonNull P6 ipv6PrefixForShort(final byte @NonNull[] array, final int startOffset, final int mask) {
        if (mask == 0) {
            // Easy case, reuse the template
            return prefix6Factory.getTemplate();
        }

        checkArgument(mask > 0 && mask <= 128, "Invalid mask %s", mask);
        final int size = mask / Byte.SIZE + (mask % Byte.SIZE == 0 ? 0 : 1);

        // Until we can instantiate an IPv6 address for a partial array, use a temporary buffer
        byte[] tmp = new byte[INET6_LENGTH];
        System.arraycopy(array, startOffset, tmp, 0, size);
        return ipv6PrefixFor(tmp, mask);
    }

    private P6 newIpv6Prefix(final String addr, final int mask) {
        checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return prefix6Factory.newInstance(addr + '/' + mask);
    }

    public final @NonNull Entry<A6NZ, Integer> splitIpv6Prefix(final @NonNull P6 prefix) {
        return splitPrefix(address6NoZoneFactory, ipv6PrefixString(prefix));
    }

    private static <T> @NonNull T prefixToAddress(final StringValueObjectFactory<T> factory, final String str) {
        return factory.newInstance(str.substring(0, str.lastIndexOf('/')));
    }

    private static <T> @NonNull Entry<T, Integer> splitPrefix(final StringValueObjectFactory<T> factory,
            final String str) {
        final int slash = str.lastIndexOf('/');
        return new SimpleImmutableEntry<>(factory.newInstance(str.substring(0, slash)),
                Integer.valueOf(str.substring(slash + 1)));
    }

    public final byte @NonNull[] ipv6PrefixToBytes(final @NonNull P6 prefix) {
        final String str = ipv6PrefixString(prefix);
        final byte[] bytes = new byte[INET6_LENGTH + 1];
        final int slash = str.lastIndexOf('/');
        Ipv6Utils.fillIpv6Bytes(bytes, str, slash);
        bytes[INET6_LENGTH] = (byte)Integer.parseInt(str.substring(slash + 1), 10);
        return bytes;
    }

    private static void appendIpv4String(final StringBuilder sb, final byte @NonNull[] bytes) {
        checkArgument(bytes.length == INET4_LENGTH, "IPv4 address length is 4 bytes");

        sb.append(Byte.toUnsignedInt(bytes[0]));
        for (int i = 1; i < INET4_LENGTH; ++i) {
            sb.append('.').append(Byte.toUnsignedInt(bytes[i]));
        }
    }

    static String addressStringV4(final byte @NonNull[] bytes) {
        final StringBuilder sb = new StringBuilder(15);
        appendIpv4String(sb, bytes);
        return sb.toString();
    }

    private static @NonNull String addressStringV4(final InetAddress addr) {
        requireAddress(addr);
        checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        return addr.getHostAddress();
    }

    private static String addressStringV6(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == INET6_LENGTH, "IPv6 address length is 16 bytes");

        try {
            return addressStringV6(Inet6Address.getByAddress(null, bytes, null));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(String.format("Invalid input %s", bytes), e);
        }
    }

    private static String addressStringV6(final InetAddress addr) {
        requireAddress(addr);
        checkArgument(addr instanceof Inet6Address, "Address has to be an Inet6Address");
        return addressStringV6((Inet6Address) addr);
    }

    private static String addressStringV6(final Inet6Address addr) {
        return InetAddresses.toAddrString(addr);
    }

    private static String prefixStringV4(final byte @NonNull[] bytes) {
        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        return sb.append("/32").toString();
    }

    private static String prefixStringV4(final byte @NonNull[] bytes, final int mask) {
        checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);

        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        return sb.append('/').append(mask).toString();
    }

    private P4 v4PrefixForShort(final byte @NonNull[] array, final int startOffset, final int size, final int mask) {
        if (startOffset == 0 && size == INET4_LENGTH && array.length == INET4_LENGTH) {
            // Easy case, fall back to non-short
            return ipv4PrefixFor(array, mask);
        }

        final StringBuilder sb = new StringBuilder(18);

        // Add from address
        sb.append(Byte.toUnsignedInt(array[startOffset]));
        for (int i = 1; i < size; i++) {
            sb.append('.').append(Byte.toUnsignedInt(array[startOffset + i]));
        }

        // Add zeros
        for (int i = size; i < INET4_LENGTH; i++) {
            sb.append(".0");
        }

        // Add mask
        checkArgument(mask > 0 && mask <= 32, "Invalid mask %s", mask);
        sb.append('/').append(mask);

        return prefix4Factory.newInstance(sb.toString());
    }

    private @NonNull A6 coerceIpv6Address(final @NonNull A addr) {
        final A6 ret = maybeIpv6Address(addr);
        checkArgument(ret != null, "Address %s is neither IPv4 nor IPv6", addr);
        return ret;
    }

    private @NonNull A6NZ coerceIpv6AddressNoZone(final @NonNull ANZ addr) {
        final A6NZ ret = maybeIpv6AddressNoZone(addr);
        checkArgument(ret != null, "Address %s is neither IPv4 nor IPv6", addr);
        return ret;
    }
}
