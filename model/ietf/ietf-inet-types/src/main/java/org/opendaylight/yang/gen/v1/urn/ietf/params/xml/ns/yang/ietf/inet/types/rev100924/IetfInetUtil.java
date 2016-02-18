/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.model.ietf.util.AbstractIetfInetUtil;

/**
 * A set of utility methods to efficiently instantiate various ietf-inet-types DTOs.
 *
 * FIXME: IPv6 addresses are not emitted in canonical format as specified by the model.
 */
@Beta
public final class IetfInetUtil extends AbstractIetfInetUtil<Ipv4Address, Ipv4Prefix, Ipv6Address, Ipv6Prefix, IpAddress> {
    public static final IetfInetUtil INSTANCE = new IetfInetUtil();

    private IetfInetUtil() {
        super(Ipv4Address.class, Ipv4Prefix.class, Ipv6Address.class, Ipv6Prefix.class);
    }

    @Override
    protected IpAddress ipv4Address(final Ipv4Address addr) {
        return new IpAddress(addr);
    }

    @Override
    protected IpAddress ipv6Address(final Ipv6Address addr) {
        return new IpAddress(addr);
    }
}
