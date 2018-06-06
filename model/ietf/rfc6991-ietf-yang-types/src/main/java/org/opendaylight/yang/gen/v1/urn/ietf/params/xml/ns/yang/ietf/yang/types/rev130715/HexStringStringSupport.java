/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.util.HexFormat;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 * {@link CanonicalValueSupport} class for {@link HexStringString}.
 */
@Beta
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
public final class HexStringStringSupport extends AbstractCanonicalValueSupport<HexStringString> {
    private static final HexStringStringSupport INSTANCE = new HexStringStringSupport();

    public HexStringStringSupport() {
        super(HexStringString.class);
    }

    public static HexStringStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Either<HexStringString, CanonicalValueViolation> fromCanonicalString(final String str) {
        if (str.isEmpty()) {
            return Either.ofFirst(HexStringString.empty());
        }

        final int strlen = str.length();
        final byte[] bytes = new byte[(strlen + 1) / 3];
        for (int i = 0; i < strlen; i += 3) {
            bytes[i] = (byte) (HexFormat.fromHexDigit(str.charAt(i)) << 4 | HexFormat.fromHexDigit(str.charAt(i + 1)));
        }
        return Either.ofFirst(HexStringString.valueOf(bytes));
    }

    @Override
    public Either<HexStringString, CanonicalValueViolation> fromString(final String str) {
        final int strlen = str.length();
        for (int i = 2; i < strlen; i += 3) {
            checkColon(str, i);
        }
        return fromCanonicalString(str);
    }

    private static void checkColon(final String str, final int offset) {
        final char ch = str.charAt(offset);
        checkArgument(ch == ':', "Invalid character '%s' at offset %s", ch, offset);
    }
}
