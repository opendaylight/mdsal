/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Result of executing a {@link QueryExpression}. It is composed of one or more result values, which can be accessed via
 * {@link #spliterator()}, {@link #stream()} and {@link #getValues()} methods.
 *
 * @param <T> Result object type
 */
@Beta
public interface QueryResult<T extends DataObject> {
    /**
     * Returns a spliterator over values of the result.
     *
     * @return Returns the a spliterator which visits query results.
     */
    // TODO: @throws IllegalStateException if values have been already been consumed?
    // FIXME: we really may want to wrap each entry in a CheckedValue, so that we can communicate fetch problems
    @NonNull Spliterator<? extends T> spliterator();

    /**
     * Returns a sequential {@link Stream} of values from the result.
     *
     * @return A stream of non-null values.
     */
    default @NonNull Stream<? extends T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a parallel {@link Stream} of values from the result.
     *
     * @return A stream of non-null values.
     */
    default @NonNull Stream<? extends T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    default @NonNull List<? extends T> getValues() {
        return stream().collect(Collectors.toUnmodifiableList());
    }
}
