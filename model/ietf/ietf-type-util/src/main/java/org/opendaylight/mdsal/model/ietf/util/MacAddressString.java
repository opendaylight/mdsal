/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.appendHexByte;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.first;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.fourth;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.second;
import static org.opendaylight.mdsal.model.ietf.util.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * {@link DerivedString} representation of the {@code mac-address} type as defined by ietf-yang-types.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
@NonNullByDefault
public class MacAddressString extends DerivedString<MacAddressString> implements ByteArrayLike {
    private static final long serialVersionUID = 1L;

    private final int mostSigBits;
    private final short leastSigBits;

    MacAddressString(final int mostSigBits, final short leastBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastBits;
    }

    protected MacAddressString(final MacAddressString other) {
        this(other.mostSigBits, other.leastSigBits);
    }

    @Override
    public final byte getByteAt(final int offset) {
        switch (offset) {
            case 0:
                return first(mostSigBits);
            case 1:
                return second(mostSigBits);
            case 2:
                return third(mostSigBits);
            case 3:
                return fourth(mostSigBits);
            case 4:
                return first(leastSigBits);
            case 5:
                return second(leastSigBits);
            default:
                throw new IndexOutOfBoundsException("Invalid offset " + offset);
        }
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
        final StringBuilder sb = new StringBuilder(17);
        return appendHexByte(appendHexByte(appendHexByte(appendHexByte(appendHexByte(appendHexByte(sb,
            first(mostSigBits)).append(':'), second(mostSigBits)).append(':'), third(mostSigBits)).append(':'),
            fourth(mostSigBits)).append(':'), first(leastSigBits)).append(':'), second(leastSigBits)).toString();
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
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MacAddressString)) {
            return false;
        }
        final MacAddressString other = (MacAddressString) obj;
        return mostSigBits == other.mostSigBits && leastSigBits == other.leastSigBits;
    }
}
