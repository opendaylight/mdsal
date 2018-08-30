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
 * Builder for {@link IpPrefix} instances.
 */
public class IpPrefixBuilder {
    private static final Pattern IPV4_PATTERN;

    static {
        verify(Ipv4Prefix.PATTERN_CONSTANTS.size() == 1);
        IPV4_PATTERN = Pattern.compile(Ipv4Prefix.PATTERN_CONSTANTS.get(0));
    }

    private IpPrefixBuilder() {

    }

    public static IpPrefix getDefaultInstance(final String defaultValue) {
        return IPV4_PATTERN.matcher(defaultValue).matches() ? new IpPrefix(new Ipv4Prefix(defaultValue))
                : new IpPrefix(new Ipv6Prefix(defaultValue));
    }
}
