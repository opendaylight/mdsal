/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Primary entry point to creating {@link QueryExpression} instances.
 */
public interface QueryFactory {
    /**
     * Create a new {@link DescendantQueryBuilder} for a specified {@link DataObjectIdentifier}.
     *
     * @param <T> Target object type
     * @param identifier Subtree root
     * @return a subtree query instance
     * @throws IllegalArgumentException if rootPath is incorrect
     * @throws NullPointerException if rootPath is {@code null}
     */
    <T extends DataObject> @NonNull InstanceQueryBuilder<T> queryInstance(@NonNull DataObjectIdentifier<T> identifier);

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
    @Deprecated(since = "15.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull DescendantQueryBuilder<T> querySubtree(
            final InstanceIdentifier<T> rootPath) {
        if (rootPath.isExact()) {
            return queryInstance(rootPath.toIdentifier());
        }

        // Slow path, ensuring the spec is decomposed as needed
        final var steps = new ArrayList<ExactDataObjectStep<?>>();
        final var it = rootPath.steps().iterator();
        while (it.hasNext()) {
            switch (it.next()) {
                case ExactDataObjectStep<?> step -> steps.add(step);
                case KeylessStep<?> step -> {
                    final var identifier = DataObjectIdentifier.ofUnsafeSteps(steps);
                    if (it.hasNext()) {
                        throw new IllegalArgumentException("Invalid path " + rootPath + ": cannot follow " + identifier
                            + " with " + it.next());
                    }

                    final var query = queryInstance(identifier);
                    @SuppressWarnings("unchecked")
                    final var casted = (DescendantQueryBuilder<T>) query.queryAllEntries(step.type());
                    return casted;
                }
            }
        }

        return (DescendantQueryBuilder<T>) queryInstance(DataObjectIdentifier.ofUnsafeSteps(steps));
    }
}
