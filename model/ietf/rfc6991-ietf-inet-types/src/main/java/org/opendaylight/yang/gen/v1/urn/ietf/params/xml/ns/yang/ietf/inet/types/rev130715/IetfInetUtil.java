/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.model.ietf.type.util.Ipv4Utils;
import org.opendaylight.mdsal.model.ietf.type.util.Ipv6Utils;
import org.opendaylight.yangtools.binding.reflect.StringValueObjectFactory;

/**
 * A set of utility methods to efficiently instantiate various {@code ietf-inet-types} DTOs.
 */
public final class IetfInetUtil {
    private static final StringValueObjectFactory<Ipv4AddressNoZone> V4NZ_FACTORY =
        StringValueObjectFactory.create(Ipv4AddressNoZone.class, "0.0.0.0");
    private static final StringValueObjectFactory<Ipv4Prefix> P4_FACTORY =
        StringValueObjectFactory.create(Ipv4Prefix.class, "0.0.0.0/0");
    private static final StringValueObjectFactory<Ipv6AddressNoZone> V6NZ_FACTORY =
        StringValueObjectFactory.create(Ipv6AddressNoZone.class, "::0");
    private static final StringValueObjectFactory<Ipv6Prefix> P6_FACTORY =
        StringValueObjectFactory.create(Ipv6Prefix.class, "::0/0");
    private static final Pattern HOST_IPV4_PATTERN = Pattern.compile(
        "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])"
            + "(%[\\p{N}\\p{L}]+)?");
    private static final Pattern HOST_IPV6_PATTERN1 = Pattern.compile("((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}"
        +"((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}"
        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?");
    private static final Pattern HOST_IPV6_PATTERN2 = Pattern.compile(
        "(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?");
    private static final Pattern HOST_DOMAIN_PATTERN = Pattern.compile(
        "((([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.)*([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?"
            +"[a-zA-Z0-9]\\.?)|\\.");

    private static final Pattern IPADDRESS_IPV4_PATTERN;
    static {
        verify(Ipv4Address.PATTERN_CONSTANTS.size() == 1);
        IPADDRESS_IPV4_PATTERN = Pattern.compile(Ipv4Address.PATTERN_CONSTANTS.get(0));
    }

    private static final Pattern IPADDRESS_NO_ZONE_IPV4_PATTERN = Pattern.compile("[0-9\\.]*");
    private static final Pattern IPADDRESS_NO_ZONE_IPV6_PATTERN = Pattern.compile("[0-9a-fA-F:\\.]*");

    private static final Pattern IPPREFIX_IPV4_PATTERN;
    static {
        verify(Ipv4Prefix.PATTERN_CONSTANTS.size() == 1);
        IPPREFIX_IPV4_PATTERN = Pattern.compile(Ipv4Prefix.PATTERN_CONSTANTS.get(0));
    }

    @Beta
    public static Host hostFor(final String str) {
        final var ipv4Matcher = HOST_IPV4_PATTERN.matcher(str);
        final var ipv6Matcher1 = HOST_IPV6_PATTERN1.matcher(str);
        final var ipv6Matcher2 = HOST_IPV6_PATTERN2.matcher(str);
        final var domainMatcher = HOST_DOMAIN_PATTERN.matcher(str);
        final var matchers = new ArrayList<>(3);
        if (ipv6Matcher1.matches() || ipv6Matcher2.matches()) {
            matchers.add(Ipv6Address.class.getSimpleName());
        }
        // Ipv4 and Domain Name patterns are not exclusive
        // Address 127.0.0.1 matches both patterns
        // This way Ipv4 address is preferred to domain name
        if (ipv4Matcher.matches()) {
            matchers.add(Ipv4Address.class.getSimpleName());
        } else if (domainMatcher.matches()) {
            matchers.add(DomainName.class.getSimpleName());
        }
        if (matchers.size() > 1) {
            throw new IllegalArgumentException("Cannot create Host from " + str + ". Value is ambigious for "
                + matchers);
        }

        final IpAddress ipAddress;
        if (ipv4Matcher.matches()) {
            ipAddress = new IpAddress(new Ipv4Address(str));
        } else if (ipv6Matcher1.matches() || ipv6Matcher2.matches()) {
            ipAddress = new IpAddress(new Ipv6Address(str));
        } else if (domainMatcher.matches()) {
            return new Host(new DomainName(str));
        } else {
            throw new IllegalArgumentException("Cannot create Host from " + str);
        }
        return new Host(ipAddress);
    }

    @Beta
    public static IpAddress ipAddressFor(final String str) {
        return IPADDRESS_IPV4_PATTERN.matcher(str).matches() ? new IpAddress(new Ipv4Address(str))
                : new IpAddress(new Ipv6Address(str));
    }

    @Beta
    public static IpAddressNoZone ipAddressNoZoneFor(final String str) {
        if (IPADDRESS_NO_ZONE_IPV4_PATTERN.matcher(str).matches()) {
            return new IpAddressNoZone(new Ipv4AddressNoZone(str));
        } else if (IPADDRESS_NO_ZONE_IPV6_PATTERN.matcher(str).matches()) {
            return new IpAddressNoZone(new Ipv6AddressNoZone(str));
        } else {
            throw new IllegalArgumentException("Cannot create IpAddress from " + str);
        }
    }

    @Beta
    public static IpPrefix ipPrefixFor(final String defaultValue) {
        return IPPREFIX_IPV4_PATTERN.matcher(defaultValue).matches() ? new IpPrefix(new Ipv4Prefix(defaultValue))
                : new IpPrefix(new Ipv6Prefix(defaultValue));
    }

    /**
     * Create an IpAddress by interpreting input bytes as an IPv4 or IPv6 address, based on array length.
     *
     * @param bytes 4-byte (IPv4) or 6-byte (IPv6) array
     * @return An IpAddress object
     * @throws IllegalArgumentException if bytes has length different from 4 or 6
     * @throws NullPointerException if bytes is null
     */
    public static @NonNull IpAddress ipAddressFor(final byte @NonNull[] bytes) {
        return switch (bytes.length) {
            case Ipv4Utils.INET4_LENGTH -> new IpAddress(ipv4AddressFor(bytes));
            case Ipv6Utils.INET6_LENGTH -> new IpAddress(ipv6AddressFor(bytes));
            default -> throwInvalidArray(bytes);
        };
    }

    public static @NonNull IpAddress ipAddressFor(final @NonNull InetAddress addr) {
        return switch (addr) {
            case Inet4Address inet4 -> new IpAddress(ipv4AddressFor(inet4));
            case Inet6Address inet6 -> new IpAddress(ipv6AddressFor(inet6));
            default -> throw unhandledAddress(addr);
        };
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
    public static @NonNull IpAddressNoZone ipAddressNoZoneFor(final byte @NonNull[] bytes) {
        return switch (bytes.length) {
            case Ipv4Utils.INET4_LENGTH -> new IpAddressNoZone(ipv4AddressFor(bytes));
            case Ipv6Utils.INET6_LENGTH -> new IpAddressNoZone(ipv6AddressFor(bytes));
            default -> throwInvalidArray(bytes);
        };
    }

    public static @NonNull IpAddressNoZone ipAddressNoZoneFor(final @NonNull InetAddress addr) {
        return switch (addr) {
            case Inet4Address inet4 -> new IpAddressNoZone(ipv4AddressFor(inet4));
            case Inet6Address inet6 -> new IpAddressNoZone(ipv6AddressFor(inet6));
            default -> throw unhandledAddress(addr);
        };
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
    public static @NonNull IpPrefix ipPrefixFor(final byte @NonNull[] bytes, final int mask) {
        return switch (bytes.length) {
            case Ipv4Utils.INET4_LENGTH -> new IpPrefix(ipv4PrefixFor(bytes, mask));
            case Ipv6Utils.INET6_LENGTH -> new IpPrefix(ipv6PrefixFor(bytes, mask));
            default -> throwInvalidArray(bytes);
        };
    }

    public static @NonNull IpPrefix ipPrefixFor(final @NonNull InetAddress addr, final int mask) {
        return switch (addr) {
            case Inet4Address inet4 -> new IpPrefix(newIpv4Prefix(inet4.getHostAddress(), mask));
            case Inet6Address inet6 -> new IpPrefix(newIpv6Prefix(inet6.getHostAddress(), mask));
            default -> throw unhandledAddress(addr);
        };
    }

    public static @NonNull IpPrefix ipPrefixFor(final @NonNull IpAddress addr) {
        final var v4 = addr.getIpv4Address();
        return v4 != null ? new IpPrefix(ipv4PrefixFor(v4)) : new IpPrefix(ipv6PrefixFor(coerceIpv6Address(addr)));
    }

    public static @NonNull IpPrefix ipPrefixForNoZone(final @NonNull IpAddressNoZone addr) {
        final var v4 = addr.getIpv4AddressNoZone();
        return v4 != null ? new IpPrefix(ipv4PrefixFor(inet4AddressForNoZone(v4)))
            : new IpPrefix(ipv6PrefixFor(coerceIpv6AddressNoZone(addr)));
    }

    public static @NonNull InetAddress inetAddressFor(final @NonNull IpAddress addr) {
        final var v4 = addr.getIpv4Address();
        return v4 != null ? inet4AddressFor(v4) : inet6AddressFor(coerceIpv6Address(addr));
    }

    public static @NonNull InetAddress inetAddressForNoZone(final @NonNull IpAddressNoZone addr) {
        final var v4 = addr.getIpv4AddressNoZone();
        return v4 != null ? inet4AddressForNoZone(v4) : inet6AddressForNoZone(coerceIpv6AddressNoZone(addr));
    }

    public static @NonNull Inet4Address inet4AddressFor(final @NonNull Ipv4Address addr) {
        try {
            return (Inet4Address) InetAddress.getByAddress(ipv4AddressBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    public static @NonNull Inet4Address inet4AddressForNoZone(final @NonNull Ipv4AddressNoZone addr) {
        try {
            return (Inet4Address) InetAddress.getByAddress(ipv4AddressNoZoneBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    public static @NonNull Inet6Address inet6AddressFor(final @NonNull Ipv6Address addr) {
        try {
            return (Inet6Address) InetAddress.getByAddress(ipv6AddressBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    public static @NonNull Inet6Address inet6AddressForNoZone(final @NonNull Ipv6AddressNoZone addr) {
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
    public static @NonNull Ipv4AddressNoZone ipv4AddressFor(final byte @NonNull[] bytes) {
        return V4NZ_FACTORY.newInstance(Ipv4Utils.addressString(bytes));
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4AddressNoZone object
     * @throws IllegalArgumentException if addr is not an {@link Inet4Address}
     * @throws NullPointerException if addr is null
     */
    public static @NonNull Ipv4AddressNoZone ipv4AddressFor(final @NonNull InetAddress addr) {
        return V4NZ_FACTORY.newInstance(addressStringV4(addr));
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4AddressNoZone object
     * @throws NullPointerException if addr is null
     */
    public static @NonNull Ipv4AddressNoZone ipv4AddressFor(final @NonNull Inet4Address addr) {
        return V4NZ_FACTORY.newInstance(addr.getHostAddress());
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting input 32 bits as an IPv4 address in big-endian format.
     *
     * @param bits 32 bits, big endian
     * @return An Ipv4AddressNoZone object
     */
    public static @NonNull Ipv4AddressNoZone ipv4AddressFor(final int bits) {
        return V4NZ_FACTORY.newInstance(Ipv4Utils.addressString(bits));
    }

    /**
     * Create an Ipv4AddressNoZone by interpreting an Ipv4Address.
     *
     * @param addr An Ipv4Address
     * @return An Ipv4AddressNoZone object
     * @throws NullPointerException if {@code addr} is {@code null}
     */
    public static @NonNull Ipv4AddressNoZone ipv4AddressNoZoneFor(final @NonNull Ipv4Address addr) {
        return addr instanceof Ipv4AddressNoZone noZone ? noZone
            :  V4NZ_FACTORY.newInstance(stripZone(requireAddress(addr).getValue()));
    }

    public static @NonNull Ipv4AddressNoZone ipv4AddressFrom(final @NonNull Ipv4Prefix prefix) {
        return prefixToAddress(V4NZ_FACTORY, prefix.getValue());
    }

    public static byte @NonNull[] ipv4AddressBytes(final @NonNull Ipv4Address addr) {
        /*
         * This implementation relies heavily on the input string having been validated to comply with
         * the Ipv4Address pattern, which may include a zone index.
         */
        final var str = addr.getValue();
        final int percent = str.indexOf('%');
        return Ipv4Utils.addressBytes(str, percent == -1 ? str.length() : percent);
    }

    public static int ipv4AddressBits(final @NonNull Ipv4Address addr) {
        final var str = addr.getValue();
        final int percent = str.indexOf('%');
        return Ipv4Utils.addressBits(str, percent == -1 ? str.length() : percent);
    }

    public static byte @NonNull[] ipv4AddressNoZoneBytes(final @NonNull Ipv4AddressNoZone addr) {
        /*
         * This implementation relies heavily on the input string having been validated to comply with
         * the Ipv4AddressNoZone pattern, which must not include a zone index.
         */
        final String str = addr.getValue();
        return Ipv4Utils.addressBytes(str, str.length());
    }

    public static int ipv4AddressNoZoneBits(final @NonNull Ipv4AddressNoZone addr) {
        final var str = addr.getValue();
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
    public static @NonNull Ipv4Prefix ipv4PrefixFor(final byte @NonNull[] bytes) {
        return P4_FACTORY.newInstance(prefixStringV4(bytes));
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
    public static @NonNull Ipv4Prefix ipv4PrefixFor(final byte @NonNull[] address, final int mask) {
        return P4_FACTORY.newInstance(prefixStringV4(address, mask));
    }

    /**
     * Create a /32 Ipv4Prefix for an {@link Inet4Address}.
     *
     * @param addr An {@link Inet4Address}
     * @return An Ipv4Prefix object
     * @throws IllegalArgumentException if addr is not an Inet4Address
     * @throws NullPointerException if addr is null
     */
    public static @NonNull Ipv4Prefix ipv4PrefixFor(final @NonNull InetAddress addr) {
        return P4_FACTORY.newInstance(addressStringV4(addr) + "/32");
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
    public static @NonNull Ipv4Prefix ipv4PrefixFor(final @NonNull InetAddress addr, final int mask) {
        return newIpv4Prefix(addressStringV4(addr), mask);
    }

    public static @NonNull Ipv4Prefix ipv4PrefixFor(final @NonNull Ipv4Address addr) {
        return P4_FACTORY.newInstance(stripZone(addr.getValue()) + "/32");
    }

    public static @NonNull Ipv4Prefix ipv4PrefixFor(final @NonNull Ipv4Address addr, final int mask) {
        return newIpv4Prefix(stripZone(addr.getValue()), mask);
    }

    public static @NonNull Ipv4Prefix ipv4PrefixForNoZone(final @NonNull Ipv4AddressNoZone addr) {
        return P4_FACTORY.newInstance(addr.getValue() + "/32");
    }

    public static @NonNull Ipv4Prefix ipv4PrefixForNoZone(final @NonNull Ipv4AddressNoZone addr, final int mask) {
        return newIpv4Prefix(addr.getValue(), mask);
    }

    public static @NonNull Ipv4Prefix ipv4PrefixForShort(final byte @NonNull[] address, final int mask) {
        if (mask == 0) {
            // Easy case, reuse the template
            return P4_FACTORY.getTemplate();
        }

        return v4PrefixForShort(address, 0, mask / Byte.SIZE + (mask % Byte.SIZE == 0 ? 0 : 1), mask);
    }

    public static @NonNull Ipv4Prefix ipv4PrefixForShort(final byte @NonNull[] array, final int startOffset,
            final int mask) {
        if (mask == 0) {
            // Easy case, reuse the template
            return P4_FACTORY.getTemplate();
        }

        return v4PrefixForShort(array, startOffset, mask / Byte.SIZE + (mask % Byte.SIZE == 0 ? 0 : 1), mask);
    }

    private static String stripZone(final String str) {
        final int percent = str.indexOf('%');
        return percent == -1 ? str : str.substring(0, percent);
    }

    private static @NonNull Ipv4Prefix newIpv4Prefix(final String addr, final int mask) {
        checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);
        return P4_FACTORY.newInstance(addr + '/' + mask);
    }

    public static @NonNull Entry<Ipv4AddressNoZone, Integer> splitIpv4Prefix(final @NonNull Ipv4Prefix prefix) {
        return splitPrefix(V4NZ_FACTORY, prefix.getValue());
    }

    public static byte @NonNull[] ipv4PrefixToBytes(final @NonNull Ipv4Prefix prefix) {
        final var str = prefix.getValue();
        final int slash = str.lastIndexOf('/');

        final byte[] bytes = new byte[Ipv4Utils.INET4_LENGTH + 1];
        Ipv4Utils.fillIpv4Bytes(bytes, 0, str, 0, slash);
        bytes[Ipv4Utils.INET4_LENGTH] = (byte)Integer.parseInt(str.substring(slash + 1), 10);
        return bytes;
    }

    /**
     * Create an Ipv6Address by interpreting input bytes as an IPv6 address.
     *
     * @param bytes 16-byte array
     * @return An Ipv6Address object
     * @throws IllegalArgumentException if bytes has length different from 16
     * @throws NullPointerException if {@code bytes} is {@code null}
     */
    public static @NonNull Ipv6AddressNoZone ipv6AddressFor(final byte @NonNull[] bytes) {
        return V6NZ_FACTORY.newInstance(addressStringV6(bytes));
    }

    /**
     * Create an Ipv6Address by interpreting an {@link InetAddress}.
     *
     * @param addr An {@link InetAddress}
     * @return An Ipv6Address object
     * @throws IllegalArgumentException if @{code addr} is not an {@link Inet6Address}
     * @throws NullPointerException if {@code addr} is {@code null}
     */
    public static @NonNull Ipv6AddressNoZone ipv6AddressFor(final @NonNull InetAddress addr) {
        return V6NZ_FACTORY.newInstance(addressStringV6(addr));
    }

    /**
     * Create an Ipv6Address from an {@link Inet6Address}.
     *
     * @param addr An {@link Inet6Address}
     * @return An Ipv6Address object
     * @throws NullPointerException if {@code addr} is {@code null}
     */
    public static @NonNull Ipv6AddressNoZone ipv6AddressFor(final @NonNull Inet6Address addr) {
        return V6NZ_FACTORY.newInstance(addressStringV6(addr));
    }

    /**
     * Create an Ipv6AddressNoZone by interpreting an Ipv6Address.
     *
     * @param addr An Ipv6Address
     * @return An Ipv6AddressNoZone object
     * @throws NullPointerException if addr is null
     */
    public static @NonNull Ipv6AddressNoZone ipv6AddressNoZoneFor(final @NonNull Ipv6Address addr) {
        return addr instanceof Ipv6AddressNoZone noZone ? noZone
            : V6NZ_FACTORY.newInstance(stripZone(requireAddress(addr).getValue()));
    }

    public static @NonNull Ipv6AddressNoZone ipv6AddressFrom(final @NonNull Ipv6Prefix prefix) {
        return prefixToAddress(V6NZ_FACTORY, prefix.getValue());
    }

    public static byte @NonNull[] ipv6AddressBytes(final @NonNull Ipv6Address addr) {
        final var str = addr.getValue();
        final int percent = str.indexOf('%');
        return ipv6StringBytes(str, percent == -1 ? str.length() : percent);
    }

    public static byte @NonNull[] ipv6AddressNoZoneBytes(final @NonNull Ipv6Address addr) {
        final var str = addr.getValue();
        return ipv6StringBytes(str, str.length());
    }

    private static byte @NonNull[] ipv6StringBytes(final @NonNull String str, final int limit) {
        final byte[] bytes = new byte[Ipv6Utils.INET6_LENGTH];
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
    public static @NonNull Ipv6Prefix ipv6PrefixFor(final byte @NonNull[] bytes) {
        return P6_FACTORY.newInstance(addressStringV6(bytes) + "/128");
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
    public static @NonNull Ipv6Prefix ipv6PrefixFor(final byte @NonNull[] address, final int mask) {
        checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return P6_FACTORY.newInstance(addressStringV6(address) + '/' + mask);
    }

    /**
     * Create a /128 Ipv6Prefix by interpreting input bytes as an IPv6 address.
     *
     * @param addr an {@link Inet6Address}
     * @return An Ipv6Prefix object
     * @throws IllegalArgumentException if addr is not an Inet6Address
     * @throws NullPointerException if addr is null
     */
    public static @NonNull Ipv6Prefix ipv6PrefixFor(final @NonNull InetAddress addr) {
        return P6_FACTORY.newInstance(addressStringV6(addr) + "/128");
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
    public static @NonNull Ipv6Prefix ipv6PrefixFor(final @NonNull InetAddress addr, final int mask) {
        checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return P6_FACTORY.newInstance(addressStringV6(addr) + '/' + mask);
    }

    public static @NonNull Ipv6Prefix ipv6PrefixFor(final @NonNull Ipv6Address addr) {
        return P6_FACTORY.newInstance(stripZone(addr.getValue()) + "/128");
    }

    public static @NonNull Ipv6Prefix ipv6PrefixFor(final @NonNull Ipv6Address addr, final int mask) {
        return newIpv6Prefix(stripZone(addr.getValue()), mask);
    }

    public static @NonNull Ipv6Prefix ipv6PrefixForNoZone(final @NonNull Ipv6AddressNoZone addr) {
        return P6_FACTORY.newInstance(addr.getValue() + "/128");
    }

    public static @NonNull Ipv6Prefix ipv6PrefixForNoZone(final @NonNull Ipv6AddressNoZone addr, final int mask) {
        return newIpv6Prefix(addr.getValue(), mask);
    }

    public static @NonNull Ipv6Prefix ipv6PrefixForShort(final byte @NonNull[] address, final int mask) {
        return ipv6PrefixForShort(address, 0, mask);
    }

    public static @NonNull Ipv6Prefix ipv6PrefixForShort(final byte @NonNull[] array, final int startOffset,
            final int mask) {
        if (mask == 0) {
            // Easy case, reuse the template
            return P6_FACTORY.getTemplate();
        }

        checkArgument(mask > 0 && mask <= 128, "Invalid mask %s", mask);
        final int size = mask / Byte.SIZE + (mask % Byte.SIZE == 0 ? 0 : 1);

        // Until we can instantiate an IPv6 address for a partial array, use a temporary buffer
        byte[] tmp = new byte[Ipv6Utils.INET6_LENGTH];
        System.arraycopy(array, startOffset, tmp, 0, size);
        return ipv6PrefixFor(tmp, mask);
    }

    private static Ipv6Prefix newIpv6Prefix(final String addr, final int mask) {
        checkArgument(mask >= 0 && mask <= 128, "Invalid mask %s", mask);
        return P6_FACTORY.newInstance(addr + '/' + mask);
    }

    public static @NonNull Entry<Ipv6AddressNoZone, Integer> splitIpv6Prefix(final @NonNull Ipv6Prefix prefix) {
        return splitPrefix(V6NZ_FACTORY, prefix.getValue());
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

    public static byte @NonNull[] ipv6PrefixToBytes(final @NonNull Ipv6Prefix prefix) {
        final var str = prefix.getValue();
        final byte[] bytes = new byte[Ipv6Utils.INET6_LENGTH + 1];
        final int slash = str.lastIndexOf('/');
        Ipv6Utils.fillIpv6Bytes(bytes, str, slash);
        bytes[Ipv6Utils.INET6_LENGTH] = (byte)Integer.parseInt(str.substring(slash + 1), 10);
        return bytes;
    }

    private static @NonNull String addressStringV4(final InetAddress addr) {
        requireAddress(addr);
        checkArgument(addr instanceof Inet4Address, "Address has to be an Inet4Address");
        return addr.getHostAddress();
    }

    private static String addressStringV6(final byte @NonNull[] bytes) {
        checkArgument(bytes.length == Ipv6Utils.INET6_LENGTH, "IPv6 address length is 16 bytes");

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
        Ipv4Utils.appendIpv4String(sb, bytes);
        return sb.append("/32").toString();
    }

    private static String prefixStringV4(final byte @NonNull[] bytes, final int mask) {
        checkArgument(mask >= 0 && mask <= 32, "Invalid mask %s", mask);

        final StringBuilder sb = new StringBuilder(18);
        Ipv4Utils.appendIpv4String(sb, bytes);
        return sb.append('/').append(mask).toString();
    }

    private static @NonNull Ipv4Prefix v4PrefixForShort(final byte @NonNull[] array, final int startOffset,
            final int size, final int mask) {
        if (startOffset == 0 && size == Ipv4Utils.INET4_LENGTH && array.length == Ipv4Utils.INET4_LENGTH) {
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
        for (int i = size; i < Ipv4Utils.INET4_LENGTH; i++) {
            sb.append(".0");
        }

        // Add mask
        checkArgument(mask > 0 && mask <= 32, "Invalid mask %s", mask);
        sb.append('/').append(mask);

        return P4_FACTORY.newInstance(sb.toString());
    }

    private static @NonNull Ipv6Address coerceIpv6Address(final @NonNull IpAddress addr) {
        final var ret = addr.getIpv6Address();
        checkArgument(ret != null, "Address %s is neither IPv4 nor IPv6", addr);
        return ret;
    }

    private static @NonNull Ipv6AddressNoZone coerceIpv6AddressNoZone(final @NonNull IpAddressNoZone addr) {
        final var ret = addr.getIpv6AddressNoZone();
        checkArgument(ret != null, "Address %s is neither IPv4 nor IPv6", addr);
        return ret;
    }
}
