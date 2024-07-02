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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastorePath;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A Binding version of {@link LogicalDatastorePath}. Uses {@link InstanceIdentifier} for path addressing in the
 * {@link DataObjectReference} sense.
 */
public final class DataTreeMatch<T extends DataObject>
        implements LogicalDatastorePath<@NonNull DataTreeMatch<?>, @NonNull InstanceIdentifier<?>> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull LogicalDatastoreType datastore;
    private final @NonNull DataObjectReference<T> path;

    private DataTreeMatch(final @NonNull LogicalDatastoreType datastore, final @NonNull DataObjectReference<T> path) {
        this.datastore = requireNonNull(datastore);
        this.path = requireNonNull(path);
    }

    /**
     * Create a new {@link DataTreeMatch} with specified datastore and path.
     *
     * @param <T> target {@link DataObject} type
     * @param datastore {@link LogicalDatastoreType} of this identifier
     * @param path {@link DataObjectReference} path of this identifier
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T extends DataObject> @NonNull DataTreeMatch<T> of(
            final @NonNull LogicalDatastoreType datastore, final @NonNull DataObjectReference<T> path) {
        return new DataTreeMatch<>(datastore, path);
    }

    /**
     * Create a new {@link DataTreeMatch} with specified datastore and path.
     *
     * @param <T> target {@link DataObject} type
     * @param datastore {@link LogicalDatastoreType} of this identifier
     * @param path {@link InstanceIdentifier} path of this identifier
     * @throws NullPointerException if any argument is {@code null}
     * @deprecated Use #{@link #of(LogicalDatastoreType, DataObjectReference)} instead
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    public static <T extends DataObject> @NonNull DataTreeMatch<T> of(
            final @NonNull LogicalDatastoreType datastore, final @NonNull InstanceIdentifier<T> path) {
        return of(datastore, path.toReference());
    }

    /**
     * Create a new {@link DataTreeMatch} with specified datastore and path.
     *
     * @param <T> target {@link DataObject} type
     * @param datastore {@link LogicalDatastoreType} of this identifier
     * @param path {@link InstanceIdentifier} path of this identifier
     * @throws NullPointerException if any argument is {@code null}
     * @deprecated Use {@link #of(LogicalDatastoreType, DataObjectReference)} instead
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    public static <T extends DataObject> @NonNull DataTreeMatch<T> create(
            final @NonNull LogicalDatastoreType datastore, final @NonNull InstanceIdentifier<T> path) {
        return of(datastore, path);
    }

    @Override
    public LogicalDatastoreType datastore() {
        return datastore;
    }

    /**
     * Return the logical data store type.
     *
     * @return Logical data store type. Guaranteed to be non-null.
     * @deprecated Use {@link #datastore()} instead
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    public @NonNull LogicalDatastoreType getDatastoreType() {
        return datastore();
    }

    @Override
    public InstanceIdentifier<T> path() {
        return path.toLegacy();
    }

    /**
     * Return the {@link InstanceIdentifier} of the root node.
     *
     * @return Instance identifier corresponding to the root node.
     * @deprecated Use {@link #path()} instead
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    public @NonNull InstanceIdentifier<T> getRootIdentifier() {
        return path();
    }

    @Override
    public int hashCode() {
        return datastore.hashCode() * 31 + path.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof DataTreeMatch<?> other
            && datastore.equals(other.datastore) && path.equals(other.path);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("datastore", datastore).add("root", path).toString();
    }

    @java.io.Serial
    Object writeReplace() {
        return new DTIv1(this);
    }
}