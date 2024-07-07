/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastorePath;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A DOM version of {@link LogicalDatastorePath}. Uses {@link YangInstanceIdentifier} for path addressing.
 */
@NonNullByDefault
public final class DOMDataTreeIdentifier implements LogicalDatastorePath<DOMDataTreeIdentifier, YangInstanceIdentifier>,
        Comparable<DOMDataTreeIdentifier> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final YangInstanceIdentifier rootIdentifier;
    private final LogicalDatastoreType datastoreType;

    /**
     * Default constructor.
     *
     * @param datastore {@link LogicalDatastoreType} of this identifier
     * @param path {@link YangInstanceIdentifier} path of this identifier
     * @throws NullPointerException if any argument is {@code null}
     */
    private DOMDataTreeIdentifier(final LogicalDatastoreType datastore, final YangInstanceIdentifier path) {
        datastoreType = requireNonNull(datastore);
        rootIdentifier = requireNonNull(path);
    }

    /**
     * Create a new {@link DOMDataTreeIdentifier} with specified datastore and path.
     *
     * @param datastore {@link LogicalDatastoreType} of this identifier
     * @param path {@link YangInstanceIdentifier} path of this identifier
     * @throws NullPointerException if any argument is {@code null}
     */
    public static DOMDataTreeIdentifier of(final LogicalDatastoreType datastore, final YangInstanceIdentifier path) {
        return new DOMDataTreeIdentifier(datastore, path);
    }

    @Override
    public LogicalDatastoreType datastore() {
        return datastoreType;
    }

    /**
     * Return the logical data store type.
     *
     * @return Logical data store type. Guaranteed to be non-null.
     * @deprecated Use {@link #datastore()} instead
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    public LogicalDatastoreType getDatastoreType() {
        return datastore();
    }

    @Override
    public YangInstanceIdentifier path() {
        return rootIdentifier;
    }

    /**
     * Return the {@link YangInstanceIdentifier} of the root node.
     *
     * @return Instance identifier corresponding to the root node.
     * @deprecated Use {@link #path()} instead
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    public YangInstanceIdentifier getRootIdentifier() {
        return path();
    }

    public DOMDataTreeIdentifier toOptimized() {
        final var opt = rootIdentifier.toOptimized();
        return opt == rootIdentifier ? this : DOMDataTreeIdentifier.of(datastoreType, opt);
    }

    @Override
    public int compareTo(final DOMDataTreeIdentifier domDataTreeIdentifier) {
        int cmp = datastoreType.compareTo(domDataTreeIdentifier.datastoreType);
        if (cmp != 0) {
            return cmp;
        }

        final var myIter = rootIdentifier.getPathArguments().iterator();
        final var otherIter = domDataTreeIdentifier.rootIdentifier.getPathArguments().iterator();

        while (myIter.hasNext()) {
            if (!otherIter.hasNext()) {
                return 1;
            }

            final var myPathArg = myIter.next();
            final var otherPathArg = otherIter.next();
            cmp = myPathArg.compareTo(otherPathArg);
            if (cmp != 0) {
                return cmp;
            }
        }

        return otherIter.hasNext() ? -1 : 0;
    }

    @Override
    public int hashCode() {
        return datastoreType.hashCode() * 31 + rootIdentifier.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DOMDataTreeIdentifier other && datastoreType == other.datastoreType
            && rootIdentifier.equals(other.rootIdentifier);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("datastore", datastoreType).add("root", rootIdentifier).toString();
    }

    @java.io.Serial
    Object writeReplace() {
        return new DTIv1(this);
    }
}
