/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueSupport} class for {@link Ipv6PrefixString}.
 */
@Beta
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
public final class Ipv6PrefixStringSupport extends AbstractCanonicalValueSupport<Ipv6PrefixString> {
    private static final Ipv6PrefixStringSupport INSTANCE = new Ipv6PrefixStringSupport();

    public Ipv6PrefixStringSupport() {
        super(Ipv6PrefixString.class);
    }

    public static Ipv6PrefixStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Either<Ipv6PrefixString, CanonicalValueViolation> fromString(final String str) {
        final int slash = StringTypeUtils.findSingleHash(str);
        final InetAddress address = InetAddresses.forString(str.substring(0, slash));
        checkArgument(address instanceof Inet6Address, "Value \"%s\" is not a valid ipv6-prefix", str);
        final int length = Integer.parseUnsignedInt(str.substring(slash + 1));
        return Either.ofFirst(Ipv6PrefixString.valueOf((Inet6Address) address, (byte)length));
    }
}
