/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ethertypes.rev190304;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.CharMatcher;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ethertypes.rev190304.Ethertype.Enumeration;

/**
 * Utility methods for dealing with {@link Ethertype}.
 */
public final class EthertypeUtils {
    private static final CharMatcher DIGITS = CharMatcher.inRange('0', '9');
    private static final EnumMap<Enumeration, Ethertype> ENUM_ETHERTYPES;

    static {
        final EnumMap<Enumeration, Ethertype> map = new EnumMap<>(Enumeration.class);
        for (Enumeration value : Enumeration.values()) {
            verify(map.put(value, new Ethertype(value)) == null);
        }
        ENUM_ETHERTYPES = map;
    }

    private EthertypeUtils() {
        //Exists only to defeat instantiation.
    }

    // TODO: the canonical representation is undefined. We may consider turning integers into enum values, but at this
    //       point it would confuse people and would not work well with JSON/XML parser output.
    public static @NonNull Ethertype getDefaultInstance(final String defaultValue) {
        final int length = defaultValue.length();
        if (length > 0 && length < 6 && DIGITS.matchesAllOf(defaultValue)) {
            final int value = Integer.parseInt(defaultValue);
            if (value < 65536) {
                return new Ethertype(Uint16.valueOf(value));
            }

            // Fall through and interpret as a string
        }

        final var known = Enumeration.forName(defaultValue);
        checkArgument(known != null, "Unknown ethertype %s", defaultValue);
        return verifyNotNull(ENUM_ETHERTYPES.get(known));
    }

    /**
     * Semantically compare two {@link Ethertype}s based on their numeric value, according to
     * {@link Comparator#compare(Object, Object)} contract.
     *
     * @param first First Ethertype
     * @param second Second Ethertype
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     * @throws NullPointerException if any argument is null
     */
    public static int compareValue(final Ethertype first, final Ethertype second) {
        return Integer.compare(extractValue(first), extractValue(second));
    }

    /**
     * Determine semantic equality of two {@link Ethertype}s based on their numeric value, according to
     * {@link Objects#equals(Object, Object)} contract. This is distinct from {@link Ethertype#equals(Object)}, which
     * does not perform semantic comparison.
     *
     * @param first First Ethertype
     * @param second Second Ethertype
     * @return True if the two Ethertypes are equal, false otherwise.
     */
    public static boolean equalValue(final Ethertype first, final Ethertype second) {
        return first == second || first != null && extractValue(first) == extractValue(second);
    }

    /**
     * Determine semantic hash value of a {@link Ethertype}s based on its numeric value, according to
     * {@link Object#hashCode()} contract. This is distinct from {@link Ethertype#hashCode()}, which does not perform
     * semantic hashing. Unlike {@link Objects#hashCode(Object)}, this method does not return 0 for null objects.
     *
     * @param value Ethertype object
     * @return Specified object's semantic hashCode
     */
    public static int hashValue(final Ethertype value) {
        return value == null ? 65536 : extractValue(value);
    }

    private static int extractValue(final Ethertype type) {
        final Enumeration known = type.getEnumeration();
        return known != null ? known.getIntValue() : verifyNotNull(type.getUint16()).intValue();
    }
}
