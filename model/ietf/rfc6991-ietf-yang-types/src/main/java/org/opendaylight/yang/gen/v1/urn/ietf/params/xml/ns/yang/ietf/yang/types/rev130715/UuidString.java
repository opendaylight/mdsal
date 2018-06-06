/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.eighth;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.fifth;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.first;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.fourth;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.second;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.seventh;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.sixth;
import static org.opendaylight.mdsal.model.ietf.type.util.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * {@link DerivedString} representation of the {@code uuid} type as defined by ietf-yang-types.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
@NonNullByDefault
public class UuidString extends DerivedString<UuidString> {
    private static final long serialVersionUID = 1L;

    private final long mostBits;
    private final long leastBits;

    protected UuidString(final long mostBits, final long leastBits) {
        this.mostBits = mostBits;
        this.leastBits = leastBits;
    }

    protected UuidString(final UuidString other) {
        this(other.mostBits, other.leastBits);
    }

    public final long getMostBits() {
        return mostBits;
    }

    public final long getLeastBits() {
        return leastBits;
    }

    public static UuidString valueOf(final long mostBits, final long leastBits) {
        return new UuidString(mostBits, leastBits);
    }

    public static UuidString valueOf(final byte[] bytes) {
        checkArgument(bytes.length == 16, "Byte array %s has incorrect length", bytes);
        return valueOf(lshift(bytes[0], 56) | lshift(bytes[1], 48) | lshift(bytes[2], 40) | lshift(bytes[3], 32)
            | bytes[4] << 24 | bytes[5] << 16 | bytes[6] << 8 | bytes[7],
            lshift(bytes[8], 56) | lshift(bytes[9], 48) | lshift(bytes[10], 40) | lshift(bytes[11], 32)
            | bytes[12] << 24 | bytes[13] << 16 | bytes[14] << 8 | bytes[15]);
    }

    public static UuidString valueOf(final UUID uuid) {
        return valueOf(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public final byte[] toByteArray() {
        return new byte[] {
                first(mostBits), second(mostBits), third(mostBits), fourth(mostBits), fifth(mostBits), sixth(mostBits),
                seventh(mostBits), eighth(mostBits), first(leastBits), second(leastBits), third(leastBits),
                fourth(leastBits), fifth(leastBits), sixth(leastBits), seventh(leastBits), eighth(leastBits)
        };
    }

    public final UUID toUUID() {
        return new UUID(mostBits, leastBits);
    }

    @Override
    public final String toCanonicalString() {
        return toUUID().toString();
    }

    @Override
    public final UuidStringSupport support() {
        return UuidStringSupport.getInstance();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final UuidString o) {
        final int cmp = Long.compareUnsigned(mostBits, o.mostBits);
        return cmp != 0 ? cmp : Long.compareUnsigned(leastBits, leastBits);
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(mostBits) ^ Long.hashCode(leastBits);
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final UuidString other)) {
            return false;
        }

        return mostBits == other.mostBits && leastBits == other.leastBits;
    }

    private static long lshift(final byte value, final int shift) {
        return (value & 0xFFL) << shift;
    }
}
