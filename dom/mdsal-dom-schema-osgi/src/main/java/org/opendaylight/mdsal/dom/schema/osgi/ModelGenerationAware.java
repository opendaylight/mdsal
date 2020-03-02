/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi;

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.osgi.framework.Constants;

/**
 * Additional interface for exposing linear generation of the backing effective model. Implementations of this interface
 * are expected to be effectively-immutable.
 *
 * @param <S> service type
 */
@Beta
public interface ModelGenerationAware<S> extends Immutable {

    @NonNull UnsignedLong getGeneration();

    @NonNull S getService();

    /**
     * Get service ranking based on the generation. Higher generation results in a higher ranking.
     *
     * @return Ranging for use with {@link Constants#SERVICE_RANKING}
     */
    default @NonNull Integer getServiceRanking() {
        return computeServiceRanking(getGeneration().longValue());
    }

    /**
     * Calculate service ranking based on generation. Higher generation results in a higher ranking.
     *
     * @param generation generation number, treated as an unsigned long
     * @return Ranging for use with {@link Constants#SERVICE_RANKING}
     */
    static @NonNull Integer computeServiceRanking(final long generation) {
        return generation >= 0 && generation <= Integer.MAX_VALUE ? (int) generation : Integer.MAX_VALUE;
    }
}
