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

@Beta
public interface ContainsComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends ContainsMatchBuilder<T, V> {
    /**
     * Match if the leaf exists and its value is less than the specified value.
     *
     * @param value value to check against
     * @return A ValueMatch
     * @throws NullPointerException if value is null
     */
    @NonNull ValueMatch<T> lessThan(V value);

    /**
     * Match if the leaf exists and its value is less than, or equal to, the specified value.
     *
     * @param value value to check against
     * @return A ValueMatch
     * @throws NullPointerException if value is null
     */
    @NonNull ValueMatch<T> lessThanOrEqual(V value);

    /**
     * Match if the leaf exists and its value is greater than the specified value.
     *
     * @param value value to check against
     * @return A ValueMatch
     * @throws NullPointerException if value is null
     */
    @NonNull ValueMatch<T> greaterThan(V value);

    /**
     * Match if the leaf exists and its value is greater than, or equal to the specified value.
     *
     * @param value value to check against
     * @return A ValueMatch
     * @throws NullPointerException if value is null
     */
    @NonNull ValueMatch<T> greaterThanOrEqual(V value);
}
