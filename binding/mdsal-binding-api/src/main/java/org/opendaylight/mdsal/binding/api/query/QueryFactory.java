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
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.EntryObject;

/**
 * Primary entry point to creating {@link QueryExpression} instances.
 */
@Beta
public interface QueryFactory {
    /**
     * Create a new {@link DescendantQueryBuilder} for a specified root path. Root path must be a non-wildcard
     * InstanceIdentifier in general sense.
     *
     * @param <T> Target object type
     * @param rootPath Subtree root
     * @return a subtree query instance
     * @throws IllegalArgumentException if rootPath is incorrect
     * @throws NullPointerException if rootPath is null
     */
    <T extends DataObject> @NonNull DescendantQueryBuilder<T> querySubtree(DataObjectIdentifier<T> rootPath);

    /**
     * Create a new {@link DescendantQueryBuilder} searching all in the specified list of a parent.
     *
     * @param <P> parent object type
     * @param <T> Target object type
     * @param parentPath parent path
     * @param list the list to search
     * @return a subtree query instance
     * @throws IllegalArgumentException if rootPath is incorrect
     * @throws NullPointerException if rootPath is null
     */
    <P extends DataObject, T extends EntryObject<? super P, T, ?>> @NonNull DescendantQueryBuilder<T> querySubtree(
        DataObjectIdentifier<P> parentPath, Class<T> list);

    /**
     * Create a new {@link DescendantQueryBuilder} searching all in the specified top-level list.
     *
     * @param <T> Target object type
     * @param list the list to search
     * @return a subtree query instance
     * @throws IllegalArgumentException if rootPath is incorrect
     * @throws NullPointerException if rootPath is null
     */
    <T extends EntryObject<? extends DataRoot<?>, T, ?>> @NonNull DescendantQueryBuilder<T> querySubtree(Class<T> list);
}
