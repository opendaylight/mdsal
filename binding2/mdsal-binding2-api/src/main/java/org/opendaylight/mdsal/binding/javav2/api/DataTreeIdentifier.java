/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;

/**
 * A unique identifier for a particular subtree. It's composed of the logical
 * data store type and the instance identifier of the root node.
 */
@Beta
public final class DataTreeIdentifier<T extends TreeNode> implements Immutable,
        Path<DataTreeIdentifier<?>>, Serializable {

    private static final long serialVersionUID = 1L;
    private final InstanceIdentifier<T> rootIdentifier;
    private final LogicalDatastoreType datastoreType;

    private DataTreeIdentifier(final LogicalDatastoreType datastoreType, final InstanceIdentifier<T> rootIdentifier) {
        this.datastoreType = requireNonNull(datastoreType);
        this.rootIdentifier = requireNonNull(rootIdentifier);
    }

    public static <T extends TreeNode> DataTreeIdentifier<T> create(final LogicalDatastoreType datastoreType,
        final InstanceIdentifier<T> rootIdentifier) {
        return new DataTreeIdentifier<>(datastoreType, rootIdentifier);
    }

    /**
     * Return the logical data store type.
     *
     * @return Logical data store type. Guaranteed to be non-null.
     */
    @Nonnull
    public LogicalDatastoreType getDatastoreType() {
        return datastoreType;
    }

    /**
     * Return the {@link InstanceIdentifier} of the root node.
     *
     * @return Instance identifier corresponding to the root node.
     */
    @Nonnull
    public InstanceIdentifier<T> getRootIdentifier() {
        return rootIdentifier;
    }

    /**
     * Checks whether this identifier contains some other.
     * @param other Other path, may not be null.
     * @return true/false
     */
    @Override
    public boolean contains(@Nonnull final DataTreeIdentifier<?> other) {
        return datastoreType == other.datastoreType && rootIdentifier.contains(other.rootIdentifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DataTreeIdentifier)) {
            return false;
        }
        final DataTreeIdentifier<?> other = (DataTreeIdentifier<?>) obj;
        if (datastoreType != other.datastoreType) {
            return false;
        }
        return rootIdentifier.equals(other.rootIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootIdentifier, datastoreType);
    }
}
