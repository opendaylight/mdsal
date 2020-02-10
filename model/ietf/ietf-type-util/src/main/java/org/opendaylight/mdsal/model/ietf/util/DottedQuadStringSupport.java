/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.InetAddress;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueSupport} class for {@link DottedQuadString}.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
public final class DottedQuadStringSupport extends AbstractCanonicalValueSupport<DottedQuadString> {
    private static final DottedQuadStringSupport INSTANCE = new DottedQuadStringSupport();

    public DottedQuadStringSupport() {
        super(DottedQuadString.class);
    }

    public static DottedQuadStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Variant<DottedQuadString, CanonicalValueViolation> fromString(final String str) {
        // FIXME: we need more efficient parsing
        final InetAddress address = InetAddresses.forString(str);
        checkArgument(address instanceof Inet4Address, "Value \"%s\" is not a valid dotted-quad", str);
        return Variant.ofFirst(DottedQuadString.valueOf((Inet4Address) address));
    }
}
