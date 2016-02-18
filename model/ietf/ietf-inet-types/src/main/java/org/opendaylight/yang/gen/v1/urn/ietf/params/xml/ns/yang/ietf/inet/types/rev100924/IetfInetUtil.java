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
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 */
@Beta
public final class IetfInetUtil {
    private static final Lookup LOOKUP = MethodHandles.lookup();

    private static MethodHandle valueSetter(final Class<?> clazz) {
        try {
            final Field f = clazz.getDeclaredField("_value");
            f.setAccessible(true);
            return LOOKUP.unreflectSetter(f);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    private static final Ipv4Address IPV4_ADDRESS_TEMPLATE = new Ipv4Address("0.0.0.0");
    private static final MethodHandle IPV4_ADDRESS_FIELD = valueSetter(Ipv4Address.class);
    private static final Ipv4Prefix IPV4_PREFIX_TEMPLATE = new Ipv4Prefix("0.0.0.0/0");
    private static final MethodHandle IPV4_PREFIX_FIELD = valueSetter(Ipv4Prefix.class);
    private static final Ipv6Address IPV6_ADDRESS_TEMPLATE = new Ipv6Address("::0");
    private static final MethodHandle IPV6_ADDRESS_FIELD = valueSetter(Ipv6Address.class);
    private static final Ipv6Prefix IPV6_PREFIX_TEMPLATE = new Ipv6Prefix("::0/0");
    private static final MethodHandle IPV6_PREFIX_FIELD = valueSetter(Ipv6Prefix.class);

    private IetfInetUtil() {
        throw new UnsupportedOperationException();
    }

    private static void appendIpv4String(final StringBuilder sb, final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 4, "IPv4 address length is 4 bytes");

        sb.append(Byte.toUnsignedInt(bytes[0]));
        for (int i = 1; i < 4; ++i) {
            sb.append('.');
            sb.append(Byte.toUnsignedInt(bytes[1]));
        }
    }

    private static String addressStringV4(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(15);
        appendIpv4String(sb, bytes);
        return sb.toString();
    }

    public static Ipv4Address ipv4AddressFor(final byte[] bytes) {
        final Ipv4Address ret = new Ipv4Address(IPV4_ADDRESS_TEMPLATE);
        try {
            IPV4_ADDRESS_FIELD.invokeExact(ret, addressStringV4(bytes));
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
        return ret;
    }

    private static Ipv4Prefix ipv4Prefix(final byte[] bytes, final String str) {
        final Ipv4Prefix ret = new Ipv4Prefix(IPV4_PREFIX_TEMPLATE);
        try {
            IPV4_PREFIX_FIELD.invokeExact(ret, str);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
        return ret;
    }

    private static String prefixStringV4(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(18);
        appendIpv4String(sb, bytes);
        sb.append("/32");
        return sb.toString();
    }

    public static Ipv4Prefix ipv4PrefixFor(final byte[] bytes) {
        return ipv4Prefix(bytes, prefixStringV4(bytes));
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
        return ipv4Prefix(bytes, prefixStringV4(bytes, mask));
    }
}
