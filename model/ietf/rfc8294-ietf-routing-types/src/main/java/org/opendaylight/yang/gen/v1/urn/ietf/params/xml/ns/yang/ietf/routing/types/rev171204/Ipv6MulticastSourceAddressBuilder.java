/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204.Ipv6MulticastSourceAddress.Enumeration;

/**
 * Builder for {@link IpMulticastGroupAddress} instances.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class Ipv6MulticastSourceAddressBuilder {
    private static final Map<Enumeration, Ipv6MulticastSourceAddress> ENUMERATED = Arrays.stream(Enumeration.values())
            .collect(Maps.toImmutableEnumMap(Verify::verifyNotNull, Ipv6MulticastSourceAddress::new));

    private Ipv6MulticastSourceAddressBuilder() {

    }

    public static Ipv6MulticastSourceAddress getDefaultInstance(final String defaultValue) {
        return Enumeration.forName(defaultValue).map(ENUMERATED::get)
                .orElse(new Ipv6MulticastSourceAddress(new Ipv6Address(defaultValue)));
    }

    public static Ipv6MulticastSourceAddress forEnumeration(final Enumeration enumeration) {
        return verifyNotNull(ENUMERATED.get(requireNonNull(enumeration)));
    }
}
