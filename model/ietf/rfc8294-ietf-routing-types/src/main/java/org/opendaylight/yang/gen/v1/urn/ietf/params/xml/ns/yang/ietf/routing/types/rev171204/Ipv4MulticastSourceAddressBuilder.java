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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204.Ipv4MulticastSourceAddress.Enumeration;

/**
 * Builder for {@link IpMulticastGroupAddress} instances.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class Ipv4MulticastSourceAddressBuilder {
    private static final Map<Enumeration, Ipv4MulticastSourceAddress> ENUMERATED = Arrays.stream(Enumeration.values())
            .collect(Maps.toImmutableEnumMap(Verify::verifyNotNull, Ipv4MulticastSourceAddress::new));

    private Ipv4MulticastSourceAddressBuilder() {
        //Exists only to defeat instantiation.
    }

    public static Ipv4MulticastSourceAddress getDefaultInstance(final String defaultValue) {
        return Enumeration.forName(defaultValue).map(ENUMERATED::get)
                .orElse(new Ipv4MulticastSourceAddress(new Ipv4Address(defaultValue)));
    }

    public static Ipv4MulticastSourceAddress forEnumeration(final Enumeration enumeration) {
        return verifyNotNull(ENUMERATED.get(requireNonNull(enumeration)));
    }
}
