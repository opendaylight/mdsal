/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import java.util.regex.Pattern;

public class IpAddressNoZoneBuilder {

    private static final Pattern IPV4_NO_ZONE_PATTERN =
        Pattern.compile("[0-9\\.]*");
    private static final Pattern IPV6_NO_ZONE_PATTERN1 =
        Pattern.compile("[0-9a-fA-F:\\.]*");

    public static IpAddressNoZone getDefaultInstance(final String defaultValue) {
        if (IPV4_NO_ZONE_PATTERN.matcher(defaultValue).matches()) {
            return new IpAddressNoZone((new Ipv4AddressNoZone(defaultValue)));
        } else if (IPV6_NO_ZONE_PATTERN1.matcher(defaultValue).matches()) {
            return new IpAddressNoZone((new Ipv6AddressNoZone(defaultValue)));
        } else {
            throw new IllegalArgumentException("Cannot create IpAddress from " + defaultValue);
        }
    }
}