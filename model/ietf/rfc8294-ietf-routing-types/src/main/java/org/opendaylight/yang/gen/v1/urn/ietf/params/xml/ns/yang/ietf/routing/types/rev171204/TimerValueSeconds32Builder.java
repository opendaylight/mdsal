/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev171204.TimerValueSeconds32.Enumeration;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Builder for {@link TimerValueSeconds32} instances.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class TimerValueSeconds32Builder {
    private static final ImmutableMap<Enumeration, TimerValueSeconds32> ENUMERATED = Arrays.stream(Enumeration.values())
            .collect(Maps.toImmutableEnumMap(Verify::verifyNotNull, TimerValueSeconds32::new));

    private TimerValueSeconds32Builder() {

    }

    public static TimerValueSeconds32 getDefaultInstance(final String defaultValue) {
        return Enumeration.forName(defaultValue).map(ENUMERATED::get)
                .orElse(new TimerValueSeconds32(Uint32.valueOf(defaultValue)));
    }

    public static TimerValueSeconds32 forEnumeration(final Enumeration enumeration) {
        return verifyNotNull(ENUMERATED.get(requireNonNull(enumeration)));
    }
}
