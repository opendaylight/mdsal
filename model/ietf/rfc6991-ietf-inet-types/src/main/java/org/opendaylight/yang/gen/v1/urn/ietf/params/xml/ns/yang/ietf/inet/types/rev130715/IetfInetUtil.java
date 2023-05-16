/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 */
@Beta
public final class IetfInetUtil extends AbstractIetfInetUtil {
    public static final @NonNull IetfInetUtil INSTANCE = new IetfInetUtil();

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

    private IetfInetUtil() {
        // Hidden on purpose
    }

    @Beta
    public static Host hostFor(final String str) {
        final Matcher ipv4Matcher = HOST_IPV4_PATTERN.matcher(str);
        final Matcher ipv6Matcher1 = HOST_IPV6_PATTERN1.matcher(str);
        final Matcher ipv6Matcher2 = HOST_IPV6_PATTERN2.matcher(str);
        final Matcher domainMatcher = HOST_DOMAIN_PATTERN.matcher(str);
        List<String> matchers = new ArrayList<>(3);
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
        if (ipv4Matcher.matches()) {
            Ipv4Address ipv4 = new Ipv4Address(str);
            IpAddress ipAddress = new IpAddress(ipv4);
            return new Host(ipAddress);
        }
        if (ipv6Matcher1.matches() || ipv6Matcher2.matches()) {
            Ipv6Address ipv6 = new Ipv6Address(str);
            IpAddress ipAddress = new IpAddress(ipv6);
            return new Host(ipAddress);
        }
        if (domainMatcher.matches()) {
            DomainName domainName = new DomainName(str);
            return new Host(domainName);
        }
        throw new IllegalArgumentException("Cannot create Host from " + str);
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
}
