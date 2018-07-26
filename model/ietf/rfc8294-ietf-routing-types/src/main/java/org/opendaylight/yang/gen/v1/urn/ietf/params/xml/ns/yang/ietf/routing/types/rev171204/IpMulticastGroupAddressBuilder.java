/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

/**
 * Builder for {@link IpMulticastGroupAddress} instances.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class IpMulticastGroupAddressBuilder {
    private static final Pattern IPV4_PATTERN;

    static {
        verify(Ipv4Address.PATTERN_CONSTANTS.size() == 1);
        IPV4_PATTERN = Pattern.compile(Ipv4MulticastGroupAddress.PATTERN_CONSTANTS.get(0));
    }

    private IpMulticastGroupAddressBuilder() {
        //Exists only to defeat instantiation.
    }

    public static IpMulticastGroupAddress getDefaultInstance(final String defaultValue) {
        return IPV4_PATTERN.matcher(defaultValue).matches()
                ? new IpMulticastGroupAddress(new Ipv4MulticastGroupAddress(defaultValue))
                        : new IpMulticastGroupAddress(new Ipv6MulticastGroupAddress(defaultValue));
    }
}
