/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A unique identifier for a particular subtree. It is composed of the logical
 * data store type and the instance identifier of the root node.
 */
public final class DataTreeIdentifier<T extends DataObject> implements HierarchicalIdentifier<DataTreeIdentifier<?>> {
    @Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull InstanceIdentifier<T> rootIdentifier;
    private final @NonNull LogicalDatastoreType datastoreType;

    private DataTreeIdentifier(final @NonNull LogicalDatastoreType datastoreType,
            final @NonNull InstanceIdentifier<T> rootIdentifier) {
        this.datastoreType = requireNonNull(datastoreType);
        this.rootIdentifier = requireNonNull(rootIdentifier);
    }

    public static <T extends DataObject> @NonNull DataTreeIdentifier<T> create(
            final @NonNull LogicalDatastoreType datastoreType, final @NonNull InstanceIdentifier<T> rootIdentifier) {
        return new DataTreeIdentifier<>(datastoreType, rootIdentifier);
    }

    /**
     * Return the logical data store type.
     *
     * @return Logical data store type. Guaranteed to be non-null.
     */
    public @NonNull LogicalDatastoreType getDatastoreType() {
        return datastoreType;
    }

    /**
     * Return the {@link InstanceIdentifier} of the root node.
     *
     * @return Instance identifier corresponding to the root node.
     */
    public @NonNull InstanceIdentifier<T> getRootIdentifier() {
        return rootIdentifier;
    }

    @Override
    public boolean contains(final DataTreeIdentifier<?> other) {
        return datastoreType == other.datastoreType && rootIdentifier.contains(other.rootIdentifier);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + datastoreType.hashCode();
        result = prime * result + rootIdentifier.hashCode();
        return result;
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
    public String toString() {
        return MoreObjects.toStringHelper(this).add("datastore", datastoreType).add("root", rootIdentifier).toString();
    }
}