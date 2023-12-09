/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

/**
 * A path to a subtree within a particular datastore.
 */
@NonNullByDefault
public interface LogicalDatastorePath<T extends LogicalDatastorePath<T, P>, P extends HierarchicalIdentifier<P>>
        extends HierarchicalIdentifier<T> {
    /**
     * Return the {@link LogicalDatastoreType}.
     *
     * @return the {@link LogicalDatastoreType}
     */
    LogicalDatastoreType datastore();

    /**
     * Return the absolute path.
     *
     * @return the absolute path
     */
    P path();

    @Override
    default boolean contains(final T other) {
        return datastore() == other.datastore() && path().contains(other.path());
    }
}
