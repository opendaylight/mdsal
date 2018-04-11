/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.model.ietf.util.AbstractIetfInetUtil;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 */
@Beta
public final class IetfInetUtil extends AbstractIetfInetUtil<Ipv4Address, Ipv4AddressNoZone, Ipv4Prefix, Ipv6Address,
        Ipv6AddressNoZone, Ipv6Prefix, IpAddress, IpAddressNoZone, IpPrefix> {
    public static final IetfInetUtil INSTANCE = new IetfInetUtil();

    private IetfInetUtil() {
        super(Ipv4AddressNoZone.class, Ipv4Prefix.class, Ipv6AddressNoZone.class, Ipv6Prefix.class);
    }

    @Nonnull public static InetAddress inetAddressFor(@Nonnull final IpAddressNoZone addr) {
        final Ipv4AddressNoZone v4 = addr.getIpv4AddressNoZone();
        if (v4 != null) {
            return inet4AddressFor(v4);
        }
        final Ipv6AddressNoZone v6 = addr.getIpv6AddressNoZone();
        Preconditions.checkArgument(v6 != null, "Address %s is neither IPv4 nor IPv6", addr);
        return inet6AddressFor(v6);
    }

    @Nonnull public static Inet4Address inet4AddressFor(@Nonnull final Ipv4AddressNoZone addr) {
        try {
            return (Inet4Address) InetAddress.getByAddress(ipv4AddressBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    @Nonnull public static Inet6Address inet6AddressFor(@Nonnull final Ipv6AddressNoZone addr) {
        try {
            return (Inet6Address) InetAddress.getByAddress(ipv6AddressBytes(addr));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address " + addr, e);
        }
    }

    @Nonnull public static byte[] ipv4AddressBytes(@Nonnull final Ipv4AddressNoZone addr) {
        final String str = addr.getValue();
        return ipv4AddressBytes(str, str.length());
    }

    @Nonnull public static byte[] ipv6AddressBytes(@Nonnull final Ipv6AddressNoZone addr) {
        final String str = addr.getValue();
        return ipv6AddressBytes(str, str.length());
    }

    @Override
    protected IpAddress ipv4Address(final Ipv4Address addr) {
        return new IpAddress(addr);
    }

    @Override
    protected IpAddressNoZone ipv4AddressNoZone(final Ipv4AddressNoZone addr) {
        return new IpAddressNoZone(addr);
    }

    @Override
    protected IpAddress ipv6Address(final Ipv6Address addr) {
        return new IpAddress(addr);
    }

    @Override
    protected IpAddressNoZone ipv6AddressNoZone(final Ipv6AddressNoZone addr) {
        return new IpAddressNoZone(addr);
    }

    @Override
    protected IpPrefix ipv4Prefix(final Ipv4Prefix addr) {
        return new IpPrefix(addr);
    }

    @Override
    protected IpPrefix ipv6Prefix(final Ipv6Prefix addr) {
        return new IpPrefix(addr);
    }

    @Override
    protected String ipv4AddressString(final Ipv4Address addr) {
        return addr.getValue();
    }

    @Override
    protected String ipv6AddressString(final Ipv6Address addr) {
        return addr.getValue();
    }

    @Override
    protected String ipv4PrefixString(final Ipv4Prefix prefix) {
        return prefix.getValue();
    }

    @Override
    protected String ipv6PrefixString(final Ipv6Prefix prefix) {
        return prefix.getValue();
    }

    @Override
    protected Ipv4Address maybeIpv4Address(final IpAddress addr) {
        return addr.getIpv4Address();
    }

    @Override
    protected Ipv6Address maybeIpv6Address(final IpAddress addr) {
        return addr.getIpv6Address();
    }
}
