/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Basic builder for a match of a leaf value.
 *
 * @param <T> query result type
 * @param <V> value type
 */
@Beta
public sealed interface ValueMatchBuilder<T extends DataObject, V>
        permits ComparableMatchBuilder, StringMatchBuilder, AbstractValueMatchBuilder {
    /**
     * Match any existing value.
     *
     * @return A ValueMatch
     */
    @NonNull ValueMatch<T> nonNull();

    /**
     * Match when non-existent.
     *
     * @return A ValueMatch
     */
    @NonNull ValueMatch<T> isNull();

    /**
     * Match exact value.
     *
     * @param value value to match
     * @return A ValueMatch
     * @throws NullPointerException if value is null
     */
    @NonNull ValueMatch<T> valueEquals(@NonNull V value);
}
