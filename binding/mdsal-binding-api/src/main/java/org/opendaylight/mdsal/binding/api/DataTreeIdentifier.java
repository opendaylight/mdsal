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

/**
 * A Binding version of {@link LogicalDatastorePath}. Uses {@link DataObjectReference} for path addressing.
 */
// FIXME: DataObjectReference has a DataObjectIdentifier specialization, which makes 'Identifier' part of this class
//        name a bit confusing. Consider a better name -- like DataTreeMatch?
public final class DataTreeIdentifier<T extends DataObject>
        implements LogicalDatastorePath<@NonNull DataTreeIdentifier<?>, @NonNull DataObjectReference<?>> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull LogicalDatastoreType datastore;
    private final @NonNull DataObjectReference<T> path;

    private DataTreeIdentifier(final @NonNull LogicalDatastoreType datastore,
            final @NonNull DataObjectReference<T> path) {
        this.datastore = requireNonNull(datastore);
        this.path = requireNonNull(path);
    }

    /**
     * Create a new {@link DataTreeIdentifier} with specified datastore and path.
     *
     * @param <T> target {@link DataObject} type
     * @param datastore {@link LogicalDatastoreType} of this identifier
     * @param path {@link DataObjectReference} path of this identifier
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T extends DataObject> @NonNull DataTreeIdentifier<T> of(
            final @NonNull LogicalDatastoreType datastore, final @NonNull DataObjectReference<T> path) {
        return new DataTreeIdentifier<>(datastore, path);
    }

    @Override
    public LogicalDatastoreType datastore() {
        return datastore;
    }

    @Override
    public DataObjectReference<T> path() {
        return path;
    }

    @Override
    public boolean contains(final DataTreeIdentifier<?> other) {
        if (datastore != other.datastore) {
            return false;
        }
        final var oit = other.path.steps().iterator();
        for (var step : path.steps()) {
            if (!oit.hasNext()) {
                return false;
            }
            if (!step.equals(oit.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return datastore.hashCode() * 31 + path.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof DataTreeIdentifier<?> other
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
