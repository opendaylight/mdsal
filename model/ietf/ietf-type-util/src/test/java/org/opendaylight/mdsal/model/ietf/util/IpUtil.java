/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.RegEx;

final class IpUtil extends AbstractIetfInetUtil<IpClass, IpClass, IpClass, IpClass, IpClass, IpClass, IpClass,
        IpClass, IpClass> {

    @RegEx
    private static final String IP_V4_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";
    private static final Pattern IP_V4_PATTERN = Pattern.compile(IP_V4_REGEX);

    IpUtil() {
        super(IpClass.class, IpClass.class, IpClass.class, IpClass.class);
    }

    @Override
    @Nonnull
    protected IpClass ipv4Address(final IpClass addr) {
        return addr;
    }

    @Override
    protected IpClass ipv4AddressNoZone(final IpClass addr) {
        return addr;
    }

    @Override
    @Nonnull
    protected IpClass ipv6Address(final IpClass addr) {
        return addr;
    }

    @Override
    protected IpClass ipv6AddressNoZone(final IpClass addr) {
        return addr;
    }

    @Override
    @Nonnull
    protected IpClass ipv4Prefix(final IpClass addr) {
        return addr;
    }

    @Override
    @Nonnull
    protected IpClass ipv6Prefix(final IpClass addr) {
        return addr;
    }

    @Override
    @Nonnull
    protected String ipv4AddressString(final IpClass addr) {
        return addr.getValue();
    }

    @Override
    @Nonnull
    protected String ipv6AddressString(final IpClass addr) {
        return addr.getValue();
    }

    @Override
    @Nonnull
    protected String ipv4PrefixString(final IpClass prefix) {
        return prefix.getValue();
    }

    @Override
    @Nonnull
    protected String ipv6PrefixString(final IpClass prefix) {
        return prefix.getValue();
    }

    @Override
    protected IpClass maybeIpv4Address(final IpClass addr) {
        return IP_V4_PATTERN.matcher(addr.getValue()).matches() ? addr : null;
    }

    @Override
    protected IpClass maybeIpv6Address(final IpClass addr) {
        return addr.getValue().indexOf(':') != -1 ? addr : null;
    }
}