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
 * {@link CanonicalValueValidator} class for {@link Ipv4AddressNoZoneString}.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Deprecated
@Beta
@MetaInfServices(value = CanonicalValueValidator.class)
@NonNullByDefault
public final class Ipv4AddressNoZoneStringValidator
        extends AbstractCanonicalValueValidator<Ipv4AddressString, Ipv4AddressNoZoneString> {

    private static final Ipv4AddressNoZoneStringValidator INSTANCE = new Ipv4AddressNoZoneStringValidator();

    public Ipv4AddressNoZoneStringValidator() {
        super(Ipv4AddressStringSupport.getInstance(), Ipv4AddressNoZoneString.class);
    }

    public static Ipv4AddressNoZoneStringValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected Variant<Ipv4AddressString, CanonicalValueViolation> validate(final Ipv4AddressString value) {
        return Variant.ofFirst(new Ipv4AddressNoZoneString(value));
    }

    @Override
    protected Variant<Ipv4AddressString, CanonicalValueViolation> validate(final Ipv4AddressString value,
            final String canonicalString) {
        return validate(value);
    }
}
