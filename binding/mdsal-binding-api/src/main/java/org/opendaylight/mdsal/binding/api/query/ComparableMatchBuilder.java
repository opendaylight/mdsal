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
 * Builder for a match of a leaf value which define a total ordering by implementing the {@link Comparable} interface.
 *
 * <p>
 * Note that value comparison is preconditioned on target leaf existence. If the leaf does not exist any total ordering
 * checks will not match it -- thus a non-existent leaf does not match {@link #greaterThan(Comparable)} and at the same
 * time it does not match {@link #lessThanOrEqual(Comparable)}.
 *
 * @param <T> query result type
 * @param <V> value type
 */
@Beta
public non-sealed interface ComparableMatchBuilder<T extends DataObject, V extends Comparable<V>>
        extends ValueMatchBuilder<T, V> {
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
