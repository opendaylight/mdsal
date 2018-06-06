/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.HexFormat.fromHexDigit;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueSupport} class for {@link MacAddressString}.
 */
@Beta
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
public final class MacAddressStringSupport extends AbstractCanonicalValueSupport<MacAddressString> {
    private static final MacAddressStringSupport INSTANCE = new MacAddressStringSupport();

    public MacAddressStringSupport() {
        super(MacAddressString.class);
    }

    public static MacAddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Either<MacAddressString, CanonicalValueViolation> fromCanonicalString(final String str) {
        checkArgument(str.length() == 17, "Malformed string \"%s\"", str);
        return Either.ofFirst(new MacAddressString(fromHexDigit(str.charAt(0)) << 28 | fromHexDigit(str.charAt(1)) << 24
            | fromHexDigit(str.charAt(3)) << 20 | fromHexDigit(str.charAt(4)) << 16 | fromHexDigit(str.charAt(6)) << 12
            | fromHexDigit(str.charAt(7)) << 8 | fromHexDigit(str.charAt(9)) << 4 | fromHexDigit(str.charAt(10)),
            (short) (fromHexDigit(str.charAt(12)) << 12 | fromHexDigit(str.charAt(13)) << 8
                | fromHexDigit(str.charAt(15)) << 4 | fromHexDigit(str.charAt(16)))));
    }

    @Override
    public Either<MacAddressString, CanonicalValueViolation> fromString(final String str) {
        checkArgument(str.length() == 17, "Malformed string \"%s\"", str);
        for (int i = 2; i < 17; i += 3) {
            checkColon(str, i);
        }
        return fromCanonicalString(str);
    }

    private static void checkColon(final String str, final int offset) {
        final char ch = str.charAt(offset);
        checkArgument(ch == ':', "Invalid character '%s' at offset %s", ch, offset);
    }
}
