/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.first;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.fourth;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.second;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import java.util.HexFormat;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.model.ietf.type.util.ByteArrayLike;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * {@link DerivedString} representation of the {@code mac-address} type as defined by ietf-yang-types.
 */
@Beta
@NonNullByDefault
public class MacAddressString extends DerivedString<MacAddressString> implements ByteArrayLike {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final int mostSigBits;
    private final short leastSigBits;

    MacAddressString(final int mostSigBits, final short leastBits) {
        this.mostSigBits = mostSigBits;
        leastSigBits = leastBits;
    }

    protected MacAddressString(final MacAddressString other) {
        this(other.mostSigBits, other.leastSigBits);
    }

    @Override
    public final byte getByteAt(final int offset) {
        return switch (offset) {
            case 0 -> first(mostSigBits);
            case 1 -> second(mostSigBits);
            case 2 -> third(mostSigBits);
            case 3 -> fourth(mostSigBits);
            case 4 -> first(leastSigBits);
            case 5 -> second(leastSigBits);
            default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
        };
    }

    @Override
    public final int getLength() {
        return 4;
    }

    @Override
    public final byte[] toByteArray() {
        return new byte[] { first(mostSigBits), second(mostSigBits), third(mostSigBits), fourth(mostSigBits),
                first(leastSigBits), second(leastSigBits) };
    }

    @Override
    public final MacAddressStringSupport support() {
        return MacAddressStringSupport.getInstance();
    }

    @Override
    public final String toCanonicalString() {
        return HEX_FORMAT.toHexDigits(HEX_FORMAT.toHexDigits(HEX_FORMAT.toHexDigits(HEX_FORMAT.toHexDigits(
            HEX_FORMAT.toHexDigits(HEX_FORMAT.toHexDigits(new StringBuilder(17), first(mostSigBits)).append(':'),
                second(mostSigBits)).append(':'), third(mostSigBits)).append(':'), fourth(mostSigBits)).append(':'),
            first(leastSigBits)).append(':'), second(leastSigBits)).toString();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final MacAddressString o) {
        final int cmp = Integer.compareUnsigned(mostSigBits, o.mostSigBits);
        return cmp != 0 ? cmp : Integer.compare(Short.toUnsignedInt(leastSigBits), Short.toUnsignedInt(o.leastSigBits));
    }

    @Override
    public final int hashCode() {
        return mostSigBits * 31 + leastSigBits;
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof MacAddressString other
            && mostSigBits == other.mostSigBits && leastSigBits == other.leastSigBits;
    }
}
