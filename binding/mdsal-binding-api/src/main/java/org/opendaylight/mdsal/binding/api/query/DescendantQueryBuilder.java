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
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;

/**
 * Intermediate Query builder stage, which allows the specification of the query result type to be built up via
 * {@link #extractChild(Class)} and {@link #extractChild(Class, Class)} methods. Once completed, use either
 * {@link #build()} to create a simple query, or {@link #matching()} to transition to specify predicates.
 *
 * @param <T> Query result type
 */
@Beta
public interface DescendantQueryBuilder<T extends DataObject> extends StructuralBuilder<QueryExpression<T>> {
    /**
     * Add a child path component to the specification of what needs to be extracted. This method, along with its
     * alternatives, can be used to specify which object type to select from the root path.
     *
     * @param <N> Container type
     * @param childClass child container class
     * @return This builder
     * @throws NullPointerException if childClass is null
     */
    <N extends ChildOf<? super T>> @NonNull DescendantQueryBuilder<N> extractChild(Class<N> childClass);

    /**
     * Add a child path component to the specification of what needs to be extracted, specifying an exact match in
     * a keyed list. This method, along with its alternatives, can be used to specify which object type to select from
     * the root path.
     *
     * @param <N> List type
     * @param <K> Key type
     * @param listKey List key
     * @return This builder
     * @throws NullPointerException if childClass is null
     */
    <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> @NonNull DescendantQueryBuilder<N> extractChild(
        Class<@NonNull N> listItem, K listKey);

    /**
     * Add a child path component to the specification of what needs to be extracted. This method, along with its
     * alternatives, can be used to specify which object type to select from the root path.
     *
     * @param <C> Case type
     * @param <N> Container type
     * @param caseClass child case class
     * @param childClass child container class
     * @return This builder
     * @throws NullPointerException if any argument is null
     */
    <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
        @NonNull DescendantQueryBuilder<N> extractChild(Class<C> caseClass, Class<N> childClass);

    /**
     * Start specifying type match predicates.
     *
     * @return A predicate match builder based on current result type
     */
    @NonNull MatchBuilderPath<T, T> matching();

    @Override
    QueryExpression<T> build();
}
