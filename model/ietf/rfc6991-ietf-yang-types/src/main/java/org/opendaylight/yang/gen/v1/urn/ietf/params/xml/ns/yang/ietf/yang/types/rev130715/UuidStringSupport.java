/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import com.google.common.annotations.Beta;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueSupport} class for {@link UuidString}.
 */
@Beta
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
public final class UuidStringSupport extends AbstractCanonicalValueSupport<UuidString> {
    private static final UuidStringSupport INSTANCE = new UuidStringSupport();

    public UuidStringSupport() {
        super(UuidString.class);
    }

    public static UuidStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Either<UuidString, CanonicalValueViolation> fromString(final String str) {
        // TODO: we should be able to do something better here
        return Either.ofFirst(UuidString.valueOf(UUID.fromString(str)));
    }
}
