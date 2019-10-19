/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueValidator;
import org.opendaylight.yangtools.yang.common.CanonicalValueValidator;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueValidator} class for {@link Ipv6AddressNoZoneString}.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
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
    protected Variant<Ipv6AddressString, CanonicalValueViolation> validate(final Ipv6AddressString value) {
        return Variant.ofFirst(new Ipv6AddressNoZoneString(value));
    }

    @Override
    protected Variant<Ipv6AddressString, CanonicalValueViolation> validate(final Ipv6AddressString value,
            final String canonicalString) {
        return validate(value);
    }
}
