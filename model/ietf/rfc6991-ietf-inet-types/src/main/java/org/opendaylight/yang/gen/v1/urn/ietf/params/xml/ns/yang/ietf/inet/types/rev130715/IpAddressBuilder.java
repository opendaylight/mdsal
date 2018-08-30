/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static com.google.common.base.Verify.verify;

import java.util.regex.Pattern;

/**
 * Builder for {@link IpAddress} instances.
 */
public class IpAddressBuilder {
    private static final Pattern IPV4_PATTERN;

    static {
        verify(Ipv4Address.PATTERN_CONSTANTS.size() == 1);
        IPV4_PATTERN = Pattern.compile(Ipv4Address.PATTERN_CONSTANTS.get(0));
    }

    private IpAddressBuilder() {

    }

    public static IpAddress getDefaultInstance(final String defaultValue) {
        return IPV4_PATTERN.matcher(defaultValue).matches() ? new IpAddress(new Ipv4Address(defaultValue))
                : new IpAddress(new Ipv6Address(defaultValue));
    }
}
