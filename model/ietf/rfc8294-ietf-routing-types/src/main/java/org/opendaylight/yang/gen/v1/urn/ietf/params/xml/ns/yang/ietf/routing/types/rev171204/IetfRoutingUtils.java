/*
 * Copyright (c) 2018, 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Utility methods for dealing with unions in {@code ietf-routing-types.yang}.
 */
@Beta
@NonNullByDefault
public final class IetfRoutingUtils {
    private static final ImmutableMap<Ipv4MulticastSourceAddress.Enumeration, Ipv4MulticastSourceAddress>
        ENUMERATED_IPV4_MCAST_SRC = Arrays.stream(Ipv4MulticastSourceAddress.Enumeration.values()).collect(
            Maps.toImmutableEnumMap(Verify::verifyNotNull, Ipv4MulticastSourceAddress::new));
    private static final ImmutableMap<Ipv6MulticastSourceAddress.Enumeration, Ipv6MulticastSourceAddress>
        ENUMERATED_IPV6_MCAST_SRC = Arrays.stream(Ipv6MulticastSourceAddress.Enumeration.values()).collect(
            Maps.toImmutableEnumMap(Verify::verifyNotNull, Ipv6MulticastSourceAddress::new));
    private static final ImmutableMap<TimerValueMilliseconds.Enumeration, TimerValueMilliseconds>
        ENUMERATED_TIMERVAR_MS = Arrays.stream(TimerValueMilliseconds.Enumeration.values())
        .collect(Maps.toImmutableEnumMap(Verify::verifyNotNull, TimerValueMilliseconds::new));
    private static final ImmutableMap<TimerValueSeconds16.Enumeration, TimerValueSeconds16> ENUMERATED_TIMERVAL_16 =
        Arrays.stream(TimerValueSeconds16.Enumeration.values())
            .collect(Maps.toImmutableEnumMap(Verify::verifyNotNull, TimerValueSeconds16::new));
    private static final ImmutableMap<TimerValueSeconds32.Enumeration, TimerValueSeconds32> ENUMERATED_TIMERVAL_32 =
        Arrays.stream(TimerValueSeconds32.Enumeration.values())
            .collect(Maps.toImmutableEnumMap(Verify::verifyNotNull, TimerValueSeconds32::new));

    private static final Pattern IP_MCAST_GRP_ADDR_IPV4_PATTERN;

    static {
        verify(Ipv4Address.PATTERN_CONSTANTS.size() == 1);
        IP_MCAST_GRP_ADDR_IPV4_PATTERN = Pattern.compile(Ipv4MulticastGroupAddress.PATTERN_CONSTANTS.get(0));
    }

    private IetfRoutingUtils() {
        // Hidden on purpose
    }

    public static IpMulticastGroupAddress ipMulticastGroupAddressFor(final String str) {
        return IP_MCAST_GRP_ADDR_IPV4_PATTERN.matcher(str).matches()
            ? new IpMulticastGroupAddress(new Ipv4MulticastGroupAddress(str))
                : new IpMulticastGroupAddress(new Ipv6MulticastGroupAddress(str));
    }

    public static Ipv4MulticastSourceAddress ipv4MulticastSourceAddressFor(final String str) {
        final var enumeration = Ipv4MulticastSourceAddress.Enumeration.forName(str);
        return enumeration != null ? ipv4MulticastSourceAddressFor(enumeration)
            : new Ipv4MulticastSourceAddress(new Ipv4Address(str));
    }

    public static Ipv4MulticastSourceAddress ipv4MulticastSourceAddressFor(
            final Ipv4MulticastSourceAddress.Enumeration enumeration) {
        return verifyNotNull(ENUMERATED_IPV4_MCAST_SRC.get(requireNonNull(enumeration)));
    }

    public static Ipv6MulticastSourceAddress ipv6MulticastSourceAddressFor(final String str) {
        final var enumeration = Ipv6MulticastSourceAddress.Enumeration.forName(str);
        return enumeration != null ? ipv6MulticastSourceAddressFor(enumeration)
            : new Ipv6MulticastSourceAddress(new Ipv6Address(str));
    }

    public static Ipv6MulticastSourceAddress ipv6MulticastSourceAddressFor(
            final Ipv6MulticastSourceAddress.Enumeration enumeration) {
        return verifyNotNull(ENUMERATED_IPV6_MCAST_SRC.get(requireNonNull(enumeration)));
    }

    public static TimerValueMilliseconds timerValueMillisecondsFor(final String str) {
        final var enumeration = TimerValueMilliseconds.Enumeration.forName(str);
        return enumeration != null ? timerValueMillisecondsFor(enumeration)
            : new TimerValueMilliseconds(Uint32.valueOf(str));
    }

    public static TimerValueMilliseconds timerValueMillisecondsFor(
            final TimerValueMilliseconds.Enumeration enumeration) {
        return verifyNotNull(ENUMERATED_TIMERVAR_MS.get(requireNonNull(enumeration)));
    }

    public static TimerValueSeconds16 timerValueSeconds16For(final String str) {
        final var enumeration = TimerValueSeconds16.Enumeration.forName(str);
        return enumeration != null ? timerValueSeconds16For(enumeration) : new TimerValueSeconds16(Uint16.valueOf(str));
    }

    public static TimerValueSeconds16 timerValueSeconds16For(
            final TimerValueSeconds16.Enumeration enumeration) {
        return verifyNotNull(ENUMERATED_TIMERVAL_16.get(requireNonNull(enumeration)));
    }

    public static TimerValueSeconds32 timerValueSeconds32For(final String str) {
        final var enumeration = TimerValueSeconds32.Enumeration.forName(str);
        return enumeration != null ? timerValueSeconds32For(enumeration) : new TimerValueSeconds32(Uint32.valueOf(str));
    }

    public static TimerValueSeconds32 timerValueSeconds32For(final TimerValueSeconds32.Enumeration enumeration) {
        return verifyNotNull(ENUMERATED_TIMERVAL_32.get(requireNonNull(enumeration)));
    }
}
