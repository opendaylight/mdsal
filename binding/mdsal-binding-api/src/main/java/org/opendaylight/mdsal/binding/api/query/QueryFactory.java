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
import org.opendaylight.yangtools.binding.InstanceIdentifier;

/**
 * Primary entry point to creating {@link QueryExpression} instances.
 */
@Beta
public interface QueryFactory {
    /**
     * Create a new {@link DescendantQueryBuilder} for a specified root path. Root path must be a non-wildcard
     * InstanceIdentifier in general sense. If the target type represents a list, the last path argument may be a
     * wildcard, in which case the path is interpreted to search the specified list. Inner path elements have to be
     * always non-wildcarded.
     *
     * @param <T> Target object type
     * @param rootPath Subtree root
     * @return a subtree query instance
     * @throws IllegalArgumentException if rootPath is incorrect
     * @throws NullPointerException if rootPath is null
     */
    <T extends DataObject> @NonNull DescendantQueryBuilder<T> querySubtree(InstanceIdentifier<T> rootPath);
}
