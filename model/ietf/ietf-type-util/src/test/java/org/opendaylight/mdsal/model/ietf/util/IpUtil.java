/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import java.util.regex.Pattern;
import javax.annotation.RegEx;

final class IpUtil extends AbstractIetfInetUtil<IpClass, IpClass, IpClass, IpClass, IpClass, IpClass> {

    IpUtil() {
        super(IpClass.class, IpClass.class, IpClass.class, IpClass.class);
    }

    @Override
    protected IpClass ipv4Address(final IpClass addr) {
        return addr;
    }

    @Override
    protected IpClass ipv6Address(final IpClass addr) {
        return addr;
    }

    @Override
    protected IpClass ipv4Prefix(IpClass addr) {
        return addr;
    }

    @Override
    protected IpClass ipv6Prefix(IpClass addr) {
        return addr;
    }

    @Override
    protected String ipv4AddressString(final IpClass addr) {
        return addr._value;
    }

    @Override
    protected String ipv6AddressString(final IpClass addr) {
        return addr._value;
    }

    @Override
    protected String ipv4PrefixString(final IpClass prefix) {
        return prefix._value;
    }

    @Override
    protected String ipv6PrefixString(final IpClass prefix) {
        return prefix._value;
    }

    @RegEx
    private static final String IP_V4_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";
    private static final Pattern IP_V4_PATTERN = Pattern.compile(IP_V4_REGEX);

    @Override
    protected IpClass maybeIpv4Address(IpClass addr) {
        return IP_V4_PATTERN.matcher(addr._value).matches() ? addr : null;
    }

    @Override
    protected IpClass maybeIpv6Address(IpClass addr) {
        return addr._value.contains(":") ? addr : null;
    }
}
