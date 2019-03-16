/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ethertypes.rev190304;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ethertypes.rev190304.Ethertype.Enumeration;

public final class EthertypeBuilder {
    private static final CharMatcher DIGITS = CharMatcher.inRange('0', '9');
    private static final ImmutableMap<Enumeration, Ethertype> ENUM_ETHERTYPES;
    private static final LoadingCache<Integer, Ethertype> UINT_ETHERTYPES = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<Integer, Ethertype>() {
                @Override
                public Ethertype load(final Integer key) {
                    return new Ethertype(key);
                }
            });

    static {
        final Builder<Enumeration, Ethertype> builder = ImmutableMap.builder();
        for (Enumeration value : Enumeration.values()) {
            builder.put(value, new Ethertype(value));
        }
        ENUM_ETHERTYPES = Maps.immutableEnumMap(builder.build());
    }

    private EthertypeBuilder() {
        //Exists only to defeat instantiation.
    }

    // TODO: the canonical representation is undefined. We may consider turning integers into enum values, but at this
    //       point it would confuse people and would not work well with JSON/XML parser output.
    public static @NonNull Ethertype getDefaultInstance(final String defaultValue) {
        final int length = defaultValue.length();
        if (length > 0 && length < 6 && DIGITS.matchesAllOf(defaultValue)) {
            final int value = Integer.parseInt(defaultValue);
            if (value < 65536) {
                return UINT_ETHERTYPES.getUnchecked(value);
            }
        }

        final Optional<Enumeration> known = Enumeration.forName(defaultValue);
        checkArgument(known.isPresent(), "Unknown ethertype %s", defaultValue);
        return verifyNotNull(ENUM_ETHERTYPES.get(known.get()));
    }

    public static boolean equalEthertype(final Ethertype first, final Ethertype second) {
        return first == second || first != null && extractValue(first) == extractValue(second);
    }

    private static int extractValue(final Ethertype type) {
        final Enumeration known = type.getEnumeration();
        return known != null ? known.getIntValue() : type.getUint16();
    }
}
