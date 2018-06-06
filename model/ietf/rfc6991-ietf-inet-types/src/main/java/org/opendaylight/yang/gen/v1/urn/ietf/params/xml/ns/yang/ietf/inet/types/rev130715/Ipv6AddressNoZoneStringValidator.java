/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueValidator;
import org.opendaylight.yangtools.yang.common.CanonicalValueValidator;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueValidator} class for {@link Ipv6AddressNoZoneString}.
 */
@Beta
@MetaInfServices(value = CanonicalValueValidator.class)
@NonNullByDefault
public final class Ipv6AddressNoZoneStringValidator
        extends AbstractCanonicalValueValidator<Ipv6AddressString, Ipv6AddressNoZoneString> {

    private static final Ipv6AddressNoZoneStringValidator INSTANCE = new Ipv6AddressNoZoneStringValidator();

    public Ipv6AddressNoZoneStringValidator() {
        super(Ipv6AddressStringSupport.getInstance(), Ipv6AddressNoZoneString.class);
    }

    public static Ipv6AddressNoZoneStringValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected Either<Ipv6AddressString, CanonicalValueViolation> validate(final Ipv6AddressString value) {
        return Either.ofFirst(new Ipv6AddressNoZoneString(value));
    }

    @Override
    protected Either<Ipv6AddressString, CanonicalValueViolation> validate(final Ipv6AddressString value,
            final String canonicalString) {
        return validate(value);
    }
}
